package com.smartwater.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    private Long apartmentId;
    private Long householdId;
}
