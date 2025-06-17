// src/main/java/com/optimal/backend/springboot/domain/repository/TaskRepository.java
package com.optimal.backend.springboot.domain.repository;

import com.optimal.backend.springboot.domain.entity.Task;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
      SELECT t
        FROM Task t
       WHERE t.userId = :userId
    ORDER BY t.createdDate DESC
    """)
    List<Task> findByUserId(@Param("userId") UUID userId);
    
}
