package com.api.artezans.password.services;


import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.password.data.dtos.ChangePasswordRequest;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;

public interface ChangePasswordService {

    ApiResponse changePassword(String token);
    ApiResponse initChangeOfPassword(ChangePasswordRequest passwordRequest, SecuredUser currentUser);

}
