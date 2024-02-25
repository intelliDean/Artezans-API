package com.api.artezans.gateway.task;

import com.api.artezans.task.controller.TaskController;
import com.api.artezans.task.data.dto.TaskRequest;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.api.artezans.gateway.task.TaskUtil.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@AllArgsConstructor
@Tag(name = "Task Controller")
@RequestMapping("/api/v1/task")
public class TaskGateway {
    private final TaskController taskController;


    @PostMapping(value = "post", consumes = MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'SERVICE_PROVIDER')")
    @Operation(summary = POST_TASK_SUM, description = POST_TASK_DESC, operationId = POST_TASK_OP_ID)
    public ResponseEntity<ApiResponse> postATask(@ModelAttribute @Valid TaskRequest taskRequest) {
        return ResponseEntity.ok(taskController.postATask(taskRequest));
    }

    @GetMapping("active-tasks")
    @Operation(summary = ACTIVE_TASK_SUM, description = ACTIVE_TASK_DESC, operationId = ACTIVE_TASK_OP_ID)
    public ResponseEntity<List<Task>> findActivePosts() {
        return ResponseEntity.ok(taskController.findActiveTasks());
    }

    @PostMapping("delete-task/{postId}")
    @Operation(summary = DELETE_TASK_SUM, description = DELETE_TASK_DESC, operationId = DELETE_TASK_OP_ID)
    public ResponseEntity<ApiResponse> deleteTask(@PathVariable Long postId) {
        return ResponseEntity.ok(taskController.deleteTask(postId));
    }
}
