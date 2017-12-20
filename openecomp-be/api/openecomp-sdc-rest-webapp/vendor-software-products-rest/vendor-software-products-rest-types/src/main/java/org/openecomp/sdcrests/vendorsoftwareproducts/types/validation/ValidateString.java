package org.openecomp.sdcrests.vendorsoftwareproducts.types.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The interface Validate string.
 */
@Documented
@Constraint(validatedBy = StringValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER, CONSTRUCTOR})
@Retention(RUNTIME)
public @interface ValidateString {
  /**
   * The constant message.
   */
  String message = "";

  /**
   * Accepted values string [ ].
   *
   * @return the string [ ]
   */
  String[] acceptedValues();

  String message() default "{org.openecomp.sdcrests.vendorsoftwareproducts.types.validation"
      + ".message}";

  boolean isCaseSensitive() default false;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
