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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final Long ENTERPRISE_ID = 1L;
    private static final Long OTHER_ENTERPRISE_ID = 2L;
    private static final CurrentUser ADMIN = new CurrentUser("admin-1", "admin", true);

    @Mock
    TaskRepository taskRepository;

    @InjectMocks
    TaskService taskService;

    Task task;

    @BeforeEach
    void setUp() {
        // Default: an ADMIN of ENTERPRISE_ID, not a cross-tenant SUPER_ADMIN.
        TenantContext.set(ENTERPRISE_ID, false);
        task = new Task("Fix bug", "Login page crash", Task.Priority.HIGH, LocalDate.now().plusDays(1), ENTERPRISE_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_savesAndReturnsResponse() {
        when(taskRepository.save(any())).thenReturn(task);

        TaskResponse response = taskService.create(
                new CreateTaskRequest("Fix bug", "Login page crash", Task.Priority.HIGH, null));

        assertThat(response.title()).isEqualTo("Fix bug");
        assertThat(response.status()).isEqualTo(Task.Status.TODO);
        assertThat(response.enterpriseId()).isEqualTo(ENTERPRISE_ID);
        verify(taskRepository).save(any());
    }

    @Test
    void create_throwsWhenNoEnterpriseOnToken() {
        TenantContext.set(null, false);

        assertThatThrownBy(() -> taskService.create(
                new CreateTaskRequest("Fix bug", null, Task.Priority.HIGH, null)))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(taskRepository.findByIdAndEnterpriseId(99L, ENTERPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getById_throwsWhenTaskBelongsToAnotherEnterprise() {
        // A cross-tenant task id simply doesn't match the tenant-scoped query, same as "not found".
        when(taskRepository.findByIdAndEnterpriseId(1L, ENTERPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(1L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void getById_superAdminBypassesTenantScoping() {
        TenantContext.set(null, true);
        Task otherTenantTask = new Task("Other tenant task", null, Task.Priority.LOW, null, OTHER_ENTERPRISE_ID);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(otherTenantTask));

        TaskResponse response = taskService.getById(5L);

        assertThat(response.enterpriseId()).isEqualTo(OTHER_ENTERPRISE_ID);
    }

    @Test
    void getAll_filtersByStatus() {
        when(taskRepository.findByEnterpriseIdAndStatus(ENTERPRISE_ID, Task.Status.TODO)).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getAll(Task.Status.TODO, null, ADMIN);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(Task.Status.TODO);
    }

    @Test
    void getAll_nonAdminOnlySeesOwnTasks() {
        task.assignTo("user-1", "alice");
        Task othersTask = new Task("Other task", null, Task.Priority.LOW, null, ENTERPRISE_ID);
        othersTask.assignTo("user-2", "bob");
        when(taskRepository.findByEnterpriseId(ENTERPRISE_ID)).thenReturn(List.of(task, othersTask));

        List<TaskResponse> result = taskService.getAll(null, null, new CurrentUser("user-1", "alice", false));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).assigneeId()).isEqualTo("user-1");
    }

    @Test
    void assign_setsAssignee() {
        when(taskRepository.findByIdAndEnterpriseId(1L, ENTERPRISE_ID)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        TaskResponse response = taskService.assign(1L, new AssignTaskRequest("user-1", "alice"));

        assertThat(response.assigneeId()).isEqualTo("user-1");
        assertThat(response.assigneeUsername()).isEqualTo("alice");
    }

    @Test
    void claim_assignsToCallingUserWhenUnassigned() {
        when(taskRepository.findByIdAndEnterpriseId(1L, ENTERPRISE_ID)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        TaskResponse response = taskService.claim(1L, new CurrentUser("user-1", "alice", false));

        assertThat(response.assigneeId()).isEqualTo("user-1");
    }

    @Test
    void claim_throwsWhenAlreadyAssigned() {
        task.assignTo("user-1", "alice");
        when(taskRepository.findByIdAndEnterpriseId(1L, ENTERPRISE_ID)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.claim(1L, new CurrentUser("user-2", "bob", false)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void update_changesFields() {
        when(taskRepository.findByIdAndEnterpriseId(1L, ENTERPRISE_ID)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        taskService.update(1L, new UpdateTaskRequest("New title", null, Task.Priority.LOW, null));

        verify(taskRepository).save(task);
    }

    @Test
    void changeStatus_toInProgress_succeeds() {
        when(taskRepository.findByIdAndEnterpriseId(1L, ENTERPRISE_ID)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        taskService.changeStatus(1L, Task.Status.IN_PROGRESS);

        assertThat(task.getStatus()).isEqualTo(Task.Status.IN_PROGRESS);
    }

    @Test
    void changeStatus_startAlreadyInProgress_throws() {
        task.start();
        when(taskRepository.findByIdAndEnterpriseId(1L, ENTERPRISE_ID)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.changeStatus(1L, Task.Status.IN_PROGRESS))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(taskRepository.existsByIdAndEnterpriseId(99L, ENTERPRISE_ID)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
