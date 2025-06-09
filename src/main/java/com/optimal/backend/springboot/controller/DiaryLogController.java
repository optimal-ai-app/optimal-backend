package com.optimal.controller;

import com.optimal.model.CategoryEnum;
import com.optimal.model.DiaryLog;
import com.optimal.model.Tag;
import com.optimal.repository.DiaryLogRepository;
import com.optimal.repository.TagRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
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

    // Helper to get the Supabase user ID from the JWT
    private UUID getUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    @GetMapping
    public List<DiaryLog> listLogs(@AuthenticationPrincipal Jwt jwt) {
        return diaryLogRepo.findAllByUserId(getUserId(jwt));
    }

    @PostMapping
    public DiaryLog createLog(@AuthenticationPrincipal Jwt jwt,
                              @RequestBody DiaryLog dto) {
        UUID userId = getUserId(jwt);
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
    public ResponseEntity<DiaryLog> getLog(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable UUID id) {
        return diaryLogRepo.findById(id)
            .filter(dl -> dl.getUserId().equals(getUserId(jwt)))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiaryLog> updateLog(@AuthenticationPrincipal Jwt jwt,
                                              @PathVariable UUID id,
                                              @RequestBody DiaryLog dto) {
        return diaryLogRepo.findById(id)
            .filter(dl -> dl.getUserId().equals(getUserId(jwt)))
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
    public ResponseEntity<Void> deleteLog(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable UUID id) {
        return diaryLogRepo.findById(id)
            .filter(dl -> dl.getUserId().equals(getUserId(jwt)))
            .map(dl -> {
                diaryLogRepo.delete(dl);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
