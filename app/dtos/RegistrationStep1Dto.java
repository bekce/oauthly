package dtos;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

public class RegistrationStep1Dto implements Constraints.Validatable<ValidationError> {
    @Constraints.Email
    private String email;
    @Constraints.Required
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public ValidationError validate() {
        return null;
    }
}
