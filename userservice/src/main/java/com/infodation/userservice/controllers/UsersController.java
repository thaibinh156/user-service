package com.infodation.userservice.controllers;

import com.infodation.userservice.models.TaskDTO.TaskUserResponseDTO;
import com.infodation.userservice.models.User;
import com.infodation.userservice.models.dto.user.CreateUserDTO;
import com.infodation.userservice.models.dto.user.UpdateUserDTO;
import com.infodation.userservice.repositories.UserRepository;
import com.infodation.userservice.services.iservice.IUserService;
import com.infodation.userservice.utils.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import com.infodation.userservice.utils.ApiResponseUtil;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    private final IUserService userService;
    private final UserRepository userRepository;

    public UsersController(IUserService userService, UserRepository userRepository, RestTemplate restTemplate) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }
    private final RestTemplate restTemplate;
    // API receives user_id and calls the task-service API to fetch the user's tasks
    @GetMapping("/{userId}/tasks")
    public ResponseEntity<TaskUserResponseDTO> getUserWithTasks(@PathVariable String userId) {
        // Kiểm tra xem người dùng có tồn tại không
        User user = userRepository.findByUserId(userId).orElse(null);

        if (user == null) {
            logger.info("User not found for user_id: " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Trả về 404
        }

        try {
            // Chỉ thực hiện logic khi người dùng tồn tại
            TaskUserResponseDTO response = userService.getUserWithTasks(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi bất ngờ
            logger.error("An error occurred while fetching tasks for user_id: " + userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/csv-migrate")
    public ResponseEntity<String> importUsers(@RequestPart("file") MultipartFile file) {
        logger.info("CSV migration started");
        try {
            userService.importUsersFromCsvAsync(file);
            logger.info("CSV file is being processed in the background");
            return ResponseEntity.ok("CSV file is being processed in the background");
        } catch (Exception e) {
            logger.error("Error occurred during CSV import", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during CSV import");
        }
    }

    @PutMapping("/bulk-edit")
    public ResponseEntity<ApiResponse<String>> bulkEdit(@RequestBody List<UpdateUserDTO> usersDTO) {
        logger.info("Bulk edit started for {} users", usersDTO.size());
        userService.bulkEditUsersAsync(usersDTO);
        ApiResponse<String> response = ApiResponseUtil.buildApiResponse(
                "The update is working on background",
                HttpStatus.OK,
                "Bulk update initiated",
                null
        );
        logger.info("Bulk edit request initiated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<User>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size,
            @RequestParam(required = false) String name
    ) {
        logger.info("Fetching users with page: {} and size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getAll(pageable, name);
        ApiResponse<Page<User>> apiResponse = ApiResponseUtil.buildApiResponse(users, HttpStatus.OK, "Users fetched successfully", null);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable("userId") String userId) {
        logger.info("Fetching user with ID: {}", userId);
        User user = userService.getByUserId(userId);
        HttpStatus status = user == null ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        String message = user == null ? String.format("User with ID %s not found", userId) : "User fetched successfully";

        ApiResponse<User> response = ApiResponseUtil.buildApiResponse(user, status, message, null);
        return new ResponseEntity<>(response, status);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> create(@Valid @RequestBody CreateUserDTO userDto) {
        logger.info("Creating user with ID: {}", userDto.getUserId());
        User existingUser = userService.getByUserId(userDto.getUserId());
        HttpStatus status;
        String message;
        User savedUser = null;

        if (existingUser != null) {
            status = HttpStatus.CONFLICT;
            message = String.format("User with ID %s already exists", userDto.getUserId());
            logger.warn("User with ID {} already exists", userDto.getUserId());
        } else {
            savedUser = userService.save(userDto);
            status = HttpStatus.CREATED;
            message = "User created successfully";
            logger.info("User with ID {} created successfully", userDto.getUserId());
        }

        ApiResponse<User> response = ApiResponseUtil.buildApiResponse(savedUser, status, message, null);
        return new ResponseEntity<>(response, status);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> update(@PathVariable("userId") String userId,@Valid @RequestBody UpdateUserDTO userDTO) {
        logger.info("Updating user with ID: {}", userId);
        User existingUser = userService.getByUserId(userId);

        HttpStatus status;
        String message;
        User updatedUser = null;

        if (existingUser == null) {
            status = HttpStatus.NOT_FOUND;
            message = String.format("User with ID %s not found", userId);
            logger.warn("User with ID {} not found", userId);
        } else {
            updatedUser = userService.update(userId, userDTO);
            status = HttpStatus.OK;
            message = "User updated successfully";
            logger.info("User with ID {} updated successfully", userId);
        }

        ApiResponse<User> response = ApiResponseUtil.buildApiResponse(updatedUser, status, message, null);

        return new ResponseEntity<>(response, status);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable("userId") String userId) {
        logger.info("Deleting user with ID: {}", userId);
        User existingUser = userService.getByUserId(userId);
        HttpStatus status;
        String message;

        if (existingUser == null) {
            status = HttpStatus.NOT_FOUND;
            message = String.format("User with ID %s not found", userId);
            logger.warn("User with ID {} not found", userId);
        } else {
            userService.delete(userId);
            status = HttpStatus.OK;
            message = String.format("User with ID %s deleted successfully", userId);
            logger.info("User with ID {} deleted successfully", userId);
        }

        ApiResponse<?> response = ApiResponseUtil.buildApiResponse(null, status, message, null);
        return new ResponseEntity<>(response, status);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        logger.error("Validation failed for request", ex);
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                .timestamp(LocalDateTime.now())
                .error("Validation Error")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .data(errors)
                .build());
    }
}
