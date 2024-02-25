package com.api.artezans.gateway.password;

public class ChangePasswordUtil {
    public final static String INIT_DESC = "When a user wants to change his password, he initiates the process by " +
            "first calling this endpoint with his old password and new password. This will send the user an email" +
            " to confirm the change of password.  (tested✅)";
    public final static String INIT_SUM = "Initiate change of password";
    public final static String INIT_OP_ID = "initiate.password";
    public final static String CHANGE_DESC = "The email sent to the user after initiating the change of password process " +
            "contains a link, when the user clicks on the link, the link redirects the user to this " +
            "endpoint, and confirms the change of password if the link is valid.  (tested✅)";
    public final static String CHANGE_SUM = "change password";
    public final static String CHANGE_OP_ID = "change.password";

}
