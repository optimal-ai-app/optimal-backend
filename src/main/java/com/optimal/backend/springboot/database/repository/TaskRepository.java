// src/main/java/com/optimal/backend/springboot/database/repository/TaskRepository.java
package com.optimal.backend.springboot.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.optimal.backend.springboot.database.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

  @Query("""
        SELECT t
          FROM Task t
         WHERE t.userId = :userId
      ORDER BY t.createdDate DESC
      """)
  List<Task> findByUserId(@Param("userId") UUID userId);

  @Query("""
        SELECT t
          FROM Task t
          JOIN Goal g ON t.goalId = g.id
         WHERE g.title = :goalTitle AND t.userId = :userId
      ORDER BY t.createdDate DESC
      """)
  List<Task> findByUserIdAndGoalTitle(@Param("userId") UUID userId, @Param("goalTitle") String goalTitle);

  @Query("""
        SELECT t
          FROM Task t
         WHERE t.goalId = :goalId AND t.milestone = TRUE
      ORDER BY t.dueDate ASC, t.createdDate DESC
      """)
  List<Task> findMilestonesByGoalId(@Param("goalId") UUID goalId);

  @Query("""
        SELECT t
          FROM Task t
         WHERE t.userId = :userId
           AND t.dueDate IS NOT NULL
           AND t.dueDate < :dueDate
           AND t.status != 'completed'
           AND (t.milestone = FALSE OR t.milestone IS NULL)
      ORDER BY t.dueDate ASC
      """)
  List<Task> findUserTasksBeforeDueDate(@Param("userId") UUID userId, @Param("dueDate") java.sql.Timestamp dueDate);

  @Modifying
  @Query("""
        DELETE FROM Task t
         WHERE t.sharedId = :sharedId
      """)
  void deleteAllBySharedId(@Param("sharedId") UUID sharedId);

  @Modifying
  @Query("""
        DELETE FROM Task t2
         WHERE EXISTS (
             SELECT 1 FROM Task t1 
             WHERE t1.taskId = :taskId
               AND t2.sharedId = t1.sharedId 
               AND t2.dueDate >= t1.dueDate
         )
      """)
  void deleteTaskAndAfter(@Param("taskId") UUID taskId);

}
