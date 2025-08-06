// src/main/java/com/optimal/backend/springboot/controller/PersonaController.java
package com.optimal.backend.springboot.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimal.backend.springboot.database.entity.Persona;
import com.optimal.backend.springboot.service.PersonaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/personas")
public class PersonaController {

    private final PersonaService personaService;

    /** GET /api/users/{userId}/personas */
    @GetMapping
    public ResponseEntity<List<Persona>> list(@PathVariable UUID userId) {
        return ResponseEntity.ok(personaService.listForUser(userId));
    }

    /** PUT /api/users/{userId}/personas/{personaId}/default */
    @PutMapping("/{personaId}/default")
    public ResponseEntity<Persona> setDefault(
        @PathVariable UUID userId,
        @PathVariable UUID personaId
    ) {
        return ResponseEntity.ok(personaService.setDefault(userId, personaId));
    }
}
