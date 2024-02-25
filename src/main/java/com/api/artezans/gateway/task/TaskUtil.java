package com.api.artezans.gateway.task;

public class TaskUtil {

    public final static String POST_TASK_DESC = "Both customer and service provider can post a task in marketplace" +
            " and notifications will be sent to the appropriate service providers offering " +
            "such task in their listing.  (tested✅)";
    public final static String POST_TASK_SUM = "Post a task";
    public final static String POST_TASK_OP_ID = "post.task";
    public final static String ACTIVE_TASK_DESC = "To find posts that are still active, this is the endpoint to call  (tested✅)";
    public final static String ACTIVE_TASK_SUM = "Find active tasks";
    public final static String ACTIVE_TASK_OP_ID = "active.task";
    public final static String ADMIN_TASK_DESC = "Admin can view all tasks, including the deleted ones using this endpoint";
    public final static String ADMIN_TASK_SUM = "Admin view all tasks";
    public final static String ADMIN_TASK_OP_ID = "admin.view.task";
    public final static String DELETE_TASK_DESC = "User can delete post using this endpoint";
    public final static String DELETE_TASK_SUM = "Delete task";
    public final static String DELETE_TASK_OP_ID = "delete.task";

}
