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

package org.openecomp.sdc.vendorsoftwareproduct.services.composition;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface CompositionEntityDataManager {

  Map<CompositionEntityId, Collection<String>> validateEntitiesQuestionnaire();

  void addEntity(CompositionEntity entity, SchemaTemplateInput schemaTemplateInput);

  CompositionEntityValidationData validateEntity(CompositionEntity entity,
                                                 SchemaTemplateContext schemaTemplateContext,
                                                 SchemaTemplateInput schemaTemplateInput);

  void buildTrees();

  void addErrorsToTrees(Map<CompositionEntityId, Collection<String>> errors);

  Set<CompositionEntityValidationData> getEntityListWithErrors();

  Collection<CompositionEntityValidationData> getTrees();

  void saveCompositionData(String vspId, Version version, CompositionData compositionData);

  Set<CompositionEntityValidationData> getAllErrorsByVsp(String vspId);

  void saveComponents(String vspId, Version version, CompositionData compositionData,
                      Map<String, String> networkIdByName);

  void saveNicsByComponent(String vspId, Version version, Map<String, String> networkIdByName,
                           Component component, String componentId);

  Map<String, String> saveNetworks(String vspId, Version version, CompositionData compositionData);

  NetworkEntity createNetwork(NetworkEntity network);

  ComponentEntity createComponent(ComponentEntity component);

  NicEntity createNic(NicEntity nic);

  DeploymentFlavorEntity createDeploymentFlavor(DeploymentFlavorEntity deploymentFlavor);

  ImageEntity createImage(ImageEntity image);

  void saveComputesFlavorByComponent(String vspId, Version version, Component component,
                                     String componentId);

  void saveImagesByComponent(String vspId, Version version, Component component,
                             String componentId);

}
