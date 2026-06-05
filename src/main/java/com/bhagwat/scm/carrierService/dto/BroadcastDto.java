package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.BroadcastStatus;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BroadcastDto {
    private String broadcastId;
    private String carrierId;
    private String carrierName;
    private LocalDateTime sentAt;
    private BroadcastStatus status;
}
