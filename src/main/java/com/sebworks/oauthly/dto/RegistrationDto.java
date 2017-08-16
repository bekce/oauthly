package com.sebworks.oauthly.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * Created by Selim Eren Bekçe on 16.08.2017.
 */
@Data
public class RegistrationDto {
    @Size(min=4, max=20)
    private String username;
    private String email;
    private String password;
    private String passwordConfirm;
}
