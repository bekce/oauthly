package dtos;

import play.data.validation.Constraints;

public class ClientDto {
    public String id;
    @Constraints.Required
    @Constraints.MinLength(3)
    @Constraints.MaxLength(30)
    public String name;
    @Constraints.Required
    public String redirectUri;
}
