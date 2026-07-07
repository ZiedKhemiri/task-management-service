package com.example.taskmanagementservice.application.security;

import com.example.taskmanagementservice.domain.model.Task;
import com.example.taskmanagementservice.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("taskSecurity")
@RequiredArgsConstructor
public class TaskSecurity {

    private final TaskRepository taskRepository;

    /**
     * Runs inside {@code @PreAuthorize}, ahead of {@code TaskService}, so it applies its
     * own tenant scoping via {@link TenantContext} rather than relying on the service.
     * A task in a different enterprise is treated the same as a nonexistent one.
     */
    public boolean isAssignee(Long taskId, String userId) {
        Optional<Task> task = TenantContext.isSuperAdmin()
                ? taskRepository.findById(taskId)
                : taskRepository.findByIdAndEnterpriseId(taskId, TenantContext.getEnterpriseId());
        return task.map(t -> t.isAssignedTo(userId)).orElse(false);
    }
}
