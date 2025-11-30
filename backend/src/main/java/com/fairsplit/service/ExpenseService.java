package com.fairsplit.service;

import com.fairsplit.dto.ExpenseRequest;
import com.fairsplit.dto.ExpenseResponse;
import com.fairsplit.model.Expense;
import com.fairsplit.model.Group;
import com.fairsplit.model.SplitType;
import com.fairsplit.repository.ExpenseRepository;
import com.fairsplit.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    
    public ExpenseResponse addExpense(String groupId, ExpenseRequest request) {
        // Validate group exists
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
        
        // Validate amount is positive
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        // Validate date is not in future
        LocalDate expenseDate = request.getDate() != null ? request.getDate() : LocalDate.now();
        if (expenseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Expense date cannot be in the future");
        }
        
        // Validate payer is a participant
        if (!group.getParticipants().contains(request.getPaidBy())) {
            throw new IllegalArgumentException("Payer must be a participant in the group");
        }
        
        // Create expense
        Expense expense = new Expense();
        expense.setGroupId(groupId);
        expense.setDescription(request.getDescription());
        expense.setAmount(roundToTwoDecimals(request.getAmount()));
        expense.setPaidBy(request.getPaidBy());
        expense.setDate(expenseDate);
        
        // Calculate splits
        Map<String, Double> splitDetails;
        if (request.getContributions() != null && !request.getContributions().isEmpty()) {
            // Custom split
            expense.setSplitType(SplitType.CUSTOM);
            expense.setContributions(request.getContributions());
            splitDetails = request.getContributions();
        } else {
            // Equal split
            expense.setSplitType(SplitType.EQUAL);
            splitDetails = calculateEqualSplit(request.getAmount(), group.getParticipants());
            expense.setContributions(splitDetails);
        }
        
        expense.prePersist();
        
        Expense savedExpense = expenseRepository.save(expense);
        
        return mapToResponse(savedExpense, splitDetails);
    }
    
    public List<ExpenseResponse> getExpensesByGroup(String groupId) {
        // Validate group exists
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found: " + groupId);
        }
        
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByDateDesc(groupId);
        
        return expenses.stream()
                .map(expense -> mapToResponse(expense, expense.getContributions()))
                .collect(Collectors.toList());
    }
    
    private Map<String, Double> calculateEqualSplit(Double amount, List<String> participants) {
        BigDecimal totalAmount = BigDecimal.valueOf(amount);
        int participantCount = participants.size();
        
        BigDecimal sharePerPerson = totalAmount.divide(
                BigDecimal.valueOf(participantCount), 
                2, 
                RoundingMode.HALF_UP
        );
        
        Map<String, Double> splits = new HashMap<>();
        BigDecimal totalAllocated = BigDecimal.ZERO;
        
        // Allocate equal shares to all but last participant
        for (int i = 0; i < participantCount - 1; i++) {
            double share = sharePerPerson.doubleValue();
            splits.put(participants.get(i), share);
            totalAllocated = totalAllocated.add(BigDecimal.valueOf(share));
        }
        
        // Last participant gets the remainder to handle rounding
        String lastParticipant = participants.get(participantCount - 1);
        BigDecimal lastShare = totalAmount.subtract(totalAllocated);
        splits.put(lastParticipant, roundToTwoDecimals(lastShare.doubleValue()));
        
        return splits;
    }
    
    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
    
    private ExpenseResponse mapToResponse(Expense expense, Map<String, Double> splitDetails) {
        return ExpenseResponse.builder()
                .expenseId(expense.getExpenseId())
                .groupId(expense.getGroupId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .paidBy(expense.getPaidBy())
                .date(expense.getDate())
                .contributions(expense.getContributions())
                .splitDetails(splitDetails)
                .splitType(expense.getSplitType())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
