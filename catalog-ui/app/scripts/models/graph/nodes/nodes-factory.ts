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
/// <reference path="../../../references"/>
module Sdc.Utils {
    'use strict';

    export class NodesFactory {

        constructor(
            private imageCreator:ImageCreatorService) {
        }

        public createNode = (instance:Models.ComponentsInstances.ComponentInstance):Models.Graph.CompositionCiNodeBase => {

            if (instance.isUcpe()) {
                return new Models.Graph.NodeUcpe(instance, this.imageCreator);
            }
            if (instance.originType === Utils.Constants.ComponentType.SERVICE) {
                return new Models.Graph.CompositionCiNodeService(instance, this.imageCreator);
            }
            if (instance.originType === Utils.Constants.ResourceType.CP) {
                return new Models.Graph.CompositionCiNodeCp(instance, this.imageCreator);
            }
            if (instance.originType === Utils.Constants.ResourceType.VL) {
                return new Models.Graph.CompositionCiNodeVl(instance, this.imageCreator);
            }

            return new Models.Graph.CompositionCiNodeVf(instance, this.imageCreator);
        };

        public createModuleNode = (module:Models.Module):Models.Graph.ModuleNodeBase => {

            return new Models.Graph.ModuleNodeBase(module);
        };

        public createUcpeCpNode = (instance:Models.ComponentsInstances.ComponentInstance):Models.Graph.CompositionCiNodeCp => {


            return new Models.Graph.CompositionCiNodeUcpeCp(instance, this.imageCreator);
        }
    }

    NodesFactory.$inject = [
        'ImageCreatorService'
    ];
}
