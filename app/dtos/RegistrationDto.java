package dtos;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Selim Eren Bekçe on 16.08.2017.
 */
public class RegistrationDto implements Constraints.Validatable<List<ValidationError>> {
    @Constraints.Pattern(value = "^[A-Za-z0-9]+(?:[\\\\._-][A-Za-z0-9]+)*$", message = "Username can contain alphanumerics, dots, hyphens and underscores")
    @Constraints.MaxLength(20)
    @Constraints.MinLength(3)
    private String username;
    private String usernameNormalized;
    @Constraints.Required
    @Constraints.Email
    private String email;
    @Constraints.MaxLength(32)
    @Constraints.MinLength(4)
    @Constraints.Required
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsernameNormalized() {
        return usernameNormalized;
    }

    public void setUsernameNormalized(String usernameNormalized) {
        this.usernameNormalized = usernameNormalized;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationDto that = (RegistrationDto) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(usernameNormalized, that.usernameNormalized) &&
                Objects.equals(email, that.email) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, usernameNormalized, email, password);
    }

    @Override
    public String toString() {
        return "RegistrationDto{" +
                "username='" + username + '\'' +
                ", usernameNormalized='" + usernameNormalized + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    @Override
    public List<ValidationError> validate() {
        return new ArrayList<>();
    }
}
