package org.openecomp.sdc.translator.services.heattotosca.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

public class DuplicateResourceIdsInDifferentFilesErrorBuilder extends BaseErrorBuilder {

  private String DUPLICATE_RESOURCE_ID_MSG = "Resource with id %s occures more " + "than once in " +
      "different addOn files";

  public DuplicateResourceIdsInDifferentFilesErrorBuilder(String resourceId){
    getErrorCodeBuilder().withId(TranslatorErrorCodes.DUPLICATE_RESOURCE_ID_IN_DIFFERENT_FILES);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(String
        .format(DUPLICATE_RESOURCE_ID_MSG, resourceId));
  }
}
