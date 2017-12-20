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

package org.openecomp.sdc.versioning.errors;

import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;

/**
 * The type Versionable sub entity not found error builder.
 */
public class VersionableSubEntityNotFoundErrorBuilder extends BaseErrorBuilder {

  private static final String SUB_ENTITY_NOT_FOUND_MSG =
      "%s with Id %s does not exist for %s with id %s and version %s";
  private static final String SUB_ENTITIES_NOT_FOUND_MSG =
      "%ss with Ids %s do not exist for %s with id %s and version %s";

  /**
   * Instantiates a new Versionable sub entity not found error builder.
   *
   * @param entityType           the entity type
   * @param entityId             the entity id
   * @param containingEntityType the containing entity type
   * @param containingEntityId   the containing entity id
   * @param version              the version
   */
  public VersionableSubEntityNotFoundErrorBuilder(String entityType, String entityId,
                                                  String containingEntityType,
                                                  String containingEntityId, Version version) {
    getErrorCodeBuilder().withId(VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(String
        .format(SUB_ENTITY_NOT_FOUND_MSG, entityType, entityId, containingEntityType,
            containingEntityId, version.getId()));
  }

  /**
   * Instantiates a new Versionable sub entity not found error builder.
   *
   * @param entityType           the entity type
   * @param entityIds            the entity ids
   * @param containingEntityType the containing entity type
   * @param containingEntityId   the containing entity id
   * @param version              the version
   */
  public VersionableSubEntityNotFoundErrorBuilder(String entityType, List<String> entityIds,
                                                  String containingEntityType,
                                                  String containingEntityId, Version version) {
    getErrorCodeBuilder().withId(VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(String.format(SUB_ENTITIES_NOT_FOUND_MSG, entityType,
        CommonMethods.listToSeparatedString(entityIds, ','), containingEntityType,
        containingEntityId, version.toString()));
  }
}
