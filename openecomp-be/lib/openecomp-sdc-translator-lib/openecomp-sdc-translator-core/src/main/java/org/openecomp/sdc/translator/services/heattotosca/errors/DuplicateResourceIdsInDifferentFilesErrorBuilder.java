package org.openecomp.sdc.translator.services.heattotosca.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

public final class DuplicateResourceIdsInDifferentFilesErrorBuilder extends BaseErrorBuilder {

  private static final String DUPLICATE_RESOURCE_ID_MSG = "Resource with id %s occurs more than once in " +
      "different addOn files";

  public DuplicateResourceIdsInDifferentFilesErrorBuilder(String resourceId) {
    getErrorCodeBuilder().withId(TranslatorErrorCodes.DUPLICATE_RESOURCE_ID_IN_DIFFERENT_FILES)
    .withCategory(ErrorCategory.APPLICATION)
    .withMessage(String.format(DUPLICATE_RESOURCE_ID_MSG, resourceId));
  }
}
