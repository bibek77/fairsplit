package com.fairsplit.repository;

import com.fairsplit.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    
    List<Expense> findByGroupIdOrderByDateDesc(String groupId);
    
    void deleteByGroupId(String groupId);
}
