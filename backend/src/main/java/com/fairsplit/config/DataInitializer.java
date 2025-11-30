package com.fairsplit.config;

import com.fairsplit.model.Expense;
import com.fairsplit.model.Group;
import com.fairsplit.model.SplitType;
import com.fairsplit.repository.ExpenseRepository;
import com.fairsplit.repository.GroupRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    
    @PostConstruct
    public void init() {
        log.info("Initializing sample data...");
        
        // Create sample groups
        Group group1 = createGroup("Weekend Trip", Arrays.asList("Alice", "Bob", "Charlie"));
        Group group2 = createGroup("Office Lunch", Arrays.asList("David", "Emma", "Frank", "Grace"));
        Group group3 = createGroup("Apartment Expenses", Arrays.asList("Henry", "Iris"));
        
        // Add expenses to group 1
        addExpense(group1.getGroupId(), "Hotel Booking", 300.00, "Alice", 
                LocalDate.now().minusDays(5), group1.getParticipants());
        addExpense(group1.getGroupId(), "Dinner", 90.00, "Bob", 
                LocalDate.now().minusDays(4), group1.getParticipants());
        addExpense(group1.getGroupId(), "Gas", 45.00, "Charlie", 
                LocalDate.now().minusDays(3), group1.getParticipants());
        addExpense(group1.getGroupId(), "Breakfast", 36.00, "Alice", 
                LocalDate.now().minusDays(2), group1.getParticipants());
        
        // Add expenses to group 2
        addExpense(group2.getGroupId(), "Pizza Lunch", 80.00, "David", 
                LocalDate.now().minusDays(7), group2.getParticipants());
        addExpense(group2.getGroupId(), "Coffee", 24.00, "Emma", 
                LocalDate.now().minusDays(6), group2.getParticipants());
        addExpense(group2.getGroupId(), "Team Dinner", 160.00, "Frank", 
                LocalDate.now().minusDays(3), group2.getParticipants());
        
        // Add expenses to group 3
        addExpense(group3.getGroupId(), "Rent", 2000.00, "Henry", 
                LocalDate.now().minusDays(10), group3.getParticipants());
        addExpense(group3.getGroupId(), "Utilities", 150.00, "Iris", 
                LocalDate.now().minusDays(8), group3.getParticipants());
        addExpense(group3.getGroupId(), "Groceries", 120.00, "Henry", 
                LocalDate.now().minusDays(2), group3.getParticipants());
        
        log.info("Sample data initialization complete!");
        log.info("Created {} groups with {} total expenses", 
                groupRepository.count(), expenseRepository.count());
    }
    
    private Group createGroup(String name, List<String> participants) {
        Group group = new Group();
        group.setGroupName(name);
        group.setParticipants(participants);
        group.prePersist();
        return groupRepository.save(group);
    }
    
    private void addExpense(String groupId, String description, Double amount, 
                           String paidBy, LocalDate date, List<String> participants) {
        Expense expense = new Expense();
        expense.setGroupId(groupId);
        expense.setDescription(description);
        expense.setAmount(roundToTwoDecimals(amount));
        expense.setPaidBy(paidBy);
        expense.setDate(date);
        expense.setSplitType(SplitType.EQUAL);
        
        // Calculate equal split
        Map<String, Double> contributions = calculateEqualSplit(amount, participants);
        expense.setContributions(contributions);
        
        expense.prePersist();
        expenseRepository.save(expense);
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
}
