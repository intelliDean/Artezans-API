package com.api.artezans.password.controller;

import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.password.data.dtos.ChangePasswordRequest;
import com.api.artezans.password.services.ChangePasswordService;
import com.api.artezans.utils.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ChangePasswordController {

    private final ChangePasswordService changePasswordService;

    public ApiResponse initChangeOfPassword(ChangePasswordRequest passwordRequest, SecuredUser securedUser) {
        return changePasswordService.initChangeOfPassword(passwordRequest, securedUser.getUser());
    }

    public ApiResponse changePassword(String token) {
        return changePasswordService.changePassword(token);
    }

}
