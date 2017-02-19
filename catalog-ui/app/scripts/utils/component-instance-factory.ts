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
/**
 * Created by obarda on 3/7/2016.
 */
/**
 * Created by obarda on 2/8/2016.
 */
/// <reference path="../references"/>
module Sdc.Utils {
    'use strict';

    export class ComponentInstanceFactory {

        static createComponentInstance(componentInstance:Models.ComponentsInstances.ComponentInstance):Models.ComponentsInstances.ComponentInstance {
            let newComponentInstance:Models.ComponentsInstances.ComponentInstance;
            switch (componentInstance.originType) {
                case 'SERVICE':
                    newComponentInstance = new Models.ComponentsInstances.ServiceInstance(componentInstance);
                    break;

                case 'PRODUCT':
                    newComponentInstance = new Models.ComponentsInstances.ProductInstance(componentInstance);
                    break;

                default :
                    newComponentInstance = new Models.ComponentsInstances.ResourceInstance(componentInstance);
                    break;
            }
            return newComponentInstance;
        };

        public createEmptyComponentInstance = (componentInstanceType?: string):Models.ComponentsInstances.ComponentInstance => {
            let newComponentInstance:Models.ComponentsInstances.ComponentInstance;
            switch (componentInstanceType) {
                case 'SERVICE':
                    newComponentInstance = new Models.ComponentsInstances.ServiceInstance();
                    break;

                case 'PRODUCT':
                    newComponentInstance = new Models.ComponentsInstances.ProductInstance();
                    break;

                default :
                    newComponentInstance = new Models.ComponentsInstances.ResourceInstance();
                    break;
            }
            return newComponentInstance;
        };

        public createComponentInstanceFromComponent = (component: Models.Components.Component):Models.ComponentsInstances.ComponentInstance => {
            let newComponentInstance:Models.ComponentsInstances.ComponentInstance = this.createEmptyComponentInstance(component.componentType);
            newComponentInstance.uniqueId = component.uniqueId + (new Date()).getTime();
            newComponentInstance.posX = 0;
            newComponentInstance.posY = 0;
            newComponentInstance.name = component.name;
            newComponentInstance.componentVersion = component.version;
            newComponentInstance.originType = component.getComponentSubType();
            //new component instance -> req. & cap. are added on successful instance creation
            newComponentInstance.requirements = component.requirements;
            newComponentInstance.capabilities = component.capabilities;
            newComponentInstance.icon = component.icon;
            newComponentInstance.componentUid = component.uniqueId;
            return newComponentInstance;
        };

    }
}
