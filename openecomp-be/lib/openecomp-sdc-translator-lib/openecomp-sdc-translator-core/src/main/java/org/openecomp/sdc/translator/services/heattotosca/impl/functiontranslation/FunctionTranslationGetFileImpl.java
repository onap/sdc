/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.onap.sdc.tosca.datatypes.model.ArtifactDefinition;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.ToscaArtifactType;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionTranslationGetFileImpl implements FunctionTranslation {
    private static ArtifactDefinition createArtifactDefinition(Object function,
                                                               ToscaFileOutputService toscaFileOutputService) {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setType(ToscaArtifactType.NATIVE_DEPLOYMENT);
        artifactDefinition.setFile("../" + toscaFileOutputService.getArtifactsFolderName() + "/" + function);
        return artifactDefinition;
    }

    @Override
    public Object translateFunction(FunctionTranslator functionTranslator) {

        String file = ((String) functionTranslator.getFunctionValue()).replace("file:///", "");
        final String artifactId = file.split("\\.")[0];
        Map<String, Object> returnValue = new HashMap<>();
        List<String> artifactParameters = new ArrayList<>();
        artifactParameters.add(ToscaConstants.MODELABLE_ENTITY_NAME_SELF);
        returnValue.put(ToscaFunctions.GET_ARTIFACT.getDisplayName(), artifactParameters);
        artifactParameters.add(artifactId);

        ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl();
        if (functionTranslator.getToscaTemplate() instanceof NodeTemplate) {
            NodeTemplate nodeTemplate = (NodeTemplate) functionTranslator.getToscaTemplate();
            ArtifactDefinition artifactDefinition = createArtifactDefinition(file, toscaFileOutputService);
            if (nodeTemplate.getArtifacts() == null) {
                nodeTemplate.setArtifacts(new HashMap<>());
            }
            nodeTemplate.getArtifacts().put(artifactId, artifactDefinition);
        }
        return returnValue;
    }

}
