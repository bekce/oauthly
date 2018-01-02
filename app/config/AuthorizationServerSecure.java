package config;

import models.User;
import play.libs.typedmap.TypedKey;
import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@With(AuthorizationServerAuthAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizationServerSecure {
    TypedKey<User> USER = TypedKey.create("user_a");
    boolean requireAdmin() default false;
    boolean optional() default false;
}
