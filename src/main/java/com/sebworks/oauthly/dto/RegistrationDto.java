package com.sebworks.oauthly.dto;

import lombok.Data;

/**
 * Created by Selim Eren Bekçe on 16.08.2017.
 */
@Data
public class RegistrationDto {
    private String username;
    private String email;
    private String password;
    private String passwordConfirm;
}
