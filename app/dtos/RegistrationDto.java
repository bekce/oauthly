package dtos;

import config.*;
import dtos.ConstraintGroups.*;
import models.User;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Selim Eren Bekçe on 16.08.2017.
 */
@ValidateUniqueUsername(groups = {Register1.class, Register2.class, Register3.class})
@ValidateUniqueEmail(groups = {Register1.class, Register2.class})
public class RegistrationDto implements Constraints.Validatable<List<ValidationError>>,
        ValidatableUniqueUsername<ValidationError>,
        ValidatableUniqueEmail<ValidationError> {
    @Constraints.Pattern(value = "^[A-Za-z0-9]+(?:[\\._-][A-Za-z0-9]+)*$", message = "Username can contain alphanumerics, dots, hyphens and underscores", groups = {Register1.class, Register2.class, Register3.class})
    @Constraints.MaxLength(value = 20, groups = {Register1.class, Register2.class, Register3.class})
    @Constraints.MinLength(value = 3, groups = {Register1.class, Register2.class, Register3.class})
    @Constraints.Required(groups = {Register1.class, Register2.class, Register3.class})
    private String username;
    private String usernameNormalized;
    @Constraints.Required(groups = {Login.class, Register1.class, Register2.class, ChangeEmail.class})
    @Constraints.Email(groups = {Register1.class, Register2.class, ChangeEmail.class})
    private String email;
    @Constraints.MaxLength(value = 32, groups = {Register1.class, ChangePassword.class, SetPassword.class})
    @Constraints.MinLength(value = 4, groups = {Register1.class, ChangePassword.class, SetPassword.class})
    @Constraints.Required(groups = {Login.class, Register1.class, ChangePassword.class, SetPassword.class})
    private String password;
    @Constraints.Required(groups = {ChangePassword.class, ChangeEmail.class})
    private String oldPassword;
    @Constraints.Required(groups = {Register1.class, Register2.class, Register3.class})
    private Boolean acceptTos;

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

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public Boolean getAcceptTos() {
        return acceptTos;
    }

    public void setAcceptTos(Boolean acceptTos) {
        this.acceptTos = acceptTos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationDto that = (RegistrationDto) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(usernameNormalized, that.usernameNormalized) &&
                Objects.equals(email, that.email) &&
                Objects.equals(password, that.password) &&
                Objects.equals(oldPassword, that.oldPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, usernameNormalized, email, password, oldPassword);
    }

    @Override
    public String toString() {
        return "RegistrationDto{" +
                "username='" + username + '\'' +
                ", usernameNormalized='" + usernameNormalized + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public List<ValidationError> validate() {
        return new ArrayList<>();
    }

    @Override
    public ValidationError validateUniqueUsername(UserRepository userRepository) {
        usernameNormalized = Utils.normalizeUsername(username);
        if (usernameNormalized == null) return null;
        if (userRepository.findByUsernameNormalized(usernameNormalized) != null) {
            return new ValidationError("username", "Username already exists");
        }
        return null;
    }


    @Override
    public ValidationError validateUniqueEmail(UserRepository userRepository, String currentUserId) {
        // advanced email validation with commons-validator
//        if(!EmailValidator.getInstance().isValid(dto.getEmail())){
//            form = form.withError("email", "Invalid.userForm.email");
//        }

        email = Utils.normalizeEmail(email);
        if (email == null) return null;
        User byEmail = userRepository.findByEmail(email);
        if (byEmail != null && !byEmail.getId().equals(currentUserId)) {
            return new ValidationError("email", "Email is already registered");
        }
        return null;
    }
}
