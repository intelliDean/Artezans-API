package com.api.artezans.task.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import task.hub.user.task.data.model.Task;

import java.util.List;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

//    @Query(value = """
//            select task from Task task
//            where lower(task.taskServiceName) =
//            """)
    List<Task> findAllByTaskServiceNameIgnoreCase(String serviceName);

    @Query(value = """
            select task from Task task
            where task.isActive = true
            """)
    List<Task> findAllUndeletedLists();
}
