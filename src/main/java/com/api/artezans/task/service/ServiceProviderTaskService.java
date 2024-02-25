package com.api.artezans.task.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import task.hub.user.task.data.model.Task;
import task.hub.user.task.data.repo.TaskRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ServiceProviderTaskService {

    private final TaskRepository taskRepository;


    private List<Task> findTaskByServiceName(String serviceName) {
        return taskRepository.findAllByTaskServiceNameIgnoreCase(serviceName);
    }


    public List<Task> serviceProviderViewPeculiarTasks(List<String> serviceNames) {
        List<Task> tasksToDisplay = new ArrayList<>();
        serviceNames.parallelStream()
                .map(this::findTaskByServiceName)
                .forEach(tasksToDisplay::addAll);
        return tasksToDisplay;
    }
}
