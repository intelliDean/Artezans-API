package com.api.artezans.gateway.password;

import com.api.artezans.config.annotation.CurrentUser;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.password.controller.ChangePasswordController;
import com.api.artezans.password.data.dtos.ChangePasswordRequest;
import com.api.artezans.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.api.artezans.gateway.password.ChangePasswordUtil.*;

@RestController
@AllArgsConstructor
@Tag(name = "Change Password Controller")
@RequestMapping("api/v1/change-password")
public class ChangePasswordGateway {
    private final ChangePasswordController changePasswordController;

    @PostMapping("init")
    @Operation(summary = INIT_SUM, description = INIT_DESC, operationId = INIT_OP_ID)
    public ResponseEntity<ApiResponse> initChangeOfPassword(
            @RequestBody ChangePasswordRequest request, @CurrentUser SecuredUser securedUser) {
        return ResponseEntity.ok(changePasswordController.initChangeOfPassword(request, securedUser));
    }

    @PostMapping("change")
    @Operation(summary = CHANGE_SUM, description = CHANGE_DESC, operationId = CHANGE_OP_ID)
    public ResponseEntity<ApiResponse> changePassword(@RequestParam(name = "t") String token) {
        return ResponseEntity.ok(changePasswordController.changePassword(token));
    }
}
