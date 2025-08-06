// src/main/java/com/optimal/backend/springboot/domain/repository/PersonaRepository.java
package com.optimal.backend.springboot.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.optimal.backend.springboot.database.entity.Persona;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, UUID> {

    List<Persona> findByUserId(UUID userId);

    Optional<Persona> findByUserIdAndDefaultPersonaTrue(UUID userId);

    @Modifying
    @Query("UPDATE Persona p SET p.defaultPersona = FALSE WHERE p.userId = :userId")
    void clearDefaultForUser(@Param("userId") UUID userId);
}
