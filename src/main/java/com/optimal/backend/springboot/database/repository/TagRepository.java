package com.optimal.backend.springboot.database.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimal.backend.springboot.database.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByNameIgnoreCase(String name);

    
}
