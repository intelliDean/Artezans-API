package com.api.artezans.task.service;


import task.hub.user.task.data.dto.TaskRequest;
import task.hub.user.task.data.model.Task;
import task.hub.user.utils.ApiResponse;

import java.util.List;

public interface TaskService {
    ApiResponse postATask(TaskRequest request);
    List<Task> findActiveTasks();
    List<Task> adminViewAllTasks();

    ApiResponse deletePost(Long postId);

}