package com.society.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisitorEntryDTO {

    private String visitorName;
    private String phone;

    private String purpose;       // STRING
    private boolean hasVehicle;

    private String vehicleType;   // STRING
    private String vehicleNumber;
}
