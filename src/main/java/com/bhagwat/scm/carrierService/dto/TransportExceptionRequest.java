package com.bhagwat.scm.carrierService.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransportExceptionRequest {
    @NotBlank private String exceptionType;
    private String consignmentId;
    @NotBlank private String notes;
    private List<String> photoUris;
    private Double latitude;
    private Double longitude;
}
