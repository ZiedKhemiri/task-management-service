package com.example.taskmanagementservice.infrastructure.persistence;

import com.example.taskmanagementservice.domain.model.Task;
import com.example.taskmanagementservice.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskRepositoryAdapter implements TaskRepository {

    private final JpaTaskRepository jpa;

    @Override public Task save(Task task)                              { return jpa.save(task); }
    @Override public Optional<Task> findById(Long id)                 { return jpa.findById(id); }
    @Override public List<Task> findAll()                             { return jpa.findAll(); }
    @Override public List<Task> findByStatus(Task.Status status)      { return jpa.findByStatus(status); }
    @Override public List<Task> findByPriority(Task.Priority priority){ return jpa.findByPriority(priority); }
    @Override public void deleteById(Long id)                         { jpa.deleteById(id); }
    @Override public boolean existsById(Long id)                      { return jpa.existsById(id); }

    @Override public Optional<Task> findByIdAndEnterpriseId(Long id, Long enterpriseId) {
        return jpa.findByIdAndEnterpriseId(id, enterpriseId);
    }
    @Override public List<Task> findByEnterpriseId(Long enterpriseId) {
        return jpa.findByEnterpriseId(enterpriseId);
    }
    @Override public List<Task> findByEnterpriseIdAndStatus(Long enterpriseId, Task.Status status) {
        return jpa.findByEnterpriseIdAndStatus(enterpriseId, status);
    }
    @Override public List<Task> findByEnterpriseIdAndPriority(Long enterpriseId, Task.Priority priority) {
        return jpa.findByEnterpriseIdAndPriority(enterpriseId, priority);
    }
    @Override public void deleteByIdAndEnterpriseId(Long id, Long enterpriseId) {
        jpa.deleteByIdAndEnterpriseId(id, enterpriseId);
    }
    @Override public boolean existsByIdAndEnterpriseId(Long id, Long enterpriseId) {
        return jpa.existsByIdAndEnterpriseId(id, enterpriseId);
    }
}
