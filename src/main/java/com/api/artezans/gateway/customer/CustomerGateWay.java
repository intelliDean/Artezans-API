package com.api.artezans.gateway.customer;

import com.api.artezans.config.annotation.CurrentUser;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.customer.controller.CustomerController;
import com.api.artezans.customer.data.dto.request.CustomerRegistrationRequest;
import com.api.artezans.customer.data.dto.request.CustomerUpdateRequest;
import com.api.artezans.utils.ApiResponse;
import com.github.fge.jsonpatch.JsonPatch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.api.artezans.gateway.customer.CustomerUtil.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Customer Controller")
@RequestMapping("/api/v1/customer")
public class CustomerGateWay {
    private final CustomerController customerController;

    @PostMapping("/sign-up")
    @Operation(summary = REGISTER_SUM, description = REGISTER_DESC, operationId = REGISTER_OP_ID)
    public ResponseEntity<ApiResponse> register(
            @RequestBody @Valid final CustomerRegistrationRequest registrationRequest) {
        return new ResponseEntity<>(customerController.register(registrationRequest), HttpStatus.CREATED);
    }

    @PostMapping(value = "/complete")
    @Operation(summary = COMP_REG_SUM, description = COMP_REG_DESC, operationId = COMP_REG_OP_ID)
    public ResponseEntity<ApiResponse> completeCustomerRegistration(
            String token, @RequestBody @Valid CustomerUpdateRequest updateRequest) {
        return ResponseEntity.ok(customerController.updateCustomerProfile(token, updateRequest));
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PostMapping(value = "profile_picture", consumes = MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = PROF_PIC_SUM, description = PROF_PIC_DESC, operationId = PROF_PIC_OP_ID)
    public ResponseEntity<ApiResponse> uploadProfileImage(@RequestBody MultipartFile image) {
        return ResponseEntity.ok(customerController.uploadProfileImage(image));
    }

    @PatchMapping("update")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = UPDATE_SUM, description = UPDATE_DESC, operationId = UPDATE_OP_ID)
    public ResponseEntity<ApiResponse> updateCustomerProfile(
            @RequestBody JsonPatch updatePayload, @CurrentUser SecuredUser securedUser) {
        return ResponseEntity.ok(customerController.updateCustomerInfo(updatePayload, securedUser));
    }
}