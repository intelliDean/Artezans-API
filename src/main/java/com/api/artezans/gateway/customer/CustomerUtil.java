package com.api.artezans.gateway.customer;

public class CustomerUtil {
    public final static String REGISTER_DESC = "When a customer wants to register on the platform, he calls this endpoint with required" +
            " credentials. If the registration is successful, a verification mail is sent to the user";
    public final static String REGISTER_SUM = "Register customer";
    public final static String REGISTER_OP_ID = "register.customer";
    public final static String COMP_REG_DESC = "After a customer is registered, verified their email and successfully logged in," +
            "the user must complete their registration by calling this endpoint and providing all details." +
            "Note: access token from login must be provided to successfully call this endpoint";
    public final static String COMP_REG_SUM = "Customer complete registration";
    public final static String COMP_REG_OP_ID = "customer.complete.reg";
    public final static String PROF_PIC_DESC = "When a customer want to update his profile picture, " +
            "he does that by calling this endpoint with his image file";
    public final static String PROF_PIC_SUM = "Customer uploads profile picture";
    public final static String PROF_PIC_OP_ID = "customer.profile.picture";
    public final static String UPDATE_DESC = "When a user wants to update a particular field, he does that calling this endpoint." +
            "Note: the payload for this endpoint is quite unique";
    public final static String UPDATE_SUM = "Customer updates profile";
    public final static String UPDATE_OP_ID = "customer.update";

}
