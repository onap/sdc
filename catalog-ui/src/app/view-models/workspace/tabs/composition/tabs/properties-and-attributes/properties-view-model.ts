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
import {
    AttributeModel,
    AttributesGroup,
    Component,
    ComponentInstance,
    PropertyModel,
    PropertiesGroup
} from "app/models";
import {ICompositionViewModelScope} from "../../composition-view-model";
import {ModalsHandler} from "app/utils";
import {ComponentServiceNg2} from "app/ng2/services/component-services/component.service";
import {ComponentGenericResponse} from "app/ng2/services/responses/component-generic-response";

interface IResourcePropertiesAndAttributesViewModelScope extends ICompositionViewModelScope {
    properties:PropertiesGroup;
    attributes:AttributesGroup;
    propertiesMessage:string;
    groupPropertiesByInstance:boolean;
    showGroupsOfInstanceProperties:Array<boolean>;
    addProperty():void;
    updateProperty(property:PropertyModel):void;
    deleteProperty(property:PropertyModel):void;
    viewAttribute(attribute:AttributeModel):void;
    groupNameByKey(key:string):string;
    isPropertyOwner():boolean;
    getComponentInstanceNameFromInstanceByKey(key:string):string;
}

export class ResourcePropertiesViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        '$uibModal',
        'ModalsHandler',
        'ComponentServiceNg2'

    ];


    constructor(private $scope:IResourcePropertiesAndAttributesViewModelScope,
                private $filter:ng.IFilterService,
                private $uibModal:ng.ui.bootstrap.IModalService,
                private ModalsHandler:ModalsHandler,
                private ComponentServiceNg2:ComponentServiceNg2) {

        this.getComponentInstancesPropertiesAndAttributes();
    }

    private initComponentProperties = ():void => {
        let result:PropertiesGroup = {};

        if (this.$scope.selectedComponent) {
            this.$scope.propertiesMessage = undefined;
            this.$scope.groupPropertiesByInstance = false;
            if (this.$scope.isComponentInstanceSelected()) {
                if (this.$scope.currentComponent.selectedInstance.originType === 'VF') {
                    this.$scope.groupPropertiesByInstance = true;
                }
                result[this.$scope.currentComponent.selectedInstance.uniqueId] = this.$scope.currentComponent.componentInstancesProperties[this.$scope.currentComponent.selectedInstance.uniqueId];
            } else if (this.$scope.currentComponent.isService()) {
                // Temporally fix to hide properties for service (UI stack when there are many properties)
                result = this.$scope.currentComponent.componentInstancesProperties;
                this.$scope.propertiesMessage = "Note: properties for service are disabled";
            } else {
                let key = this.$scope.selectedComponent.uniqueId;
                result[key] = Array<PropertyModel>();
                let derived = Array<PropertyModel>();
                _.forEach(this.$scope.selectedComponent.properties, (property:PropertyModel) => {
                    if (key == property.parentUniqueId) {
                        result[key].push(property);
                    } else {
                        property.readonly = true;
                        derived.push(property);
                    }
                });
                if (derived.length) {
                    result['derived'] = derived;
                }
            }
            this.$scope.properties = result;
        }
    };


    private initComponentAttributes = ():void => {
        let result:AttributesGroup = {};

        if (this.$scope.selectedComponent) {
            if (this.$scope.isComponentInstanceSelected()) {
                result[this.$scope.currentComponent.selectedInstance.uniqueId] = this.$scope.currentComponent.componentInstancesAttributes[this.$scope.currentComponent.selectedInstance.uniqueId];
            } else if (this.$scope.currentComponent.isService()) {
                result = this.$scope.currentComponent.componentInstancesAttributes;
            }
            this.$scope.attributes = result;
        }
    };

    /**
     * This function is checking if the component is the value owner of the current property
     * in order to notify the edit property modal which fields to disable
     */
    private isPropertyValueOwner = ():boolean => {
        return this.$scope.currentComponent.isService() || !!this.$scope.currentComponent.selectedInstance;
    };

    /**
     *  The function opens the edit property modal.
     *  It checks if the property is from the VF or from one of it's resource instances and sends the needed property list.
     *  For create property reasons an empty array is transferd
     *
     * @param property the wanted property to edit/create
     */
    private openEditPropertyModal = (property:PropertyModel):void => {
        this.ModalsHandler.openEditPropertyModal(property,
            this.$scope.component,
            (this.$scope.isPropertyOwner() ?
                this.$scope.properties[property.parentUniqueId] :
                this.$scope.properties[property.resourceInstanceUniqueId]) || [],
            this.isPropertyValueOwner()).then((updatedProperty:PropertyModel) => {
               let oldProp = _.find(this.$scope.properties[updatedProperty.resourceInstanceUniqueId], (prop:PropertyModel) => {return prop.uniqueId == updatedProperty.uniqueId;});
            oldProp.value = updatedProperty.value;
        });
    };

    private openAttributeModal = (atrribute:AttributeModel):void => {

        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            template: 'app/view-models/forms/attribute-form/attribute-form-view.html',
            controller: 'Sdc.ViewModels.AttributeFormViewModel',
            size: 'sdc-md',
            backdrop: 'static',
            keyboard: false,
            resolve: {
                attribute: ():AttributeModel => {
                    return atrribute;
                },
                component: ():Component => {
                    return this.$scope.currentComponent;
                }
            }
        };
        this.$uibModal.open(modalOptions);
    };

    private getComponentInstancesPropertiesAndAttributes = () => {

        this.ComponentServiceNg2.getComponentInstanceAttributesAndProperties(this.$scope.currentComponent).subscribe((genericResponse:ComponentGenericResponse) => {
            this.$scope.currentComponent.componentInstancesAttributes = genericResponse.componentInstancesAttributes;
            this.$scope.currentComponent.componentInstancesProperties = genericResponse.componentInstancesProperties;
            this.initScope();
        });
    };

    private initScope = ():void => {


        this.initComponentProperties();
        this.initComponentAttributes();

        this.$scope.$watchCollection('currentComponent.properties', (newData:any):void => {
            this.initComponentProperties();
        });

        this.$scope.$watch('currentComponent.selectedInstance', (newInstance:ComponentInstance):void => {
            if (angular.isDefined(newInstance)) {
                this.initComponentProperties();
                this.initComponentAttributes();

            }
        });

        this.$scope.isPropertyOwner = ():boolean => {
            return this.$scope.currentComponent && this.$scope.currentComponent.isResource() && !this.$scope.isComponentInstanceSelected();
        };

        this.$scope.updateProperty = (property:PropertyModel):void => {
            this.openEditPropertyModal(property);
        };

        this.$scope.deleteProperty = (property:PropertyModel):void => {

            let onOk = ():void => {
                this.$scope.currentComponent.deleteProperty(property.uniqueId);
            };

            let title:string = this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TITLE");
            let message:string = this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TEXT", "{'name': '" + property.name + "'}");
            this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
        };

        this.$scope.viewAttribute = (attribute:AttributeModel):void => {
            this.openAttributeModal(attribute);
        };

        this.$scope.groupNameByKey = (key:string):string => {
            switch (key) {
                case 'derived':
                    return "Derived";

                case this.$scope.currentComponent.uniqueId:
                    return this.$filter("resourceName")(this.$scope.currentComponent.name);

                default:
                    return this.$filter("resourceName")((_.find(this.$scope.currentComponent.componentInstances, {uniqueId: key})).name);
            }
        };

        this.$scope.getComponentInstanceNameFromInstanceByKey = (key:string):string => {
            let instanceName:string = "";
            if (key !== undefined && this.$scope.selectedComponent.uniqueId == this.$scope.currentComponent.selectedInstance.componentUid) {
                instanceName = this.$filter("resourceName")((_.find(this.$scope.selectedComponent.componentInstances, {uniqueId: key})).name);
            }
            return instanceName;
        };

    }
}
