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
import { ComponentType } from 'app/utils';
import { Component, ComponentInstance, ResourceInstance, ServiceInstance, ServiceSubstitutionInstance, ServiceProxyInstance } from '../models';
import { LeftPaletteComponent } from '../models/components/displayComponent';

export class ComponentInstanceFactory {

    static createComponentInstance(componentInstance: ComponentInstance): ComponentInstance {
        let newComponentInstance: ComponentInstance;
        switch (componentInstance.originType) {
            case ComponentType.SERVICE:
                newComponentInstance = new ServiceInstance(componentInstance);
                break;
           case ComponentType.SERVICE_PROXY:
                newComponentInstance = new ServiceProxyInstance(componentInstance);
                break;
           case ComponentType.SERVICE_SUBSTITUTION:
                newComponentInstance = new ServiceSubstitutionInstance(componentInstance);
                break;
            default :
                newComponentInstance = new ResourceInstance(componentInstance);
                break;
        }
        return newComponentInstance;
    }

    static createEmptyComponentInstance = (componentInstanceType?: string): ComponentInstance => {
        let newComponentInstance: ComponentInstance;
        switch (componentInstanceType) {
            case ComponentType.SERVICE:
                newComponentInstance = new ServiceInstance();
                break;
            case ComponentType.SERVICE_PROXY:
                newComponentInstance = new ServiceProxyInstance();
                break;
           case ComponentType.SERVICE_SUBSTITUTION:
                newComponentInstance = new ServiceSubstitutionInstance();
                break;
            default :
                newComponentInstance = new ResourceInstance();
                break;
        }
        return newComponentInstance;
    }

    static createComponentInstanceFromComponent = (component: LeftPaletteComponent, useServiceSubstitutionForNestedServices?: boolean): ComponentInstance => {
        const newComponentInstance: ComponentInstance = ComponentInstanceFactory.createEmptyComponentInstance(component.componentType);
        newComponentInstance.uniqueId = component.uniqueId + (new Date()).getTime();
        newComponentInstance.posX = 0;
        newComponentInstance.posY = 0;
        newComponentInstance.name = component.name;
        newComponentInstance.componentVersion = component.version;
        newComponentInstance.originType = getOriginType(component);
        newComponentInstance.requirements = component.requirements;
        newComponentInstance.capabilities = component.capabilities;
        newComponentInstance.icon = component.icon;
        newComponentInstance.componentUid = component.uniqueId;
        return newComponentInstance;

        function getOriginType(component: LeftPaletteComponent): string  {
            if (component.componentSubType) {
                return component.componentSubType;
            } else {
                if (component.componentType === ComponentType.SERVICE) {
	                if (useServiceSubstitutionForNestedServices){
			           return ComponentType.SERVICE_SUBSTITUTION;
                    } else {
                       return ComponentType.SERVICE_PROXY;
                    }
                } else {
                    return component.resourceType;
                }
            }
        }
    }
}
