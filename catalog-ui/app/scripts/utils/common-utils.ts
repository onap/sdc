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
 * Created by obarda on 6/30/2016.
 */
/// <reference path="../references.ts"/>
module Sdc.Utils {

    export class CommonUtils {

        static initProperties(propertiesObj:Array<Sdc.Models.PropertyModel>, uniqueId?:string):Array<Sdc.Models.PropertyModel> {

            let properties = new Array<Sdc.Models.PropertyModel>();
            if (propertiesObj) {
                _.forEach(propertiesObj, (property:Sdc.Models.PropertyModel):void => {
                    if (uniqueId) {
                        property.readonly = property.parentUniqueId != uniqueId;
                    }
                    properties.push(new Sdc.Models.PropertyModel(property));
                });
            }
            return properties;
        };

        static initAttributes(attributesObj:Array<Sdc.Models.AttributeModel>, uniqueId?:string):Array<Sdc.Models.AttributeModel> {

            let attributes = new Array<Sdc.Models.AttributeModel>();
            if (attributesObj) {
                _.forEach(attributesObj, (attribute:Sdc.Models.AttributeModel):void => {
                    if (uniqueId) {
                        attribute.readonly = attribute.parentUniqueId != uniqueId;
                    }
                    attributes.push(new Sdc.Models.AttributeModel(attribute));
                });
            }
            return attributes;
        };

        static initComponentInstances(componentInstanceObj:Array<Models.ComponentsInstances.ResourceInstance>):Array<Models.ComponentsInstances.ResourceInstance> {

            let componentInstances = new Array<Models.ComponentsInstances.ResourceInstance>();
            if (componentInstanceObj) {
                _.forEach(componentInstanceObj, (instance:Models.ComponentsInstances.ResourceInstance):void => {
                    componentInstances.push(Utils.ComponentInstanceFactory.createComponentInstance(instance));
                });
            }
            return componentInstances;
        };

        static initModules(moduleArrayObj:Array<Models.Module>):Array<Models.Module> {

            let modules = new Array<Models.Module>();

            if (moduleArrayObj) {
                _.forEach(moduleArrayObj, (module:Models.Module):void => {
                    if(module.type === "org.openecomp.groups.VfModule"){
                        modules.push(new Models.Module(module));
                    }
                });
            }
            return modules;
        };
    }
}
