// src/main/java/com/optimal/backend/springboot/domain/repository/PersonaRepository.java
package com.optimal.backend.springboot.domain.repository;

import com.optimal.backend.springboot.domain.entity.Persona;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, UUID> {

    List<Persona> findByUserId(UUID userId);

    Optional<Persona> findByUserIdAndIsDefaultTrue(UUID userId);

    @Modifying
    @Query("UPDATE Persona p SET p.isDefault = FALSE WHERE p.userId = :userId")
    void clearDefaultForUser(@Param("userId") UUID userId);
}
