package com.mbstu.campussafety.dto.auth;

import com.mbstu.campussafety.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserDTO user;
}
