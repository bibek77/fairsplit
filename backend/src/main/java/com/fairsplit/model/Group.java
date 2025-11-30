package com.fairsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    
    @Id
    private String groupId;
    
    @Column(unique = true, nullable = false)
    private String groupName;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "group_participants", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "participant_name")
    private List<String> participants = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Transient
    private Double totalExpense;
    
    @PrePersist
    public void prePersist() {
        if (groupId == null) {
            groupId = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
