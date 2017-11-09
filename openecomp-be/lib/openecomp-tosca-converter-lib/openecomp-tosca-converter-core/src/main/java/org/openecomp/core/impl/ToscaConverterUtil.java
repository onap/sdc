package org.openecomp.core.impl;

import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class ToscaConverterUtil {

  public static <T> T createObjectFromClass(String objectId,
                                       Object objectCandidate,
                                       Class<T> classToCreate) {
    try {
      String objectAsString = new ObjectMapper().writeValueAsString(objectCandidate);
      return JsonUtil.json2Object(objectAsString, classToCreate);
    } catch (Exception e) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder()
          .withCategory(ErrorCategory.APPLICATION)
          .withMessage("Can't create " + classToCreate.getSimpleName() + " from " +
              objectId + ". Reason - " + e.getMessage()).build());
    }
  }
}
