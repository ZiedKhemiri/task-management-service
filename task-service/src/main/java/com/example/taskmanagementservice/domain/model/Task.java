package com.example.taskmanagementservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    private LocalDate dueDate;

    @Column(name = "assignee_id")
    private String assigneeId;

    @Column(name = "assignee_username")
    private String assigneeUsername;

    @Column(name = "enterprise_id")
    private Long enterpriseId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Task(String title, String description, Priority priority, LocalDate dueDate, Long enterpriseId) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title must not be blank");
        if (enterpriseId == null) throw new IllegalArgumentException("Enterprise id must not be null");
        this.title = title;
        this.description = description;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.dueDate = dueDate;
        this.status = Status.TODO;
        this.enterpriseId = enterpriseId;
    }

    public void start() {
        if (this.status != Status.TODO) {
            throw new IllegalStateException("Only a TODO task can be started");
        }
        this.status = Status.IN_PROGRESS;
    }

    public void complete() {
        if (this.status == Status.DONE) {
            throw new IllegalStateException("Task is already completed");
        }
        this.status = Status.DONE;
    }

    public void reopen() {
        this.status = Status.TODO;
    }

    public void updateDetails(String title, String description, Priority priority, LocalDate dueDate) {
        if (title != null && !title.isBlank()) this.title = title;
        if (description != null) this.description = description;
        if (priority != null) this.priority = priority;
        this.dueDate = dueDate;
    }

    public void assignTo(String assigneeId, String assigneeUsername) {
        if (assigneeId == null || assigneeId.isBlank()) throw new IllegalArgumentException("Assignee id must not be blank");
        this.assigneeId = assigneeId;
        this.assigneeUsername = assigneeUsername;
    }

    public boolean isUnassigned() {
        return assigneeId == null;
    }

    public boolean isAssignedTo(String userId) {
        return assigneeId != null && assigneeId.equals(userId);
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status { TODO, IN_PROGRESS, DONE }

    public enum Priority { LOW, MEDIUM, HIGH }
}
