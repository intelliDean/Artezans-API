package com.api.artezans.task.service;


import com.api.artezans.task.data.dto.TaskRequest;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.utils.ApiResponse;

import java.util.List;

public interface TaskService {
    ApiResponse postATask(TaskRequest request);
    List<Task> findActiveTasks();
    List<Task> adminViewAllTasks();
    ApiResponse deletePost(Long postId);

}