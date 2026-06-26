package com.api.artezans.gateway.task;

public interface TaskUtil {

    // Post a task
    String POST_TASK_SUM = "Post a task";
    String POST_TASK_DESC = """
            Both customers and service providers can post a task to the marketplace.
            Notifications will be sent to service providers whose listings match the task.  (tested ✅)
            """;
    String POST_TASK_OP_ID = "post.task";

    // Active tasks
    String ACTIVE_TASK_SUM = "Find active tasks";
    String ACTIVE_TASK_DESC = "To find posts that are still active, call this endpoint.  (tested ✅)";
    String ACTIVE_TASK_OP_ID = "active.task";

    // Admin view all tasks
    String ADMIN_TASK_SUM = "Admin views all tasks";
    String ADMIN_TASK_DESC = "Admin can view all tasks — including deleted ones — using this endpoint.";
    String ADMIN_TASK_OP_ID = "admin.view.task";

    // Delete task
    String DELETE_TASK_SUM = "Delete task";
    String DELETE_TASK_DESC = "A user can delete their own task post using this endpoint.";
    String DELETE_TASK_OP_ID = "delete.task";
}
