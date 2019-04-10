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

package org.openecomp.sdc.be.tosca.utils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.WordUtils;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public class OperationArtifactUtil {

    public static final String BPMN_ARTIFACT_PATH = "BPMN";

    private OperationArtifactUtil() {
        //Hiding implicit public constructor
    }

    /**
     * This method assumes that operation.getImplementation() is not NULL  ( it should be verified by the caller method)
     *
     * @param operation     the specific operation name
     * @return the full path including file name for operation's artifacts
     */
    static String createOperationArtifactPath(Component component, ComponentInstance componentInstance,
                                              OperationDataDefinition operation, boolean isAssociatedComponent) {
        if (!(component instanceof Resource || component instanceof Service)) {
            return null;
        }

        if (isAssociatedComponent) {
            // Service Proxy is only in Node Template interface
            if(componentInstance != null) {
                return createOperationArtifactPathInService(componentInstance.getToscaComponentName()
                                                                    + "_v" + component.getVersion(), operation);
            }
            // Resource Instance is part of Node Type interface
            else {
                ResourceMetadataDataDefinition resourceMetadataDataDefinition =
                        (ResourceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition();
                return createOperationArtifactPathInService(resourceMetadataDataDefinition.getToscaResourceName()
                                                                    + "_v" + component.getVersion(), operation);
            }
        }
        return createOperationArtifactPathInComponent(operation);
    }


    private static String createOperationArtifactPathInComponent(OperationDataDefinition operation) {
        return CsarUtils.ARTIFACTS + File.separator + WordUtils.capitalizeFully(ArtifactGroupTypeEnum.DEPLOYMENT.name())
                + File.separator + ArtifactTypeEnum.WORKFLOW.name() + File.separator + BPMN_ARTIFACT_PATH
                + File.separator + operation.getImplementation().getArtifactName();
    }

    private static String createOperationArtifactPathInService(String toscaComponentName,
                                                          OperationDataDefinition operation) {
        return CsarUtils.ARTIFACTS + File.separator + toscaComponentName + File.separator +
                WordUtils.capitalizeFully(ArtifactGroupTypeEnum.DEPLOYMENT.name()) + File.separator +
                ArtifactTypeEnum.WORKFLOW.name() + File.separator + BPMN_ARTIFACT_PATH + File.separator +
                operation.getImplementation().getArtifactName();
    }

    public static Map<String, ArtifactDefinition> getDistinctInterfaceOperationArtifactsByName(Component originComponent) {
        Map<String, ArtifactDefinition> distinctInterfaceArtifactsByName = new HashMap<>();
        Map<String, InterfaceDefinition> interfaces = originComponent.getInterfaces();
        if (MapUtils.isEmpty(interfaces)) {
            return distinctInterfaceArtifactsByName;
        }
        Map<String, ArtifactDefinition> interfaceArtifacts = interfaces.values().stream()
                .flatMap(interfaceDefinition -> interfaceDefinition.getOperationsMap().values().stream())
                .map(Operation::getImplementationArtifact).filter(Objects::nonNull)
                .collect(Collectors.toMap(ArtifactDataDefinition::getUniqueId,
                        artifactDefinition -> artifactDefinition));
        if (MapUtils.isNotEmpty(interfaceArtifacts)) {
            Set<String> artifactNameSet = new HashSet<>();
            for (Map.Entry<String, ArtifactDefinition> interfaceArtifactEntry : interfaceArtifacts.entrySet()) {
                String artifactName = interfaceArtifactEntry.getValue().getArtifactName();
                if (artifactNameSet.contains(artifactName)) {
                    continue;
                }
                distinctInterfaceArtifactsByName.put(interfaceArtifactEntry.getKey(),
                        interfaceArtifactEntry.getValue());
                artifactNameSet.add(artifactName);
            }

        }
        return distinctInterfaceArtifactsByName;
    }
}
