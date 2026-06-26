package com.api.artezans.gateway.customer;

public interface CustomerUtil {

    // Register
    String REGISTER_SUM = "Register customer";
    String REGISTER_DESC = """
            When a customer wants to register on the platform, they call this endpoint with the required
            credentials. If registration is successful, a verification email is sent to the user.
            """;
    String REGISTER_OP_ID = "register.customer";

    // Complete registration
    String COMP_REG_SUM = "Customer completes registration";
    String COMP_REG_DESC = """
            After a customer has registered, verified their email, and successfully logged in,
            they must complete their registration by calling this endpoint and providing all required details.
            Note: the access token from login must be included to call this endpoint.
            """;
    String COMP_REG_OP_ID = "customer.complete.reg";

    // Profile picture
    String PROF_PIC_SUM = "Customer uploads profile picture";
    String PROF_PIC_DESC = """
            When a customer wants to update their profile picture,
            they do so by calling this endpoint with their image file.""";
    String PROF_PIC_OP_ID = "customer.profile.picture";

    // Update profile
    String UPDATE_SUM = "Customer updates profile";
    String UPDATE_DESC = """
            When a customer wants to update a particular field in their profile, they call this endpoint.
            Note: the payload for this endpoint is unique — refer to the schema for details.
            """;
    String UPDATE_OP_ID = "customer.update";
}
