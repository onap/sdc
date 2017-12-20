package org.openecomp.core.converter.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

public class CreateToscaObjectErrorBuilder extends BaseErrorBuilder {
  private static final String CANT_CREATE_OBJECT_FROM_CLASS =
      "Can't create %s from %s.";
  private static final String IMPORT_TOSCA = "IMPORT_TOSCA";

  public CreateToscaObjectErrorBuilder(String className,
                                       String objectId) {
    getErrorCodeBuilder()
        .withId(IMPORT_TOSCA)
        .withCategory(ErrorCategory.APPLICATION)
        .withMessage(String.format(CANT_CREATE_OBJECT_FROM_CLASS, className, objectId));
  }
}
