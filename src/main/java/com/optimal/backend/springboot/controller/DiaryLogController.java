package com.optimal.backend.springboot.controller;

import com.optimal.backend.springboot.domain.entity.CategoryEnum;
import com.optimal.backend.springboot.domain.entity.DiaryLog;
import com.optimal.backend.springboot.domain.entity.Tag;
import com.optimal.backend.springboot.domain.repository.DiaryLogRepository;
import com.optimal.backend.springboot.domain.repository.TagRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diary-logs")
public class DiaryLogController {
    private final DiaryLogRepository diaryLogRepo;
    private final TagRepository        tagRepo;

    public DiaryLogController(DiaryLogRepository diaryLogRepo,
                              TagRepository tagRepo) {
        this.diaryLogRepo = diaryLogRepo;
        this.tagRepo      = tagRepo;
    }

    // Helper to get the user ID - simplified for now
    private UUID getUserId(Authentication auth) {
        // For now, return a default UUID - this should be implemented properly with JWT
        return UUID.randomUUID();
    }

    @GetMapping
    public List<DiaryLog> listLogs(@AuthenticationPrincipal Authentication auth) {
        return diaryLogRepo.findAllByUserId(getUserId(auth));
    }

    @PostMapping
    public DiaryLog createLog(@AuthenticationPrincipal Authentication auth,
                              @RequestBody DiaryLog dto) {
        UUID userId = getUserId(auth);
        dto.setUserId(userId);

        // Upsert tags by name
        Set<Tag> finalTags = dto.getTags().stream()
            .map(t -> tagRepo.findByNameIgnoreCase(t.getName())
                 .orElseGet(() -> tagRepo.save(
                     Optional.of(new Tag())
                             .map(n -> {n.setName(t.getName()); return n;})
                             .get()
                 )))
            .collect(Collectors.toSet());

        dto.setTags(finalTags);
        return diaryLogRepo.save(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiaryLog> getLog(@AuthenticationPrincipal Authentication auth,
                                           @PathVariable UUID id) {
        return diaryLogRepo.findById(id)
            .filter(dl -> dl.getUserId().equals(getUserId(auth)))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiaryLog> updateLog(@AuthenticationPrincipal Authentication auth,
                                              @PathVariable UUID id,
                                              @RequestBody DiaryLog dto) {
        return diaryLogRepo.findById(id)
            .filter(dl -> dl.getUserId().equals(getUserId(auth)))
            .map(existing -> {
                existing.setEntry(dto.getEntry());
                existing.setCategory(dto.getCategory());
                // handle tags the same way as create…
                Set<Tag> finalTags = dto.getTags().stream()
                  .map(t -> tagRepo.findByNameIgnoreCase(t.getName())
                       .orElseGet(() -> tagRepo.save(
                           Optional.of(new Tag())
                                   .map(n -> {n.setName(t.getName()); return n;})
                                   .get()
                       )))
                  .collect(Collectors.toSet());
                existing.setTags(finalTags);
                return ResponseEntity.ok(diaryLogRepo.save(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@AuthenticationPrincipal Authentication auth,
                                          @PathVariable UUID id) {
        return diaryLogRepo.findById(id)
            .filter(dl -> dl.getUserId().equals(getUserId(auth)))
            .map(dl -> {
                diaryLogRepo.delete(dl);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
