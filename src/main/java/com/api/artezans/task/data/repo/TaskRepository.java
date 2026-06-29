package com.api.artezans.task.data.repo;

import com.api.artezans.task.data.model.Task;
import com.api.artezans.users.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByTaskServiceNameIgnoreCase(String serviceName);

    List<Task> findAllByPoster(User poster);

    @Query(value = """
            select task from Task task
            where task.isActive = true
            """)
    List<Task> findAllUndeletedLists();

    List<Task> findAllByIsActiveTrue(); // replaces findAllUndeletedLists
}