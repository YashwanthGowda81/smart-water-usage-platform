package com.smartwater.dto;

import com.smartwater.entity.Role;
import com.smartwater.entity.UserStatus;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
    private OffsetDateTime createdAt;
}
