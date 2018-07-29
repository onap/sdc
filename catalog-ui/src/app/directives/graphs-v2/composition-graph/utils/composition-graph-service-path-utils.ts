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

import * as _ from "lodash";
import {LoaderService} from "app/services";
import {CompositionGraphGeneralUtils} from "./composition-graph-general-utils";
import {ICompositionGraphScope} from "../composition-graph.directive";
import {ServiceServiceNg2} from 'app/ng2/services/component-services/service.service';
import {Service} from "../../../../models/components/service";
import {ForwardingPath} from "app/models/forwarding-path";
import {ForwardingPathLink} from "app/models/forwarding-path-link";
import {CompositionCiServicePathLink} from "../../../../models/graph/graph-links/composition-graph-links/composition-ci-service-path-link";
import {CommonGraphUtils} from "app/directives/graphs-v2/common/common-graph-utils";

export class ServicePathGraphUtils {

    constructor(
        private loaderService:LoaderService,
        private generalGraphUtils:CompositionGraphGeneralUtils,
        private serviceService:ServiceServiceNg2,
        private commonGraphUtils:CommonGraphUtils
    ) {}

    public deletePathsFromGraph(cy: Cy.Instance, service:Service){
       cy.remove(`[type="${CompositionCiServicePathLink.LINK_TYPE}"]`);
    }
    
    public drawPath(cy: Cy.Instance, forwardingPath: ForwardingPath, service:Service) {
        let pathElements = forwardingPath.pathElements.listToscaDataDefinition;

        _.forEach(pathElements, (link: ForwardingPathLink) => {
            let data:CompositionCiServicePathLink = new CompositionCiServicePathLink(link);
            data.source = _.find(
                service.componentInstances,
                instance => instance.name === data.forwardingPathLink.fromNode
            ).uniqueId;
            data.target = _.find(
                service.componentInstances,
                instance => instance.name === data.forwardingPathLink.toNode
            ).uniqueId;
            data.pathId = forwardingPath.uniqueId;
            data.pathName = forwardingPath.name;
            this.commonGraphUtils.insertServicePathLinkToGraph(cy, data);
        });
    }

    public createOrUpdateServicePath = (scope:ICompositionGraphScope, path: any): void => {
        let service = <Service>scope.component;
        this.loaderService.showLoader('composition-graph');

        let onSuccess: (response: ForwardingPath) => void = (response: ForwardingPath) => {

            service.forwardingPaths[response.uniqueId] = response;
            scope.selectedPathId = response.uniqueId;
        };

        this.generalGraphUtils.getGraphUtilsServerUpdateQueue().addBlockingUIActionWithReleaseCallback(
            () => this.serviceService.createOrUpdateServicePath(service, path).subscribe(onSuccess),
            () => this.loaderService.hideLoader('composition-graph')
        );
    };
}

ServicePathGraphUtils.$inject = [
    'LoaderService',
    'CompositionGraphGeneralUtils',
    'ServiceServiceNg2',
    'CommonGraphUtils'
];
