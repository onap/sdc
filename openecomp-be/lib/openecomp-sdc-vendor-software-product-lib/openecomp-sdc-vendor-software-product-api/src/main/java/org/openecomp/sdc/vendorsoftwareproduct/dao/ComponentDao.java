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

package org.openecomp.sdc.vendorsoftwareproduct.dao;

import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface ComponentDao extends VersionableDao, BaseDao<ComponentEntity> {

  ComponentEntity getQuestionnaireData(String vspId, Version version, String componentId);

  void updateQuestionnaireData(String vspId, Version version, String componentId,
                               String questionnaireData);

  Collection<ComponentEntity> listQuestionnaires(String vspId, Version version);

  Collection<ComponentEntity> listCompositionAndQuestionnaire(String vspId, Version version);

  void deleteAll(String vspId, Version version);
}
