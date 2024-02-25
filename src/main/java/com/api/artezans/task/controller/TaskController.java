package com.api.artezans.task.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import task.hub.user.task.data.dto.TaskRequest;
import task.hub.user.task.data.model.Task;
import task.hub.user.task.service.TaskService;
import task.hub.user.utils.ApiResponse;

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
