package com.fairsplit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {
    
    private String groupId;
    private String groupName;
    private List<String> participants;
    private Integer participantCount;
    private Double totalExpense;
    private LocalDateTime createdAt;
}
