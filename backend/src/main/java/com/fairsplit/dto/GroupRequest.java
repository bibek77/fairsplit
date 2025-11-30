package com.fairsplit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupRequest {
    
    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String groupName;
    
    @NotNull(message = "Participants list is required")
    @Size(min = 1, max = 10, message = "Group must have between 1 and 10 participants")
    private List<@NotBlank(message = "Participant name cannot be blank") String> participants;
}
