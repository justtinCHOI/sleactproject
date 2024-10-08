package com.unitekndt.mqnavigator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequest {
    private String email;
    private String nickname;
    private String password;
}