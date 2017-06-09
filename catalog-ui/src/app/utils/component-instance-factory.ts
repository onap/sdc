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
'use strict';
import {ComponentInstance, ServiceInstance, ProductInstance, ResourceInstance, Component} from "../models";
import {LeftPaletteComponent} from "../models/components/displayComponent";

export class ComponentInstanceFactory {

    static createComponentInstance(componentInstance:ComponentInstance):ComponentInstance {
        let newComponentInstance:ComponentInstance;
        switch (componentInstance.originType) {
            case 'SERVICE':
                newComponentInstance = new ServiceInstance(componentInstance);
                break;

            case 'PRODUCT':
                newComponentInstance = new ProductInstance(componentInstance);
                break;

            default :
                newComponentInstance = new ResourceInstance(componentInstance);
                break;
        }
        return newComponentInstance;
    };

    public createEmptyComponentInstance = (componentInstanceType?:string):ComponentInstance => {
        let newComponentInstance:ComponentInstance;
        switch (componentInstanceType) {
            case 'SERVICE':
                newComponentInstance = new ServiceInstance();
                break;

            case 'PRODUCT':
                newComponentInstance = new ProductInstance();
                break;

            default :
                newComponentInstance = new ResourceInstance();
                break;
        }
        return newComponentInstance;
    };

    public createComponentInstanceFromComponent = (component:LeftPaletteComponent):ComponentInstance => {
        let newComponentInstance:ComponentInstance = this.createEmptyComponentInstance(component.componentType);
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
