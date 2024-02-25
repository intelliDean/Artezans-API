package com.api.artezans.tokens.service.interfaces;


import com.api.artezans.tokens.model.TaskHubVerificationToken;

public interface TaskHubVerificationTokenService {

    void saveToken(TaskHubVerificationToken taskHubVerificationToken);
    boolean isValid(TaskHubVerificationToken taskHubVerificationToken);

    void deleteToken(TaskHubVerificationToken verificationToken);
    TaskHubVerificationToken findByTokenAndEmail(String token, String email);
    TaskHubVerificationToken findByToken(String token);
    TaskHubVerificationToken findByEmail(String email);
}
