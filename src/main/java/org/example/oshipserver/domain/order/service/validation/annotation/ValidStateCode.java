package org.example.oshipserver.domain.order.service.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = org.example.oshipserver.domain.order.service.validation.validator.StateCodeValidator.class)
public @interface ValidStateCode {
    String message() default "일부 국가에서는 StateCode가 필수입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}