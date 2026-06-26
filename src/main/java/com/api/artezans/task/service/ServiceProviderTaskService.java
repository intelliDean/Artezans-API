package com.api.artezans.task.service;

import com.api.artezans.task.data.model.Task;
import com.api.artezans.task.data.repo.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ServiceProviderTaskService {

    private final TaskRepository taskRepository;


    private List<Task> findTaskByServiceName(String serviceName) {
        return taskRepository.findAllByTaskServiceNameIgnoreCase(serviceName);
    }

    public List<Task> serviceProviderViewPeculiarTasks(List<String> serviceNames) {
        return serviceNames.parallelStream()
                .map(this::findTaskByServiceName)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
