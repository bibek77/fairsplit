package com.fairsplit.service;

import com.fairsplit.dto.SettlementResponse;
import com.fairsplit.model.Expense;
import com.fairsplit.repository.ExpenseRepository;
import com.fairsplit.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {
    
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    
    public SettlementResponse calculateSettlements(String groupId) {
        // Validate group exists
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found: " + groupId);
        }
        
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByDateDesc(groupId);
        
        // Calculate net balances
        Map<String, BigDecimal> netBalances = calculateNetBalances(expenses);
        
        // Calculate member balances for response
        Map<String, SettlementResponse.MemberBalance> memberBalances = getMemberBalances(expenses);
        
        // Optimize transactions
        List<SettlementResponse.Settlement> settlements = optimizeTransactions(netBalances);
        
        return SettlementResponse.builder()
                .settlements(settlements)
                .memberBalances(memberBalances)
                .build();
    }
    
    private Map<String, BigDecimal> calculateNetBalances(List<Expense> expenses) {
        Map<String, BigDecimal> netBalances = new HashMap<>();
        
        for (Expense expense : expenses) {
            String payer = expense.getPaidBy();
            BigDecimal amount = BigDecimal.valueOf(expense.getAmount());
            
            // Add amount paid by payer
            netBalances.merge(payer, amount, BigDecimal::add);
            
            // Subtract contributions from each participant
            Map<String, Double> contributions = expense.getContributions();
            for (Map.Entry<String, Double> entry : contributions.entrySet()) {
                String participant = entry.getKey();
                BigDecimal contribution = BigDecimal.valueOf(entry.getValue());
                netBalances.merge(participant, contribution.negate(), BigDecimal::add);
            }
        }
        
        // Round all balances to 2 decimal places
        return netBalances.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().setScale(2, RoundingMode.HALF_UP)
                ));
    }
    
    private List<SettlementResponse.Settlement> optimizeTransactions(Map<String, BigDecimal> netBalances) {
        List<SettlementResponse.Settlement> settlements = new ArrayList<>();
        
        // Separate creditors (positive balance) and debtors (negative balance)
        List<Map.Entry<String, BigDecimal>> creditors = netBalances.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Sort descending
                .collect(Collectors.toList());
        
        List<Map.Entry<String, BigDecimal>> debtors = netBalances.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(e -> e.getValue())) // Sort ascending (most negative first)
                .collect(Collectors.toList());
        
        // Create mutable copies
        Map<String, BigDecimal> creditorBalances = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : creditors) {
            creditorBalances.put(entry.getKey(), entry.getValue());
        }
        
        Map<String, BigDecimal> debtorBalances = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : debtors) {
            debtorBalances.put(entry.getKey(), entry.getValue());
        }
        
        // Greedy algorithm: match largest creditor with largest debtor
        while (!creditorBalances.isEmpty() && !debtorBalances.isEmpty()) {
            // Get largest creditor
            Map.Entry<String, BigDecimal> maxCreditor = creditorBalances.entrySet().stream()
                    .max(Comparator.comparing(Map.Entry::getValue))
                    .orElse(null);
            
            // Get largest debtor (most negative)
            Map.Entry<String, BigDecimal> maxDebtor = debtorBalances.entrySet().stream()
                    .min(Comparator.comparing(Map.Entry::getValue))
                    .orElse(null);
            
            if (maxCreditor == null || maxDebtor == null) {
                break;
            }
            
            String creditor = maxCreditor.getKey();
            String debtor = maxDebtor.getKey();
            BigDecimal creditorAmount = maxCreditor.getValue();
            BigDecimal debtorAmount = maxDebtor.getValue().abs();
            
            // Transfer minimum of the two amounts
            BigDecimal transferAmount = creditorAmount.min(debtorAmount);
            
            // Create settlement
            settlements.add(SettlementResponse.Settlement.builder()
                    .from(debtor)
                    .to(creditor)
                    .amount(roundToTwoDecimals(transferAmount.doubleValue()))
                    .build());
            
            // Update balances
            BigDecimal newCreditorBalance = creditorAmount.subtract(transferAmount);
            BigDecimal newDebtorBalance = debtorAmount.subtract(transferAmount);
            
            // Remove or update creditor
            if (newCreditorBalance.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                creditorBalances.remove(creditor);
            } else {
                creditorBalances.put(creditor, newCreditorBalance);
            }
            
            // Remove or update debtor
            if (newDebtorBalance.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                debtorBalances.remove(debtor);
            } else {
                debtorBalances.put(debtor, newDebtorBalance.negate());
            }
        }
        
        return settlements;
    }
    
    private Map<String, SettlementResponse.MemberBalance> getMemberBalances(List<Expense> expenses) {
        Map<String, BigDecimal> totalPaid = new HashMap<>();
        Map<String, BigDecimal> totalOwed = new HashMap<>();
        
        for (Expense expense : expenses) {
            String payer = expense.getPaidBy();
            BigDecimal amount = BigDecimal.valueOf(expense.getAmount());
            
            // Track total paid
            totalPaid.merge(payer, amount, BigDecimal::add);
            
            // Track total owed (contributions)
            Map<String, Double> contributions = expense.getContributions();
            for (Map.Entry<String, Double> entry : contributions.entrySet()) {
                String participant = entry.getKey();
                BigDecimal contribution = BigDecimal.valueOf(entry.getValue());
                totalOwed.merge(participant, contribution, BigDecimal::add);
            }
        }
        
        // Get all participants
        Set<String> allParticipants = new HashSet<>();
        allParticipants.addAll(totalPaid.keySet());
        allParticipants.addAll(totalOwed.keySet());
        
        // Build member balances
        Map<String, SettlementResponse.MemberBalance> memberBalances = new HashMap<>();
        for (String participant : allParticipants) {
            BigDecimal paid = totalPaid.getOrDefault(participant, BigDecimal.ZERO);
            BigDecimal owed = totalOwed.getOrDefault(participant, BigDecimal.ZERO);
            BigDecimal netBalance = paid.subtract(owed);
            
            memberBalances.put(participant, SettlementResponse.MemberBalance.builder()
                    .totalPaid(roundToTwoDecimals(paid.doubleValue()))
                    .totalOwed(roundToTwoDecimals(owed.doubleValue()))
                    .netBalance(roundToTwoDecimals(netBalance.doubleValue()))
                    .build());
        }
        
        return memberBalances;
    }
    
    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
