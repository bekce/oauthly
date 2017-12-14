package config;

import models.Grant;
import play.libs.typedmap.TypedKey;
import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@With(ResourceServerAuthAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceServerSecure {
    TypedKey<Grant> GRANT = TypedKey.create("grant_r");

    /**
     * Whitespace separated list of scopes for this endpoint.
     * @return
     */
    String scope();
}