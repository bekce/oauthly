package com.sebworks.oauthly.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
public class DiscourseDto {
    @Id
    private String id;
    private boolean enabled;
    private String redirectUri;
    private String secret;
}
