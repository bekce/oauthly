package config;

import play.data.validation.Constraints;
import repositories.UserRepository;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;

public class ValidateUniqueUsernameValidator implements Constraints.PlayConstraintValidator<ValidateUniqueUsername, ValidatableUniqueUsername<?>> {

    private final UserRepository userRepository;

    @Inject
    public ValidateUniqueUsernameValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void initialize(ValidateUniqueUsername constraintAnnotation) {
    }

    @Override
    public boolean isValid(ValidatableUniqueUsername<?> value, ConstraintValidatorContext context) {
        return reportValidationStatus(value.validateUniqueUsername(this.userRepository), context);
    }
}
