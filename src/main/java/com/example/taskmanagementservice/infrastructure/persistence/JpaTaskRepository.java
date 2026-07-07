package com.example.taskmanagementservice.infrastructure.persistence;

import com.example.taskmanagementservice.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface JpaTaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(Task.Status status);
    List<Task> findByPriority(Task.Priority priority);

    Optional<Task> findByIdAndEnterpriseId(Long id, Long enterpriseId);
    List<Task> findByEnterpriseId(Long enterpriseId);
    List<Task> findByEnterpriseIdAndStatus(Long enterpriseId, Task.Status status);
    List<Task> findByEnterpriseIdAndPriority(Long enterpriseId, Task.Priority priority);
    void deleteByIdAndEnterpriseId(Long id, Long enterpriseId);
    boolean existsByIdAndEnterpriseId(Long id, Long enterpriseId);
}
