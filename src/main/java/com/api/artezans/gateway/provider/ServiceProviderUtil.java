package com.api.artezans.gateway.provider;

public interface ServiceProviderUtil {

    // Register
    String REGISTER_SUM = "Service provider registration";
    String REGISTER_DESC = """
            When a service provider wants to register on the platform, they call this endpoint with the required
            credentials. If registration is successful, a verification email is sent to the user.
            """;
    String REGISTER_OP_ID = "register.service.provider";

    // Complete registration
    String COMP_REG_SUM = "Service provider completes registration";
    String COMP_REG_DESC = """
            After a service provider has registered, verified their email, and successfully logged in,
            they must complete their registration by calling this endpoint and providing all required details.
            Note: the access token from login must be included to call this endpoint.
            """;
    String COMP_REG_OP_ID = "complete.registration";

    // Profile picture
    String PROF_PIC_SUM = "Service provider uploads profile picture";
    String PROF_PIC_DESC = """
            When a service provider wants to update their profile picture,
            they do so by calling this endpoint with their image file.""";
    String PROF_PIC_OP_ID = "profile.picture";

    // Update profile
    String UPDATE_SUM = "Service provider updates profile";
    String UPDATE_DESC = """
            When a service provider wants to update a particular field in their profile, they call this endpoint.
            Note: the payload for this endpoint is unique — refer to the schema for details.
            """;
    String UPDATE_OP_ID = "update.profile";

    // View relevant tasks
    String TASK_SUM = "Service provider views relevant tasks";
    String TASK_DESC = """
            A service provider can only see tasks that match their posted listings.
            For example, a provider with a cleaning listing will only see tasks posted for cleaning.
            """;
    String TASK_OP_ID = "peculiar.tasks";

    // Notifications
    String NOTIFICATION_SUM = "Service provider notifications";
    String NOTIFICATION_DESC = "Service provider views all notifications sent to them.";
    String NOTIFICATION_OP_ID = "sp.notifications";
}
