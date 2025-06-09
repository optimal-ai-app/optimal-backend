// src/main/java/com/optimal/backend/springboot/service/PersonaService.java
package com.optimal.backend.springboot.service;

import com.optimal.backend.springboot.domain.entity.Persona;
import com.optimal.backend.springboot.domain.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;

    /** List all personas for a given user. */
    public List<Persona> listForUser(UUID userId) {
        return personaRepository.findByUserId(userId);
    }

    /** Mark the given persona as the user's default, unsetting any previous default. */
    @Transactional
    public Persona setDefault(UUID userId, UUID personaId) {
        // Clear any existing default
        personaRepository.clearDefaultForUser(userId);

        // Fetch and set new default
        Persona p = personaRepository.findById(personaId)
            .orElseThrow(() -> new IllegalArgumentException("Persona not found: " + personaId));
        if (!p.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Persona does not belong to user");
        }
        p.setIsDefault(true);
        return personaRepository.save(p);
    }
}
