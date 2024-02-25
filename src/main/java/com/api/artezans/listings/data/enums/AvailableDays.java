package com.api.artezans.listings.data.enums;

import lombok.Getter;

@Getter
public enum AvailableDays {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");


    AvailableDays(String day) {}

}
