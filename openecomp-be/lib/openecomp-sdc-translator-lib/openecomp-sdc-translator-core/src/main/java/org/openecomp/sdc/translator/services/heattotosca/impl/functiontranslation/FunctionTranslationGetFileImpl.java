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

package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.tosca.datatypes.ToscaArtifactType;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.model.ArtifactDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.Template;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SHIRIA
 * @since December 15, 2016.
 */
public class FunctionTranslationGetFileImpl implements FunctionTranslation {
  @Override
  public Object translateFunction(ServiceTemplate serviceTemplate,
                                  String resourceId, String propertyName, String functionKey,
                                  Object functionValue, String heatFileName,
                                  HeatOrchestrationTemplate heatOrchestrationTemplate,
                                  Template toscaTemplate, TranslationContext context) {
    String file = ((String) functionValue).replace("file:///", "");
    Object returnValue;
    final String artifactId = file.split("\\.")[0];

    returnValue = new HashMap<>();
    List artifactParameters = new ArrayList();
    artifactParameters.add(0, ToscaConstants.MODELABLE_ENTITY_NAME_SELF);
    ((Map) returnValue).put(ToscaFunctions.GET_ARTIFACT.getDisplayName(), artifactParameters);
    artifactParameters.add(1, artifactId);

    ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl();
    if (toscaTemplate != null) {
      if (toscaTemplate instanceof NodeTemplate) {
        NodeTemplate nodeTemplate = (NodeTemplate) toscaTemplate;
        ArtifactDefinition artifactDefinition =
            createArtifactDefinition(file, toscaFileOutputService);
        if (nodeTemplate.getArtifacts() == null) {
          nodeTemplate.setArtifacts(new HashMap<>());
        }
        nodeTemplate.getArtifacts().put(artifactId, artifactDefinition);
      }
    }
    return returnValue;
  }

  private static ArtifactDefinition createArtifactDefinition(Object function,
                                                             ToscaFileOutputService
                                                                 toscaFileOutputService) {
    ArtifactDefinition artifactDefinition = new ArtifactDefinition();
    artifactDefinition.setType(ToscaArtifactType.NATIVE_DEPLOYMENT);
    artifactDefinition
        .setFile("../" + toscaFileOutputService.getArtifactsFolderName() + "/" + function);
    return artifactDefinition;
  }

}
