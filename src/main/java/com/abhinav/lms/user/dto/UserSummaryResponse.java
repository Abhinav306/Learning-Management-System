package com.abhinav.lms.user.dto;

import com.abhinav.lms.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private String profileImageUrl;
}
