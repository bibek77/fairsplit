package com.fairsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    
    @Id
    private String expenseId;
    
    @Column(nullable = false)
    private String groupId;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(nullable = false)
    private String paidBy;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expense_contributions", joinColumns = @JoinColumn(name = "expense_id"))
    @MapKeyColumn(name = "participant")
    @Column(name = "amount")
    private Map<String, Double> contributions = new HashMap<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SplitType splitType;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        if (expenseId == null) {
            expenseId = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (date == null) {
            date = LocalDate.now();
        }
    }
}
