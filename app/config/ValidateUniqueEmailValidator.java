package config;

import play.data.validation.Constraints;
import repositories.UserRepository;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;

public class ValidateUniqueEmailValidator implements Constraints.PlayConstraintValidator<ValidateUniqueEmail, ValidatableUniqueEmail<?>> {

    private final UserRepository userRepository;

    @Inject
    public ValidateUniqueEmailValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void initialize(ValidateUniqueEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(ValidatableUniqueEmail<?> value, ConstraintValidatorContext context) {
        return reportValidationStatus(value.validateUniqueEmail(this.userRepository, null), context);
    }
}
