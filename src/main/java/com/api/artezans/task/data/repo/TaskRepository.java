package com.api.artezans.task.data.repo;

import com.api.artezans.task.data.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
