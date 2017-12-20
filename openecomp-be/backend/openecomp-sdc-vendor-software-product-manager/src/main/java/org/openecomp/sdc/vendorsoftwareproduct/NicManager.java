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

package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface NicManager {

  Collection<NicEntity> listNics(String vspId, Version version, String componentId);

  NicEntity createNic(NicEntity nic);

  CompositionEntityValidationData updateNic(NicEntity nicEntity);

  CompositionEntityResponse<Nic> getNic(String vspId, Version version, String componentId,
                                        String nicId);

  void deleteNic(String vspId, Version version, String componentId, String nicId);

  QuestionnaireResponse getNicQuestionnaire(String vspId, Version version, String componentId,
                                            String nicId);

  void updateNicQuestionnaire(String vspId, Version version, String componentId, String nicId,
                              String questionnaireData);
}
