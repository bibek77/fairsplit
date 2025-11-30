package com.fairsplit.repository;

import com.fairsplit.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    
    Optional<Group> findByGroupNameIgnoreCase(String groupName);
}
