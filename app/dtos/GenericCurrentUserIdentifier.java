package dtos;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class GenericCurrentUserIdentifier implements Function<OAuthContext, CompletionStage<MeDto>> {

    private String emailFieldName = "email";
    private String idFieldName = "id";
    private String nameFieldName = "name";

    @Override
    public CompletionStage<MeDto> apply(OAuthContext context) {
        return context.getWs()
                .url(context.getProvider().getUserInfoUrl())
                .addHeader("Authorization", "Bearer "+ context.getToken().getAccessToken())
                .get()
                .thenApplyAsync(wsResponse -> {
                    JsonNode node = wsResponse.asJson();
                    String id = node.get(idFieldName).asText();
                    String name = node.get(nameFieldName).textValue();
                    String email = node.get(emailFieldName) == null ? null : node.get(emailFieldName).textValue();
                    return new MeDto(id, name, email);
                });
    }

    public String getEmailFieldName() {
        return emailFieldName;
    }

    public void setEmailFieldName(String emailFieldName) {
        this.emailFieldName = emailFieldName;
    }

    public String getIdFieldName() {
        return idFieldName;
    }

    public void setIdFieldName(String idFieldName) {
        this.idFieldName = idFieldName;
    }

    public String getNameFieldName() {
        return nameFieldName;
    }

    public void setNameFieldName(String nameFieldName) {
        this.nameFieldName = nameFieldName;
    }
}
