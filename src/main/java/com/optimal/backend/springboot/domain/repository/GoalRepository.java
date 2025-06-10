// src/main/java/com/optimal/backend/springboot/domain/repository/GoalRepository.java
package com.optimal.backend.springboot.domain.repository;

import com.optimal.backend.springboot.domain.entity.Goal;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {

    @Query("""
      SELECT g
        FROM Goal g
       WHERE g.userId = :userId
    ORDER BY g.createdAt DESC
    """)
    List<Goal> findByUserId(@Param("userId") UUID userId);
}
