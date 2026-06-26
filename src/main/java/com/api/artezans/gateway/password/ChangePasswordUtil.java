package com.api.artezans.gateway.password;

public interface ChangePasswordUtil {

    // Initiate password change
    String INIT_SUM = "Initiate change of password";
    String INIT_DESC = """
            When a user wants to change their password, they initiate the process by calling this endpoint
            with their old password and new password. A confirmation email is then sent to the user.  (tested ✅)
            """;
    String INIT_OP_ID = "initiate.password";

    // Confirm password change
    String CHANGE_SUM = "Confirm password change";
    String CHANGE_DESC = """
            The email sent after initiating the change of password contains a confirmation link.
            When the user clicks the link, they are redirected here and the
            change is confirmed if the link is valid.  (tested ✅)
            """;
    String CHANGE_OP_ID = "change.password";
}
