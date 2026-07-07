package com.example.taskmanagementservice.domain.repository;

import com.example.taskmanagementservice.domain.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);

    // Global lookups — SUPER_ADMIN only; callers must gate these on TenantContext.isSuperAdmin().
    Optional<Task> findById(Long id);
    List<Task> findAll();
    List<Task> findByStatus(Task.Status status);
    List<Task> findByPriority(Task.Priority priority);
    void deleteById(Long id);
    boolean existsById(Long id);

    // Tenant-scoped lookups — used for every non-SUPER_ADMIN caller.
    Optional<Task> findByIdAndEnterpriseId(Long id, Long enterpriseId);
    List<Task> findByEnterpriseId(Long enterpriseId);
    List<Task> findByEnterpriseIdAndStatus(Long enterpriseId, Task.Status status);
    List<Task> findByEnterpriseIdAndPriority(Long enterpriseId, Task.Priority priority);
    void deleteByIdAndEnterpriseId(Long id, Long enterpriseId);
    boolean existsByIdAndEnterpriseId(Long id, Long enterpriseId);
}
