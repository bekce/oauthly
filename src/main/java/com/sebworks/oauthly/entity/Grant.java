package com.sebworks.oauthly.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a grant made by user to client
 * Created by Selim Eren Bekçe on 15.08.2017.
 */
@Data
public class Grant {
    @Id
    private String id;
    private String userId;
    private String clientId;
    private List<String> scopes = new ArrayList<>();
}
