package com.api.artezans.payment.paypal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {

    private String serviceName;

    private Double total;

    private String firstName;

    private String lastName;

    private String email;


    //FLUENT INTERFACE DESIGN PATTERN
    public OrderDetail setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public OrderDetail setTotal(Double total) {
        this.total = total;
        return this;
    }

    public OrderDetail setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public OrderDetail setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public OrderDetail setEmail(String email) {
        this.email = email;
        return this;
    }
}
