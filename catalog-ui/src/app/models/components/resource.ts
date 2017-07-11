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
 * Created by obarda on 2/3/2016.
 */
'use strict';
import {InstancesInputsOrPropertiesMapData} from "../instance-inputs-properties-map";
import {PropertyModel} from "../properties";
import {DisplayModule} from "../modules/base-module";
import {InputModel} from "../inputs";
import {ResourceType} from "../../utils/constants";
import {Component} from "./component";
import {FileUploadModel} from "../../directives/file-upload/file-upload";
import {IResourceService} from "../../services/components/resource-service";
import {ComponentMetadata} from "../component-metadata";

export class Resource extends Component {

    public interfaces:any;
    public derivedFrom:Array<string>;
    public componentService:IResourceService;
    public resourceType:string;
    public payloadData:string;
    public payloadName:string;
    public importedFile:FileUploadModel;

    // Onboarding parameters
    public csarUUID:string;
    public csarVersion:string;
    public csarPackageType:string;
    public packageId:string;

    constructor(componentService:IResourceService, $q:ng.IQService, component?:Resource) {
        super(componentService, $q, component);
        if (component) {

            this.interfaces = component.interfaces;
            this.derivedFrom = component.derivedFrom;
            this.payloadData = component.payloadData ? component.payloadData : undefined;
            this.payloadName = component.payloadName ? component.payloadName : undefined;
            this.resourceType = component.resourceType;
            this.csarUUID = component.csarUUID;
            this.csarVersion = component.csarVersion;
            this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version + ' ' + this.resourceType;
            if (component.categories && component.categories[0] && component.categories[0].subcategories && component.categories[0].subcategories[0]) {
                component.mainCategory = component.categories[0].name;
                component.subCategory = component.categories[0].subcategories[0].name;
                this.selectedCategory = component.mainCategory + "_#_" + component.subCategory;
                this.importedFile = component.importedFile;
            }
        } else {
            this.resourceType = ResourceType.VF;
        }

        this.componentService = componentService;
        this.iconSprite = "sprite-resource-icons";
    }

    public setComponentMetadata(componentMetadata: ComponentMetadata) {
        super.setComponentMetadata(componentMetadata);
        this.resourceType = componentMetadata.resourceType;
        this.csarUUID = componentMetadata.csarUUID;
        this.csarVersion = componentMetadata.csarVersion;
        this.derivedFrom = componentMetadata.derivedFrom;
        this.setComponentDisplayData();
    };

    public getComponentSubType = ():string => {
        return this.resourceType;
    };

    public isComplex = ():boolean => {
        return this.resourceType === ResourceType.VF;
    };

    public isVl = ():boolean => {
        return ResourceType.VL == this.resourceType;
    };

    public isCsarComponent = ():boolean => {
        return !!this.csarUUID;
    };

    public createComponentOnServer = ():ng.IPromise<Component> => {
        let deferred = this.$q.defer();
        let onSuccess = (component:Resource):void => {
            this.payloadData = undefined;
            this.payloadName = undefined;
            deferred.resolve(component);
        };
        let onError = (error:any):void => {
            deferred.reject(error);
        };

        this.handleTags();
        if (this.importedFile) {
            this.payloadData = this.importedFile.base64;
            this.payloadName = this.importedFile.filename;
        }
        this.componentService.createComponent(this).then(onSuccess, onError);
        return deferred.promise;
    };


    public updateResourceGroupProperties = (module:DisplayModule, properties:Array<PropertyModel>):ng.IPromise<Array<PropertyModel>> => {
        let deferred = this.$q.defer();
        let onSuccess = (updatedProperties:Array<PropertyModel>):void => {
            _.forEach(updatedProperties, (property:PropertyModel) => { // Replace all updated properties on the module we needed to update
                _.extend(_.find(module.properties, {uniqueId: property.uniqueId}), property);

            });
            //_.extend(_.findWhere(this.groups, {uniqueId: module.uniqueId }), module); // replace the module on the component so all data will be updates if the module sent to the function is a copy
            deferred.resolve(updatedProperties);
        };
        let onError = (error:any):void => {
            deferred.reject(error);
        };

        this.componentService.updateResourceGroupProperties(this.uniqueId, module.uniqueId, properties).then(onSuccess, onError);
        return deferred.promise;
    };

    // For now we only implement the logic in service level
    public createInputsFormInstances = (instanceInputsPropertiesMap:InstancesInputsOrPropertiesMapData):ng.IPromise<Array<InputModel>> => {
        let deferred = this.$q.defer();
        return deferred.promise;
    };

    getTypeUrl():string {
        return 'resources/';
    }


    setComponentDisplayData():void {
        this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version + ' ' + this.resourceType;
        if (this.categories && this.categories[0] && this.categories[0].subcategories && this.categories[0].subcategories[0]) {
            this.mainCategory = this.categories[0].name;
            this.subCategory = this.categories[0].subcategories[0].name;
            this.selectedCategory = this.mainCategory + "_#_" + this.subCategory;
            this.iconSprite = "sprite-resource-icons";
        }
    };

    public toJSON = ():any => {
        this.componentService = undefined;
        this.filterTerm = undefined;
        this.iconSprite = undefined;
        this.mainCategory = undefined;
        this.subCategory = undefined;
        this.selectedInstance = undefined;
        this.showMenu = undefined;
        this.$q = undefined;
        this.selectedCategory = undefined;
        this.importedFile = undefined;
        return this;
    };
}


