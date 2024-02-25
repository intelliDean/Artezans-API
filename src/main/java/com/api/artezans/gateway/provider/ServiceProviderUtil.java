package com.api.artezans.gateway.provider;

public class ServiceProviderUtil {

    public final static String REGISTER_SUM = "Service Provider registration";
    public final static String REGISTER_DESC = "When a service provider wants to register on the platform, he calls this endpoint " +
            "with required credentials. If the registration is successful, a verification mail is sent to the user";
    public final static String REGISTER_OP_ID = "register.service.provider";
    public final static String COMP_REG_SUM = "Service Provider completes registration";
    public final static String COMP_REG_DESC =  "After a service provider is registered, verified their email and successfully logged in," +
            "he must complete his registration by calling this endpoint and providing all details." +
            "Note: access token from login must be provided to successfully call this endpoint";
    public final static String COMP_REG_OP_ID = "complete.registration";
    public final static String PROF_PIC_SUM = "Service Provider uploads profile picture";
    public final static String PROF_PIC_DESC =  "When a service provider want to update his profile picture, " +
            "he does that by calling this endpoint with his image file";
    public final static String PROF_PIC_OP_ID = "profile.picture";
    public final static String UPDATE_SUM = "Service Provider updates profile";
    public final static String UPDATE_DESC =  "When a service provider wants to update a particular field, he does " +
            "that calling this endpoint. Note: the payload for this endpoint is quite unique";
    public final static String UPDATE_OP_ID = "update.profile";
    public final static String TASK_SUM = "Service Provider sees only peculiar tasks";
    public final static String TASK_DESC =  "A service provider can only see the tasks that resonate with his posted listing. " +
            "A service provider that has a listing of cleaning will see all posted tasks of cleaning";
    public final static String TASK_OP_ID = "peculiar.tasks";
    public final static String NOTIFICATION_SUM = "Service Provider notifications";
    public final static String NOTIFICATION_DESC =  "Service provider views all of his own notifications sent to him";
    public final static String NOTIFICATION_OP_ID = "sp.notifications";

}
