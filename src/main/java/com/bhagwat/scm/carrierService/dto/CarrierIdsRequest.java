package com.bhagwat.scm.carrierService.dto;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CarrierIdsRequest {
    private List<String> carrierIds;
}
