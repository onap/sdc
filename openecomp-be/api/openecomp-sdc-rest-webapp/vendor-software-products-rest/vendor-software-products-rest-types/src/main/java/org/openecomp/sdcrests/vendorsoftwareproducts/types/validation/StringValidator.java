package org.openecomp.sdcrests.vendorsoftwareproducts.types.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class StringValidator implements ConstraintValidator<ValidateString, String> {

  private List<String> valueList;
  boolean isCaseSensitive;

  @Override
  public void initialize(ValidateString constraintAnnotation) {
    valueList = new ArrayList<String>();
    isCaseSensitive = constraintAnnotation.isCaseSensitive();
    for (String val : constraintAnnotation.acceptedValues()) {
      if (!isCaseSensitive) {
        val = val.toUpperCase();
      }
      valueList.add(val);
    }
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (!isCaseSensitive) {
      value = value.toUpperCase();
    }
    if (value != null && !valueList.contains(value)) {
      return false;
    }
    return true;
  }

}