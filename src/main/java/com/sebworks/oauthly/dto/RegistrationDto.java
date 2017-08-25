package com.sebworks.oauthly.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Created by Selim Eren Bekçe on 16.08.2017.
 */
@Data
public class RegistrationDto {
    @Size(min=3, max=20)
    @Pattern(regexp = "^[A-Za-z0-9]+(?:[\\\\._-][A-Za-z0-9]+)*$", message = "Username can contain alphanumerics, dots, hyphens and underscores")
    private String username;
    private String usernameNormalized;
    private String email;
    private String password;
    private String passwordConfirm;
}
