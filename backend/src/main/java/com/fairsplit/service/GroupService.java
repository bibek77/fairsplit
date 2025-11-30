package com.fairsplit.service;

import com.fairsplit.dto.GroupRequest;
import com.fairsplit.dto.GroupResponse;
import com.fairsplit.model.Expense;
import com.fairsplit.model.Group;
import com.fairsplit.repository.ExpenseRepository;
import com.fairsplit.repository.GroupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    
    private static final int MAX_GROUPS = 10;
    private static final int MAX_PARTICIPANTS = 10;
    
    @Transactional
    public GroupResponse createGroup(GroupRequest request) {
        // Validate group limit
        long groupCount = groupRepository.count();
        if (groupCount >= MAX_GROUPS) {
            throw new IllegalStateException("Maximum group limit of " + MAX_GROUPS + " reached");
        }
        
        // Validate group name uniqueness (case-insensitive)
        if (groupRepository.findByGroupNameIgnoreCase(request.getGroupName()).isPresent()) {
            throw new IllegalArgumentException("Group name already exists: " + request.getGroupName());
        }
        
        // Validate participant count
        if (request.getParticipants().size() > MAX_PARTICIPANTS) {
            throw new IllegalArgumentException("Maximum " + MAX_PARTICIPANTS + " participants allowed per group");
        }
        
        // Validate participant uniqueness (case-sensitive)
        Set<String> uniqueParticipants = new HashSet<>(request.getParticipants());
        if (uniqueParticipants.size() != request.getParticipants().size()) {
            throw new IllegalArgumentException("Duplicate participant names are not allowed");
        }
        
        // Create and save group
        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setParticipants(request.getParticipants());
        group.prePersist();
        
        Group savedGroup = groupRepository.save(group);
        
        return mapToResponse(savedGroup, 0.0);
    }
    
    public List<GroupResponse> getAllGroups() {
        List<Group> groups = groupRepository.findAll();
        
        return groups.stream()
                .map(group -> {
                    // Calculate total expense for this group
                    List<Expense> expenses = expenseRepository.findByGroupIdOrderByDateDesc(group.getGroupId());
                    double totalExpense = expenses.stream()
                            .mapToDouble(Expense::getAmount)
                            .sum();
                    
                    return mapToResponse(group, totalExpense);
                })
                .collect(Collectors.toList());
    }
    
    public GroupResponse getGroupById(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
        
        // Calculate total expense
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByDateDesc(groupId);
        double totalExpense = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        
        return mapToResponse(group, totalExpense);
    }
    
    @Transactional
    public void deleteGroup(String groupId) {
        // Verify group exists
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found: " + groupId);
        }
        
        // Delete all expenses for this group
        expenseRepository.deleteByGroupId(groupId);
        
        // Delete the group
        groupRepository.deleteById(groupId);
    }
    
    private GroupResponse mapToResponse(Group group, Double totalExpense) {
        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .participants(group.getParticipants())
                .participantCount(group.getParticipants().size())
                .totalExpense(totalExpense)
                .createdAt(group.getCreatedAt())
                .build();
    }
}
