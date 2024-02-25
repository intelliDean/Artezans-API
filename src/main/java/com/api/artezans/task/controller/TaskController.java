package com.api.artezans.task.controller;

import com.api.artezans.task.data.dto.TaskRequest;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.task.service.TaskService;
import com.api.artezans.utils.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;

    public ApiResponse postATask(TaskRequest request) {
        return taskService.postATask(request);
    }

    public List<Task> findActiveTasks() {
        return taskService.findActiveTasks();
    }

    public List<Task> adminViewAllTasks(){
        return taskService.adminViewAllTasks();
    }
    public ApiResponse deleteTask(Long postId){
        return taskService.deletePost(postId);
    }
}
