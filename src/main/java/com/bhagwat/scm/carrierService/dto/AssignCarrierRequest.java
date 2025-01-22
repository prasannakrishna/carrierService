package com.bhagwat.scm.carrierService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignCarrierRequest {

    private String shipmentId;
    private String carrierId;
    private String status;

    // Getters and setters

}

