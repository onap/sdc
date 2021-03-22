/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition;

import org.onap.sdc.tosca.datatypes.model.NodeTemplate;

/**
 * Created by Talio on 4/4/2017.
 */
public class NodeTemplateInformation {

    UnifiedCompositionEntity unifiedCompositionEntity;
    private NodeTemplate nodeTemplate;

    public NodeTemplateInformation() {
    }

    public NodeTemplateInformation(UnifiedCompositionEntity unifiedCompositionEntity, NodeTemplate nodeTemplate) {
        this.unifiedCompositionEntity = unifiedCompositionEntity;
        this.nodeTemplate = nodeTemplate;
    }

    public UnifiedCompositionEntity getUnifiedCompositionEntity() {
        return unifiedCompositionEntity;
    }

    public void setUnifiedCompositionEntity(UnifiedCompositionEntity unifiedCompositionEntity) {
        this.unifiedCompositionEntity = unifiedCompositionEntity;
    }

    public NodeTemplate getNodeTemplate() {
        return nodeTemplate;
    }

    public void setNodeTemplate(NodeTemplate nodeTemplate) {
        this.nodeTemplate = nodeTemplate;
    }
}
