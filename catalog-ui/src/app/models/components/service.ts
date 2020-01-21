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
 * Created by obarda on 2/4/2016.
 */
'use strict';
import * as _ from "lodash";
import {IServiceService} from "../../services/components/service-service";
import {Component, PropertyModel, DisplayModule, InputsAndProperties, InputModel, InstancesInputsOrPropertiesMapData, InstancesInputsPropertiesMap,
    Distribution, DistributionComponent, ArtifactGroupModel} from "../../models";
import {ArtifactGroupType} from "../../utils/constants";
import {ComponentMetadata} from "../component-metadata";
import {ForwardingPath} from "app/models/forwarding-path";

export class Service extends Component {

    public serviceApiArtifacts:ArtifactGroupModel;
    public componentService:IServiceService;
    public ecompGeneratedNaming:boolean;
    public namingPolicy:string;
    public serviceType:string;
    public serviceRole:string;
    public serviceFunction:string;
    public environmentContext:string;
    public instantiationType:string;
    public forwardingPaths:{ [key:string]:ForwardingPath } = {};

    constructor(componentService:IServiceService, $q:ng.IQService, component?:Service) {
        super(componentService, $q, component);
        this.ecompGeneratedNaming = true;
        if (component) {
            this.serviceApiArtifacts = new ArtifactGroupModel(component.serviceApiArtifacts);
            this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version;
            this.ecompGeneratedNaming = component.ecompGeneratedNaming;
            this.namingPolicy = component.namingPolicy;
            this.serviceType = component.serviceType;
            this.serviceRole = component.serviceRole;
            this.serviceFunction = component.serviceFunction;
            this.instantiationType = component.instantiationType;
            this.environmentContext = component.environmentContext;
            if (component.categories && component.categories[0]) {
                this.mainCategory = component.categories[0].name;
                this.selectedCategory = this.mainCategory;
            }
        }
        this.componentService = componentService;
        this.iconSprite = "sprite-services-icons";
    }

    public getDistributionsList = ():ng.IPromise<Array<Distribution>> => {
        return this.componentService.getDistributionsList(this.uuid);
    };

    public getDistributionsComponent = (distributionId:string):ng.IPromise<Array<DistributionComponent>> => {
        return this.componentService.getDistributionComponents(distributionId);
    };

    public markAsDeployed = (distributionId:string):ng.IPromise<any> => {
        return this.componentService.markAsDeployed(this.uniqueId, distributionId);
    };

    /* we need to change the name of the input to vfInstanceName + input name before sending to server in order to create the inputs on the service
     *  we also need to remove already selected inputs (the inputs that already create on server, and disabled in the view - but they are selected so they are still in the view model
     */
    public createInputsFormInstances = (instancesInputsMap:InstancesInputsOrPropertiesMapData, instancePropertiesMap:InstancesInputsOrPropertiesMapData):ng.IPromise<Array<InputModel>> => {

        let deferred = this.$q.defer<Array<InputModel>>();
        let onSuccess = (inputsCreated:Array<InputModel>):void => {
            this.inputs = inputsCreated.concat(this.inputs);
            deferred.resolve(inputsCreated);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };

        let propertiesAndInputsMap:InstancesInputsPropertiesMap = new InstancesInputsPropertiesMap(instancesInputsMap, instancePropertiesMap);
        propertiesAndInputsMap = propertiesAndInputsMap.cleanUnnecessaryDataBeforeSending(); // We need to create a copy of the map, without the already selected inputs / properties, and to send the clean map
        this.componentService.createInputsFromInstancesInputs(this.uniqueId, propertiesAndInputsMap).then(onSuccess, onFailed);
        return deferred.promise;
    };

    // we need to change the name of the input to vfInstanceName + input name before sending to server in order to create the inputs on the service
    public getServiceInputInputsAndProperties = (inputId:string):ng.IPromise<InputsAndProperties> => {
        let deferred = this.$q.defer<InputsAndProperties>();
        let onSuccess = (inputsAndProperties:InputsAndProperties):void => {
            let input:InputModel = _.find(this.inputs, (input:InputModel) => {
                return input.uniqueId === inputId;
            });
            input.inputs = inputsAndProperties.inputs;
            input.properties = inputsAndProperties.properties;
            deferred.resolve(inputsAndProperties);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getComponentInputInputsAndProperties(this.uniqueId, inputId).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public deleteServiceInput = (inputId:string):ng.IPromise<InputModel> => {
        let deferred = this.$q.defer<InputModel>();

        let onSuccess = (deletedInput:InputModel):void => {
            delete _.remove(this.inputs, {uniqueId: deletedInput.uniqueId})[0];
            deferred.resolve(deletedInput);
        };

        let onFailed = (error:any):void => {
            deferred.reject(error);
        };

        this.componentService.deleteComponentInput(this.uniqueId, inputId).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public getArtifactsByType = (artifactGroupType:string):ArtifactGroupModel => {
        switch (artifactGroupType) {
            case ArtifactGroupType.DEPLOYMENT:
                return this.deploymentArtifacts;
            case ArtifactGroupType.INFORMATION:
                return this.artifacts;
            case ArtifactGroupType.SERVICE_API:
                return this.serviceApiArtifacts;
        }
    };

    public updateGroupInstanceProperties = (resourceInstanceId:string, group:DisplayModule, properties:Array<PropertyModel>):ng.IPromise<Array<PropertyModel>> => {

        let deferred = this.$q.defer<Array<PropertyModel>>();
        let onSuccess = (updatedProperties:Array<PropertyModel>):void => {
            _.forEach(updatedProperties, (property:PropertyModel) => { // Replace all updated properties on the we needed to update
                _.extend(_.find(group.properties, {uniqueId: property.uniqueId}), property);
            });
            deferred.resolve(updatedProperties);
        };
        let onError = (error:any):void => {
            deferred.reject(error);
        };

        this.componentService.updateGroupInstanceProperties(this.uniqueId, resourceInstanceId, group.groupInstanceUniqueId, properties).then(onSuccess, onError);
        return deferred.promise;
    };

    getTypeUrl():string {
        return 'services/';
    }


    public setComponentMetadata(componentMetadata: ComponentMetadata) {
        super.setComponentMetadata(componentMetadata);
        this.ecompGeneratedNaming = componentMetadata.ecompGeneratedNaming;
        this.namingPolicy = componentMetadata.namingPolicy;
        this.serviceType = componentMetadata.serviceType;
        this.serviceRole = componentMetadata.serviceRole;
        this.serviceFunction = componentMetadata.serviceFunction;
        this.environmentContext = componentMetadata.environmentContext;
        this.instantiationType = componentMetadata.instantiationType;
        this.setComponentDisplayData();
    }

    setComponentDisplayData():void {
        this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version;
        if (this.categories && this.categories[0]) {
            this.mainCategory = this.categories[0].name;
            this.selectedCategory = this.mainCategory;
        }
        this.iconSprite = "sprite-services-icons";
    }

    public toJSON = ():any => {
        let temp = angular.copy(this);
        temp.componentService = undefined;
        temp.filterTerm = undefined;
        temp.iconSprite = undefined;
        temp.mainCategory = undefined;
        temp.subCategory = undefined;
        temp.selectedInstance = undefined;
        temp.showMenu = undefined;
        temp.$q = undefined;
        temp.selectedCategory = undefined;
        temp.modules = undefined;
        temp.groupInstances = undefined;
        temp.policies = undefined;
        return temp;
    };
}

