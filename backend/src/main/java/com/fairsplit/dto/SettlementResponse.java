package com.fairsplit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementResponse {
    
    private List<Settlement> settlements;
    private Map<String, MemberBalance> memberBalances;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Settlement {
        private String from;
        private String to;
        private Double amount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberBalance {
        private Double totalPaid;
        private Double totalOwed;
        private Double netBalance;
    }
}
