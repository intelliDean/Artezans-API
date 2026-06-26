package com.api.artezans.gateway.user;

public interface UserUtil {

    // Forgot password
    String FORGET_PASSWORD_SUM = "Forgot password";
    String FORGET_PASSWORD_DESC = """
            When a user clicks "forgot password", they are directed here to input their registered email address.
            A confirmation email is then sent to them.  (tested ✅)
            """;
    String FORGET_PASSWORD_OP_ID = "forget.password";

    // Reset password
    String RESET_PASSWORD_SUM = "Reset password";
    String RESET_PASSWORD_DESC = """
            The email sent after "forgot password" contains a link. When clicked, the user is redirected here
            and required to input their new password.  (tested ✅)
            """;
    String RESET_PASSWORD_OP_ID = "reset.password";

    // Verify email
    String VERIFY_SUM = "Verify email address";
    String VERIFY_DESC = """
            After registration, a verification email is sent to the user containing a link.
            Clicking the link redirects the user here to verify their email address.
            After verification, the user proceeds to log in.  (tested ✅)
            """;
    String VERIFY_OP_ID = "verify.email";

    // Deactivate account
    String DEACTIVATE_SUM = "Deactivate account";
    String DEACTIVATE_DESC = "A user can deactivate their account by calling this endpoint.  (tested ✅)";
    String DEACTIVATE_OP_ID = "deactivate.account";

    // Send activation mail
    String ACT_MAIL_SUM = "Send account activation mail";
    String ACT_MAIL_DESC = """
            When a user whose account has been deactivated wants to reactivate it, they call this endpoint
            with their registered email address and a reactivation email is sent to them.  (tested ✅)
            """;
    String ACT_MAIL_OP_ID = "activation.mail";

    // Reactivate account
    String REACTIVATE_SUM = "Reactivate account";
    String REACTIVATE_DESC = """
            The reactivation email contains a link. When clicked, the user is redirected here
            to complete the reactivation of their account.  (tested ✅)
            """;
    String REACTIVATE_OP_ID = "account.reactivated";
}
