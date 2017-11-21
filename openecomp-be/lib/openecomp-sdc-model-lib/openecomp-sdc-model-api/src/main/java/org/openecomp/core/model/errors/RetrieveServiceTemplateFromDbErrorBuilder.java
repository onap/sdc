package org.openecomp.core.model.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

public class RetrieveServiceTemplateFromDbErrorBuilder extends BaseErrorBuilder {
  private static final String CANT_RETRIEVE_SERVICE_TEMPLATE = "Could not retrirve service " +
      "template named %s. Reason - %s";
  private static final String CREATE_SERVICE_TEMPLATE = "CREATE_SERVICE_TEMPLATE";

  public RetrieveServiceTemplateFromDbErrorBuilder(String serviceTemplateName,
                                                   String reason){
    this.getErrorCodeBuilder()
        .withCategory(ErrorCategory.APPLICATION)
        .withId(CREATE_SERVICE_TEMPLATE)
        .withMessage(String.format(CANT_RETRIEVE_SERVICE_TEMPLATE, serviceTemplateName, reason));
  }
}
