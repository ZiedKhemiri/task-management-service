package com.example.taskmanagementservice.presentation;

import com.example.taskmanagementservice.application.dto.AssignTaskRequest;
import com.example.taskmanagementservice.application.dto.CreateTaskRequest;
import com.example.taskmanagementservice.application.dto.TaskResponse;
import com.example.taskmanagementservice.application.dto.UpdateTaskRequest;
import com.example.taskmanagementservice.application.security.CurrentUser;
import com.example.taskmanagementservice.application.service.TaskService;
import com.example.taskmanagementservice.domain.model.Task;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Create a new task", description = "Admin only")
    public ResponseEntity<TaskResponse> create(@RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN') or @taskSecurity.isAssignee(#id, authentication.name)")
    @Operation(summary = "Get a task by ID", description = "Admin, or the user it's assigned to")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List all tasks", description = "Optionally filter by status or priority. Admins see every task, users only see tasks assigned to them.")
    public ResponseEntity<List<TaskResponse>> getAll(
            @Parameter(description = "Filter by status: TODO, IN_PROGRESS, DONE")
            @RequestParam(required = false) Task.Status status,
            @Parameter(description = "Filter by priority: LOW, MEDIUM, HIGH")
            @RequestParam(required = false) Task.Priority priority,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.getAll(status, priority, CurrentUser.from(authentication)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Update task details", description = "Admin only")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id, @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Assign a task to a user", description = "Admin only. Can (re)assign to any user.")
    public ResponseEntity<TaskResponse> assign(@PathVariable Long id, @RequestBody AssignTaskRequest request) {
        return ResponseEntity.ok(taskService.assign(id, request));
    }

    @PatchMapping("/{id}/claim")
    @Operation(summary = "Self-claim an unassigned task", description = "Assigns the task to the calling user, only if it has no assignee yet")
    public ResponseEntity<TaskResponse> claim(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(taskService.claim(id, CurrentUser.from(authentication)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN') or @taskSecurity.isAssignee(#id, authentication.name)")
    @Operation(summary = "Change task status", description = "Transitions: TODO→IN_PROGRESS→DONE. Reopen sets back to TODO. Admin, or the user it's assigned to.")
    public ResponseEntity<TaskResponse> changeStatus(
            @PathVariable Long id,
            @Parameter(description = "New status: TODO, IN_PROGRESS, DONE")
            @RequestParam Task.Status status) {
        return ResponseEntity.ok(taskService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Delete a task", description = "Admin only")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
