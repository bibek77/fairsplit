package com.fairsplit.controller;

import com.fairsplit.dto.ExpenseRequest;
import com.fairsplit.dto.ExpenseResponse;
import com.fairsplit.dto.SettlementResponse;
import com.fairsplit.service.ExpenseService;
import com.fairsplit.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
@RequiredArgsConstructor
public class ExpenseController {
    
    private final ExpenseService expenseService;
    private final SettlementService settlementService;
    
    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponse> addExpense(
            @PathVariable String groupId,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.addExpense(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseResponse>> getExpenses(@PathVariable String groupId) {
        List<ExpenseResponse> expenses = expenseService.getExpensesByGroup(groupId);
        return ResponseEntity.ok(expenses);
    }
    
    @GetMapping("/settlements")
    public ResponseEntity<SettlementResponse> getSettlements(@PathVariable String groupId) {
        SettlementResponse response = settlementService.calculateSettlements(groupId);
        return ResponseEntity.ok(response);
    }
}
