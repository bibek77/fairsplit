package com.fairsplit.dto;

import com.fairsplit.model.SplitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    
    private String expenseId;
    private String groupId;
    private String description;
    private Double amount;
    private String paidBy;
    private LocalDate date;
    private Map<String, Double> contributions;
    private Map<String, Double> splitDetails;
    private SplitType splitType;
    private LocalDateTime createdAt;
}
