package com.api.artezans.gateway.user;

public class UserUtil {

    public final static String FORGET_PASSWORD_SUM = "Forget Password";
    public final static String FORGET_PASSWORD_DESC = "When a user click on forgot password, it brings him here. The user input his " +
            "registered email. A mail is sent to him confirm his action.  (tested✅)";
    public final static String FORGET_PASSWORD_OP_ID = "forget.password";
    public final static String RESET_PASSWORD_SUM = "Reset Password";
    public final static String RESET_PASSWORD_DESC = "The mail sent to user when forgot password was initiated contains a link, " +
            "when the link is click it redirect uer to this endpoint where uer will be required " +
            "to input his new password.  (tested✅)";
    public final static String RESET_PASSWORD_OP_ID = "reset.password";
    public final static String VERIFY_SUM = "User verifies email";
    public final static String VERIFY_DESC = "After registration, a mail was sent to the user to verify his email that contains a link." +
            "The link in the user email redirect the user here to verify their email address. After being verified," +
            "the user is required to proceed to login. (tested✅)";
    public final static String VERIFY_OP_ID = "verify.email";
    public final static String DEACTIVATE_SUM = "User deactivate account";
    public final static String DEACTIVATE_DESC = "User can decide to deactivate their account by calling this endpoint. (tested✅)";
    public final static String DEACTIVATE_OP_ID = "deactivate.account";
    public final static String ACT_MAIL_SUM = "Activation mail is sent";
    public final static String ACT_MAIL_DESC = "When a user whose account has been deactivated decided to reactivate his " +
            "account he calls this endpoint with his registered email address and a reactivation " +
            "mail will be sent to him. (tested✅)";
    public final static String ACT_MAIL_OP_ID = "activation.mail";
    public final static String REACTIVATE_SUM = "Account reactivated";
    public final static String REACTIVATE_DESC = "The reactivation mail sent to user contain a link. When the link is clicked it redirects" +
            "the user to this endpoint to reactivate his account. (tested✅)";
    public final static String REACTIVATE_OP_ID = "account.reactivated";


}
