/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.tosca.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.tosca.services.ToscaConstants;


/**
 * The type Tosca invalid substitute node template properties error builder.
 */
public class ToscaInvalidSubstituteNodeTemplatePropertiesErrorBuilder extends BaseErrorBuilder {

  private static final String INVALID_SUBSTITUTE_NODE_TEMPLATE_MSG =
      "Invalid Substitute Node Template %s, mandatory map property %s with mandatory "
          + "key %s must be defined.";

  /**
   * Instantiates a new Tosca invalid substitute node template properties error builder.
   *
   * @param nodeTemplateId the node template id
   */
  public ToscaInvalidSubstituteNodeTemplatePropertiesErrorBuilder(String nodeTemplateId) {
    builder.withId(ToscaErrorCodes.INVALID_SUBSTITUTE_NODE_TEMPLATE);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(INVALID_SUBSTITUTE_NODE_TEMPLATE_MSG, nodeTemplateId,
        ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME,
        ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME));
  }

}
