package com.example.taskmanagementservice.application.service;

import com.example.taskmanagementservice.application.dto.AssignTaskRequest;
import com.example.taskmanagementservice.application.dto.CreateTaskRequest;
import com.example.taskmanagementservice.application.dto.TaskResponse;
import com.example.taskmanagementservice.application.dto.UpdateTaskRequest;
import com.example.taskmanagementservice.application.security.CurrentUser;
import com.example.taskmanagementservice.application.security.TenantContext;
import com.example.taskmanagementservice.domain.exception.TaskNotFoundException;
import com.example.taskmanagementservice.domain.model.Task;
import com.example.taskmanagementservice.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Every read/write here is scoped to the calling user's enterprise, resolved via
 * {@link TenantContext} (populated per-request from the JWT's {@code enterprise_id}
 * claim by {@code TenantFilter}). SUPER_ADMIN is the only role that bypasses tenant
 * scoping and can see/act across enterprises.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskResponse create(CreateTaskRequest request) {
        Long enterpriseId = requireEnterpriseId();
        Task task = new Task(request.title(), request.description(), request.priority(), request.dueDate(), enterpriseId);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        return TaskResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAll(Task.Status status, Task.Priority priority, CurrentUser currentUser) {
        List<Task> tasks;
        if (TenantContext.isSuperAdmin()) {
            if (status != null)         tasks = taskRepository.findByStatus(status);
            else if (priority != null)  tasks = taskRepository.findByPriority(priority);
            else                        tasks = taskRepository.findAll();
        } else {
            Long enterpriseId = requireEnterpriseId();
            if (status != null)         tasks = taskRepository.findByEnterpriseIdAndStatus(enterpriseId, status);
            else if (priority != null)  tasks = taskRepository.findByEnterpriseIdAndPriority(enterpriseId, priority);
            else                        tasks = taskRepository.findByEnterpriseId(enterpriseId);
        }

        if (!currentUser.admin() && !TenantContext.isSuperAdmin()) {
            tasks = tasks.stream().filter(task -> task.isAssignedTo(currentUser.id())).toList();
        }
        return tasks.stream().map(TaskResponse::from).toList();
    }

    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Task task = findOrThrow(id);
        task.updateDetails(request.title(), request.description(), request.priority(), request.dueDate());
        return TaskResponse.from(taskRepository.save(task));
    }

    public TaskResponse changeStatus(Long id, Task.Status newStatus) {
        Task task = findOrThrow(id);
        switch (newStatus) {
            case IN_PROGRESS -> task.start();
            case DONE        -> task.complete();
            case TODO        -> task.reopen();
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    public TaskResponse assign(Long id, AssignTaskRequest request) {
        Task task = findOrThrow(id);
        task.assignTo(request.userId(), request.username());
        return TaskResponse.from(taskRepository.save(task));
    }

    public TaskResponse claim(Long id, CurrentUser currentUser) {
        Task task = findOrThrow(id);
        if (!task.isUnassigned()) {
            throw new IllegalStateException("Task is already assigned");
        }
        task.assignTo(currentUser.id(), currentUser.username());
        return TaskResponse.from(taskRepository.save(task));
    }

    public void delete(Long id) {
        if (TenantContext.isSuperAdmin()) {
            if (!taskRepository.existsById(id)) throw new TaskNotFoundException(id);
            taskRepository.deleteById(id);
            return;
        }
        Long enterpriseId = requireEnterpriseId();
        if (!taskRepository.existsByIdAndEnterpriseId(id, enterpriseId)) throw new TaskNotFoundException(id);
        taskRepository.deleteByIdAndEnterpriseId(id, enterpriseId);
    }

    private Task findOrThrow(Long id) {
        if (TenantContext.isSuperAdmin()) {
            return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        }
        Long enterpriseId = requireEnterpriseId();
        return taskRepository.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private Long requireEnterpriseId() {
        Long enterpriseId = TenantContext.getEnterpriseId();
        if (enterpriseId == null) {
            throw new AccessDeniedException("No enterprise associated with the current user");
        }
        return enterpriseId;
    }
}
