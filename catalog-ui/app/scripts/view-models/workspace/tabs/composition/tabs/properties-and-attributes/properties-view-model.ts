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
/// <reference path="../../../../../../references"/>
module Sdc.ViewModels {
    'use strict';

    interface IResourcePropertiesAndAttributesViewModelScope extends ICompositionViewModelScope {
        properties: Models.PropertiesGroup;
        attributes: Models.AttributesGroup;
        propertiesMessage: string;
        addProperty(): void;
        updateProperty(property:Models.PropertyModel): void;
        deleteProperty(property:Models.PropertyModel): void;
        viewAttribute(attribute:Models.AttributeModel): void;
        groupNameByKey(key:string): string;
        isPropertyOwner():boolean;
    }

    export class ResourcePropertiesViewModel {

        static '$inject' = [
            '$scope',
            '$filter',
            '$modal',
            '$templateCache',
            'ModalsHandler'
        ];


        constructor(private $scope:IResourcePropertiesAndAttributesViewModelScope,
                    private $filter:ng.IFilterService,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService,
                    private ModalsHandler: Utils.ModalsHandler) {

            this.initScope();
        }

        private initComponentProperties = ():void => {
            let result:Models.PropertiesGroup = {};

            if(this.$scope.selectedComponent){
                this.$scope.propertiesMessage = undefined;
                if(this.$scope.isComponentInstanceSelected()){
                    if (this.$scope.currentComponent.selectedInstance.originType==='VF') {
                        // Temporally fix to hide properties for VF (UI stack when there are many properties)
                        this.$scope.propertiesMessage = "Note: properties for VF are disabled";
                    } else {
                        result[this.$scope.currentComponent.selectedInstance.uniqueId] = this.$scope.currentComponent.componentInstancesProperties[this.$scope.currentComponent.selectedInstance.uniqueId];
                    }
                }else if(this.$scope.currentComponent.isService()){
                    // Temporally fix to hide properties for service (UI stack when there are many properties)
                    //result = this.$scope.currentComponent.componentInstancesProperties;
                    this.$scope.propertiesMessage = "Note: properties for service are disabled";
                }else{
                    let key = this.$scope.selectedComponent.uniqueId;
                    result[key]= Array<Models.PropertyModel>();
                    let derived = Array<Models.PropertyModel>();
                    _.forEach(this.$scope.selectedComponent.properties, (property:Models.PropertyModel) => {
                        if(key == property.parentUniqueId){
                            result[key].push(property);
                        }else{
                            property.readonly = true;
                            derived.push(property);
                        }
                    });
                    if(derived.length){
                        result['derived']= derived;
                    }
                }
                this.$scope.properties = result;
            }
        };


        private initComponentAttributes = ():void => {
            let result:Models.AttributesGroup = {};

            if(this.$scope.selectedComponent){
                if(this.$scope.isComponentInstanceSelected()){
                    result[this.$scope.currentComponent.selectedInstance.uniqueId] = this.$scope.currentComponent.componentInstancesAttributes[this.$scope.currentComponent.selectedInstance.uniqueId];
                }else if(this.$scope.currentComponent.isService()){
                    result = this.$scope.currentComponent.componentInstancesAttributes;
                }
                this.$scope.attributes = result;
            }
        };

        private openEditPropertyModal = (property:Models.PropertyModel):void => {
            let viewModelsHtmlBasePath:string = '/app/scripts/view-models/';

            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get(viewModelsHtmlBasePath + 'forms/property-form/property-form-view.html'),
                controller: 'Sdc.ViewModels.PropertyFormViewModel',
                size: 'sdc-l',
                backdrop: 'static',
                keyboard: false,
                resolve: {
                    property: ():Models.PropertyModel => {
                        return property;
                    },
                    component: ():Models.Components.Component => {
                        return this.$scope.currentComponent;
                    },
                    filteredProperties: ():Array<Models.PropertyModel> => {
                        return this.$scope.selectedComponent.properties
                    }
                }
            };


            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            modalInstance
                .result
                .then(():void => {
                  //  this.initComponentProperties();
                });
        };

        private openAttributeModal = (atrribute:Models.AttributeModel):void => {
            let viewModelsHtmlBasePath:string = '/app/scripts/view-models/';

            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get(viewModelsHtmlBasePath + 'forms/attribute-form/attribute-form-view.html'),
                controller: 'Sdc.ViewModels.AttributeFormViewModel',
                size: 'sdc-md',
                backdrop: 'static',
                keyboard: false,
                resolve: {
                    attribute: ():Models.AttributeModel => {
                        return atrribute;
                    },
                    component: ():Models.Components.Component => {
                        return this.$scope.currentComponent;
                    }
                }
            };
            this.$modal.open(modalOptions);
        };




        private initScope = ():void => {
            this.initComponentProperties();
            this.initComponentAttributes();

            this.$scope.$watchCollection('currentComponent.componentInstancesProperties', (newData:any):void => {
                this.initComponentProperties();
            });

            this.$scope.$watchCollection('currentComponent.properties', (newData:any):void => {
                this.initComponentProperties();
            });

            this.$scope.$watch('currentComponent.selectedInstance', (newInstance:Models.ComponentsInstances.ComponentInstance):void => {
                if (angular.isDefined(newInstance)) {
                    this.initComponentProperties();
                    this.initComponentAttributes();
                }
            });

            this.$scope.$watchCollection('currentComponent.componentInstancesAttributes', (newData:any):void => {
                this.initComponentAttributes();
            });

            this.$scope.isPropertyOwner = ():boolean => {
                return this.$scope.currentComponent && this.$scope.currentComponent.isResource() &&
                    !this.$scope.isComponentInstanceSelected();
            };

            this.$scope.addProperty = ():void => {
                let property = new Models.PropertyModel();
                this.openEditPropertyModal(property);
            };

            this.$scope.updateProperty = (property:Models.PropertyModel):void => {
                this.openEditPropertyModal(property);
            };

            this.$scope.deleteProperty = (property:Models.PropertyModel):void => {

                let onOk = ():void => {
                    this.$scope.currentComponent.deleteProperty(property.uniqueId);
                };

                let title:string =  this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TITLE");
                let message:string = this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TEXT", "{'name': '" + property.name + "'}");
                this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
            };

            this.$scope.viewAttribute = (attribute:Models.AttributeModel):void => {
                this.openAttributeModal(attribute);
            };

            this.$scope.groupNameByKey = (key:string):string => {
                switch (key){
                    case 'derived':
                        return "Derived";

                    case this.$scope.currentComponent.uniqueId:
                        return this.$filter("resourceName")(this.$scope.currentComponent.name);

                    default:
                        return this.$filter("resourceName")((_.find(this.$scope.currentComponent.componentInstances, {uniqueId:key})).name);
                }
            };

        }
    }
}
