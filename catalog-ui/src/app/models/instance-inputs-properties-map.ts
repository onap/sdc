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
 * Created by obarda on 9/12/2016.
 */
'use strict';
import {InputPropertyBase} from "./input-property-base";
import {PropertyModel} from "./properties";
import {InputModel} from "./inputs";

export class InstancesInputsOrPropertiesMapData {
    [instanceId:string]:Array<InputPropertyBase>;
}

export class InstancesInputsPropertiesMap {
    componentInstanceProperties:InstancesInputsOrPropertiesMapData;
    componentInstanceInputsMap:InstancesInputsOrPropertiesMapData;

    constructor(componentInstanceInputsMapData:InstancesInputsOrPropertiesMapData, componentInstanceInputsPropertiesMapData:InstancesInputsOrPropertiesMapData) {
        this.componentInstanceInputsMap = componentInstanceInputsMapData;
        this.componentInstanceProperties = componentInstanceInputsPropertiesMapData;
    }

    private removeUnnecessaryData = (properties:Array<InputPropertyBase>, instanceId:string, mapData:any) => {
        mapData[instanceId] = [];
        if (properties && properties.length > 0) {
            _.forEach(properties, (propertyOrInput:InputPropertyBase) => {
                if (propertyOrInput instanceof PropertyModel) { // Handle Properties
                    if (propertyOrInput && !propertyOrInput.isAlreadySelected) {
                        mapData[instanceId].push(propertyOrInput);
                    }
                }
                if (propertyOrInput instanceof InputModel) { // Handle Inputs
                    if (propertyOrInput && !propertyOrInput.isAlreadySelected) {
                        mapData[instanceId].push(propertyOrInput);
                    }
                }
            });
            if (mapData[instanceId].length === 0) {
                delete mapData[instanceId];
            }
        } else {
            delete mapData[instanceId];
        }
    }

    /*
     In the toJson we remove all inputs and property already selected (The check box selected but they are disable)
     also we remove empty array in order to prevent Backend error
     */

    public cleanUnnecessaryDataBeforeSending = ():InstancesInputsPropertiesMap => {

        let map:InstancesInputsPropertiesMap = new InstancesInputsPropertiesMap(new InstancesInputsOrPropertiesMapData(), new InstancesInputsOrPropertiesMapData());
        angular.copy(this, map);

        //Removing unnecessary data from inputs map
        _.forEach(map.componentInstanceInputsMap, (inputs:Array<InputModel>, instanceId:string) => {
            this.removeUnnecessaryData(inputs, instanceId, map.componentInstanceInputsMap);
        });

        //Removing unnecessary data from properties map
        _.forEach(map.componentInstanceProperties, (properties:Array<PropertyModel>, instanceId:string) => {
            this.removeUnnecessaryData(properties, instanceId, map.componentInstanceProperties);
        });

        return map;
    };
}
