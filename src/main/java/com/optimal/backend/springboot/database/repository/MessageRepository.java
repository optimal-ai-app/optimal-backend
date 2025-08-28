// src/main/java/com/optimal/backend/springboot/domain/repository/MessageRepository.java
package com.optimal.backend.springboot.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.optimal.backend.springboot.database.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

}