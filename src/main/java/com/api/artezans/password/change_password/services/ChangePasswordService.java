package com.api.artezans.password.change_password.services;


import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.password.change_password.dtos.ChangePasswordRequest;
import com.api.artezans.utils.ApiResponse;

public interface ChangePasswordService {

    ApiResponse completePasswordChange(String token);
    ApiResponse initChangeOfPassword(ChangePasswordRequest passwordRequest, SecuredUser currentUser);

}
