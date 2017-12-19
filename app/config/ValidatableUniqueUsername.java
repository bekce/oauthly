package config;

import repositories.UserRepository;

public interface ValidatableUniqueUsername<T> {
    T validateUniqueUsername(UserRepository userRepository);
}
