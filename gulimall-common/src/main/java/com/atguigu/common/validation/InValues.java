package com.atguigu.common.validation;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {InValuesConstraintValidator.class}
)
public @interface InValues {

    String message() default "Not accepted value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int[] values() default{};
}
