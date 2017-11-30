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
'use strict';

import {CompositionCiNodeUcpeCp, Module, ModuleNodeBase, CompositionCiNodeVf, CompositionCiNodeVl, CompositionCiNodeCp, CompositionCiNodeConfiguration,
    NodeUcpe, CompositionCiNodeService,CompositionCiNodeServiceProxy, CompositionCiNodeBase, ComponentInstance} from "./../../../models";
import {ComponentType, ResourceType} from "../../../utils/constants";
import {ImageCreatorService} from "../../../directives/graphs-v2/image-creator/image-creator.service";

export class NodesFactory {

    constructor(private imageCreator:ImageCreatorService) {
    }

    public createNode = (instance:ComponentInstance):CompositionCiNodeBase => {

        if (instance.isUcpe()) {
            return new NodeUcpe(instance, this.imageCreator);
        }
        if (instance.originType === ComponentType.SERVICE) {
            return new CompositionCiNodeService(instance, this.imageCreator);
        }
        if (instance.originType === ComponentType.SERVICE_PROXY) {
            return new CompositionCiNodeServiceProxy(instance, this.imageCreator);
        }
        if (instance.originType === ResourceType.CP) {
            return new CompositionCiNodeCp(instance, this.imageCreator);
        }
        if (instance.originType === ResourceType.VL) {
            return new CompositionCiNodeVl(instance, this.imageCreator);
        }
        if (instance.originType === ResourceType.CONFIGURATION) {
            return new CompositionCiNodeConfiguration(instance, this.imageCreator);
        }

        return new CompositionCiNodeVf(instance, this.imageCreator);
    };

    public createModuleNode = (module:Module):ModuleNodeBase => {

        return new ModuleNodeBase(module);
    };

    public createUcpeCpNode = (instance:ComponentInstance):CompositionCiNodeCp => {

        return new CompositionCiNodeUcpeCp(instance, this.imageCreator);
    }
}

NodesFactory.$inject = [
    'ImageCreatorService'
];
