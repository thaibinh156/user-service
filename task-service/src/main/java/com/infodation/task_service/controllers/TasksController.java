package com.infodation.task_service.controllers;

import com.infodation.task_service.client.SpiceDBClient;
import com.infodation.task_service.components.JwtAuthenticationFilter;
import com.infodation.task_service.models.AssignPermissionRequest;
import com.infodation.task_service.models.CheckPermissionRequest;
import com.infodation.task_service.models.Task;
import com.infodation.task_service.models.TaskProjection;
import com.infodation.task_service.models.dto.TaskAssignmentDTO;
import com.infodation.task_service.models.dto.TaskCreateDTO;
import com.infodation.task_service.models.dto.TaskUpdateDTO;
import com.infodation.task_service.services.iServices.ITaskService;
import com.infodation.task_service.utils.ApiResponse;
import com.infodation.task_service.utils.ApiResponseUtil;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TasksController {

    private final ITaskService taskService;
    private static final Logger log = LoggerFactory.getLogger(TasksController.class);
    private final SpiceDBClient spiceDBClient;

    public TasksController(ITaskService taskService, SpiceDBClient spiceDBClient) {
        this.taskService = taskService;
        this.spiceDBClient = spiceDBClient;
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignTaskToUser(@RequestBody TaskAssignmentDTO taskAssignmentDTO) {
        try {
            log.info("Assigning task {} to user {}", taskAssignmentDTO.getTaskId(), taskAssignmentDTO.getUserId());
            // Save assignment to database
            taskService.assignTaskToUser(taskAssignmentDTO);
            return ResponseEntity.ok("Task assigned successfully.");
        } catch (Exception e) {
            log.error("Error occurred while assigning task: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while assigning task.");
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskProjection>> getTasksByUserId(@PathVariable Long userId) {
        log.info("Received request to get tasks for user with ID: {}", userId);
        try {
            // Retrieve the list of tasks from TaskService
            List<TaskProjection> tasks = taskService.getTasksByUserId(userId);
            // If no tasks are found
            if (tasks.isEmpty()) {
                log.warn("No tasks found for user with ID: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            log.info("Successfully retrieved tasks for user with ID: {}", userId);
            // Return the list of tasks
            return ResponseEntity.ok(tasks);

        } catch (Exception e) {
            // If an error occurs
            log.error("Error occurred while fetching tasks for user with ID: {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/migrate")
    public ResponseEntity<ApiResponse<?>> importTasks(@RequestParam("file") MultipartFile file) throws Exception {
        log.info("Starting import process for file: '{}'", file.getOriginalFilename());
        String message;
        HttpStatus status;

        if (file == null || file.isEmpty()) {
            message = ("The uploaded file is empty.");
            status = HttpStatus.BAD_REQUEST;

            log.error(message);
        } else {
            taskService.importTaskFromCSVFile(file);
            status = HttpStatus.OK;
            message = "Upload file successfully";
            log.info(message);
        }
        ApiResponse<?> response = ApiResponseUtil.buildApiResponse(null, HttpStatus.OK, message, null);
        return new ResponseEntity<>(response, status);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTask(@RequestBody TaskCreateDTO task) {
        String message;
        HttpStatus status;

        String createdBy = JwtAuthenticationFilter.USER_ID;
        log.info("Received request to create task with title: {} by user with ID: {}", task.getTitle(), createdBy);
        task.setUserId(createdBy);
        Task savedTask = taskService.saveTask(task);
        if (savedTask != null) {
            message = "Task created successfully";
            status = HttpStatus.CREATED;
            // Assign permission to the task using the
            spiceDBClient.assignPermission(new AssignPermissionRequest(savedTask.getId().toString(),
                    createdBy,
                    "task",
                    "user",
                    "owner"));
            log.info(message);
        } else {
            message = "Error occurred while creating task";
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error(message);
        }

        ApiResponse<?> response = ApiResponseUtil.buildApiResponse(null, HttpStatus.OK, message + "task_id: " +savedTask.getId(), null);
        return new ResponseEntity<>(response, status);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(
            @PathVariable("id") Long taskId,
            @RequestBody TaskUpdateDTO updatedTask) {

        String message;
        HttpStatus status;
        Task data = null;

        if (taskId == null) {
            message = "Task ID is Null";
            status = HttpStatus.BAD_REQUEST;
        } else {
            boolean hasPermission = spiceDBClient.checkPermission(
                    new CheckPermissionRequest(
                            taskId.toString(),
                            JwtAuthenticationFilter.USER_ID.toString(),
                            "task",
                            "user",
                            "write"
                    )
            );

            if (hasPermission) {
                data = taskService.updateTask(taskId, updatedTask);
                if (data == null) {
                    message = "Task not found";
                    status = HttpStatus.NOT_FOUND;
                } else {
                    message = "Task " + taskId + " updated successfully";
                    status = HttpStatus.OK;
                }
            } else {
                message = "You do not have permission to update this task";
                status = HttpStatus.UNAUTHORIZED;
            }
        }

        ApiResponse<Task> response = ApiResponseUtil.buildApiResponse(data, status, message, null);
        return new ResponseEntity<>(response, status);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable("id") Long taskId) {
        String message;
        HttpStatus status;
        Task data = null;

        if (taskId == null) {
            message = "task id is Null";
            status = HttpStatus.BAD_REQUEST;
        } else {
            boolean hasPermission = spiceDBClient.checkPermission(new CheckPermissionRequest(taskId.toString(),
                    JwtAuthenticationFilter.USER_ID.toString(),
                    "task",
                    "user",
                    "read"));
            if (hasPermission) {
                data = taskService.getTaskById(taskId);
                if (data == null) {
                    message = "Not found";
                    status = HttpStatus.NOT_FOUND;
                } else {
                    message = "Get task " + taskId + " successful";
                    status = HttpStatus.OK;
                }
            } else {
                message = "You can not can read this task";
                status = HttpStatus.UNAUTHORIZED;
            }

        }
        ApiResponse response = ApiResponseUtil.buildApiResponse(data, status, message, null);
        return new ResponseEntity<>(response, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable("id") Long taskId) {
        String message;
        HttpStatus status;

        if (taskId == null) {
            message = "Task ID is Null";
            status = HttpStatus.BAD_REQUEST;
        } else {
            boolean hasPermission = spiceDBClient.checkPermission(
                    new CheckPermissionRequest(
                            taskId.toString(),
                            JwtAuthenticationFilter.USER_ID.toString(),
                            "task",
                            "user",
                            "delete"
                    )
            );

            if (hasPermission) {
                boolean deleted = taskService.deleteTask(taskId);
                if (deleted) {
                    message = "Task " + taskId + " deleted successfully";
                    status = HttpStatus.OK;
                } else {
                    message = "Task not found";
                    status = HttpStatus.NOT_FOUND;
                }
            } else {
                message = "You do not have permission to delete this task";
                status = HttpStatus.UNAUTHORIZED;
            }
        }

        ApiResponse<Void> response = ApiResponseUtil.buildApiResponse(null, status, message, null);
        return new ResponseEntity<>(response, status);
    }

}