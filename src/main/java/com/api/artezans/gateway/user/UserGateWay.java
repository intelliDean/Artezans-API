package com.api.artezans.gateway.user;

import com.api.artezans.password.dtos.EmailParam;
import com.api.artezans.password.dtos.ResetPasswordRequest;
import com.api.artezans.users.controller.UserController;
import com.api.artezans.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.api.artezans.gateway.user.UserUtil.*;


@RestController
@AllArgsConstructor
@Tag(name = "User Controller")
@RequestMapping("api/v1/user")
public class UserGateWay {
    private final UserController userController;

    @PostMapping("/forgot-password")
    @Operation(summary = FORGET_PASSWORD_SUM, description = FORGET_PASSWORD_DESC, operationId = FORGET_PASSWORD_OP_ID)
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody EmailParam request) {
        return ResponseEntity.ok(
                userController.createLinkForPasswordRequest(request)
        );
    }

    @PostMapping("/reset-password")
    @Operation(summary = RESET_PASSWORD_SUM, description = RESET_PASSWORD_DESC, operationId = RESET_PASSWORD_OP_ID)
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestBody ResetPasswordRequest request, @RequestParam("t") String token) {
        return ResponseEntity.ok(userController.resetPassword(request, token));
    }

    @PostMapping("verify")
    @Operation(summary = VERIFY_SUM, description = VERIFY_DESC, operationId = VERIFY_OP_ID)
    public ResponseEntity<ApiResponse> verifyEmailAddress(
            @RequestParam(name = "t") String token, @RequestParam(name = "e") String email) {
        return ResponseEntity.ok(
                userController.verifyUserEmail(token, email)
        );
    }

    @PostMapping("/deactivate")
    @Operation(summary = DEACTIVATE_SUM, description = DEACTIVATE_DESC, operationId = DEACTIVATE_OP_ID)
    public ResponseEntity<ApiResponse> deactivateAccount() {
        return ResponseEntity.ok(
                userController.deactivateAccount()
        );
    }

    @PostMapping("/activation-mail")
    @Operation(summary = ACT_MAIL_SUM, description = ACT_MAIL_DESC, operationId = ACT_MAIL_OP_ID)
    public ResponseEntity<ApiResponse> sendActivationMail(
            @RequestBody EmailParam emailParam, HttpServletRequest request) {
        return ResponseEntity.ok(
                userController.sendActivationMail(emailParam.getEmail(), request)
        );
    }

    @PostMapping("/reactivate")
    @Operation(summary = REACTIVATE_SUM, description = REACTIVATE_DESC, operationId = REACTIVATE_OP_ID)
    public ResponseEntity<ApiResponse> reactivateAccount(
            @RequestParam(name = "e") String email, @RequestParam(name = "t") String token) {
        return ResponseEntity.ok(
                userController.reactivate(email, token)
        );
    }
}