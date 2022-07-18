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
import * as _ from "lodash";
import {FormState, PROPERTY_DATA, PROPERTY_TYPES, PROPERTY_VALUE_CONSTRAINTS, ValidationUtils} from "app/utils";
import {DataTypesService} from "app/services";
import {DataTypesMap, PropertyModel} from "app/models";
import {ComponentInstance} from "../../../../models/componentsInstances/componentInstance";
import {ComponentInstanceServiceNg2} from "app/ng2/services/component-instance-services/component-instance.service";
import {SdcUiCommon, SdcUiComponents, SdcUiServices} from "onap-ui-angular";
import {CompositionService} from "app/ng2/pages/composition/composition.service";
import {WorkspaceService} from "app/ng2/pages/workspace/workspace.service";
import {Observable} from "rxjs";
import {TopologyTemplateService} from "app/ng2/services/component-services/topology-template.service";
import {InstanceFeDetails} from "../../../../models/instance-fe-details";
import {ToscaGetFunction} from "../../../../models/tosca-get-function";

export interface IEditPropertyModel {
    property:PropertyModel;
    types:Array<string>;
    simpleTypes:Array<string>;
    hasGetFunctionValue: boolean;
    isGetFunctionValid: boolean;
}

interface IPropertyFormViewModelScope extends ng.IScope {
    forms:any;
    editForm:ng.IFormController;
    footerButtons:Array<any>;
    isNew:boolean;
    isLoading:boolean;
    componentMetadata: { isService: boolean, isVfc: boolean }
    validationPattern:RegExp;
    propertyNameValidationPattern:RegExp;
    commentValidationPattern:RegExp;
    editPropertyModel: IEditPropertyModel;
    componentInstanceMap: Map<string, InstanceFeDetails>;
    modalInstanceProperty:ng.ui.bootstrap.IModalServiceInstance;
    currentPropertyIndex:number;
    isLastProperty:boolean;
    myValue:any;
    nonPrimitiveTypes:Array<string>;
    dataTypes:DataTypesMap;
    isTypeDataType:boolean;
    maxLength:number;
    isPropertyValueOwner:boolean;
    isVnfConfiguration:boolean;
    constraints:string[];
    modelNameFilter:string;
    isGetFunctionValueType: boolean;

    validateJson(json:string):boolean;
    save(doNotCloseModal?:boolean):void;
    getValidationPattern(type:string):RegExp;
    validateIntRange(value:string):boolean;
    close():void;
    onValueChange():void;
    onSchemaTypeChange():void;
    onTypeChange(resetSchema:boolean):void;
    showSchema():boolean;
    delete(property:PropertyModel):void;
    getPrev():void;
    getNext():void;
    isSimpleType(typeName:string):boolean;
    getDefaultValue():any;
    onValueTypeChange(): void;
}

export class PropertyFormViewModel {

    static '$inject' = [
        '$scope',
        'Sdc.Services.DataTypesService',
        '$uibModalInstance',
        'property',
        'ValidationPattern',
        'PropertyNameValidationPattern',
        'CommentValidationPattern',
        'ValidationUtils',
        // 'component',
        '$filter',
        'ModalServiceSdcUI',
        'filteredProperties',
        '$timeout',
        'isPropertyValueOwner',
        'propertyOwnerType',
        'propertyOwnerId',
        'ComponentInstanceServiceNg2',
        'TopologyTemplateService',
        'CompositionService',
        'workspaceService'
    ];

    private formState:FormState;

    constructor(private $scope:IPropertyFormViewModelScope,
                private DataTypesService:DataTypesService,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private property:PropertyModel,
                private ValidationPattern:RegExp,
                private PropertyNameValidationPattern:RegExp,
                private CommentValidationPattern:RegExp,
                private ValidationUtils:ValidationUtils,
                // private component:Component,
                private $filter:ng.IFilterService,
                private modalService:SdcUiServices.ModalService,
                private filteredProperties:Array<PropertyModel>,
                private $timeout:ng.ITimeoutService,
                private isPropertyValueOwner:boolean,
                private propertyOwnerType:string,
                private propertyOwnerId:string,
                private ComponentInstanceServiceNg2: ComponentInstanceServiceNg2,
                private topologyTemplateService: TopologyTemplateService,
                private compositionService: CompositionService,
                private workspaceService: WorkspaceService) {

        this.formState = angular.isDefined(property.name) ? FormState.UPDATE : FormState.CREATE;
        this.initScope();
    }

    private initResource = ():void => {
        this.$scope.editPropertyModel.property = new PropertyModel(this.property);
        this.$scope.editPropertyModel.property.type = this.property.type ? this.property.type : null;
        this.$scope.constraints = this.property.constraints && this.property.constraints[0] ? this.property.constraints[0]["validValues"] : null;
        this.initToscaGetFunction();
        this.setMaxLength();
    };

    private initToscaGetFunction() {
        this.$scope.editPropertyModel.hasGetFunctionValue = this.$scope.editPropertyModel.property.isToscaFunction();
        this.$scope.editPropertyModel.isGetFunctionValid = true;
    }

    private initForNotSimpleType = ():void => {
        const property = this.$scope.editPropertyModel.property;
        this.$scope.isTypeDataType = this.DataTypesService.isDataTypeForPropertyType(this.$scope.editPropertyModel.property);
        if (property.isToscaFunction()) {
            this.initValueForGetFunction();
            return;
        }

        if (this.isComplexType(property.type)) {
            if (property.value || property.defaultValue) {
                this.$scope.myValue = JSON.parse(property.value || property.defaultValue);
            } else {
                this.initEmptyComplexValue(property.type);
            }
        }
    };

    private initValueForGetFunction(): void {
        const property = this.$scope.editPropertyModel.property;
        if (property.defaultValue) {
            this.$scope.myValue = JSON.parse(property.defaultValue);
            return;
        }
        if (this.isComplexType(property.type)) {
            this.initEmptyComplexValue(property.type);
            return;
        }

        this.$scope.myValue = undefined;
    }

    private initComponentInstanceMap() {
        this.$scope.componentInstanceMap = new Map<string, InstanceFeDetails>();
        if (this.compositionService.componentInstances) {
            this.compositionService.componentInstances.forEach(value => {
                this.$scope.componentInstanceMap.set(value.uniqueId, <InstanceFeDetails>{
                    name: value.name
                });
            });
        }
    }

    private initEmptyComplexValue(type: string): any {
        switch (type) {
            case PROPERTY_TYPES.MAP:
                this.$scope.myValue = {'': null};
                break;
            case PROPERTY_TYPES.LIST:
                this.$scope.myValue = [];
                break;
            default:
                this.$scope.myValue = {};
        }
    }

    private isComplexType(type: string): boolean {
        if (!type) {
            return false;
        }
        return PROPERTY_DATA.SIMPLE_TYPES.indexOf(type) == -1;
    }

    private setMaxLength = ():void => {
        switch (this.$scope.editPropertyModel.property.type) {
            case PROPERTY_TYPES.MAP:
            case PROPERTY_TYPES.LIST:
                this.$scope.maxLength = this.$scope.editPropertyModel.property.schema.property.type == PROPERTY_TYPES.JSON ?
                    PROPERTY_VALUE_CONSTRAINTS.JSON_MAX_LENGTH :
                    PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;
                break;
            case PROPERTY_TYPES.JSON:
                this.$scope.maxLength = PROPERTY_VALUE_CONSTRAINTS.JSON_MAX_LENGTH;
                break;
            default:
                this.$scope.maxLength =PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;
        }
    };


    private initScope = ():void => {

        //scope properties
        this.$scope.isLoading = true;
        this.$scope.forms = {};
        this.$scope.validationPattern = this.ValidationPattern;
        this.$scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;
        this.$scope.commentValidationPattern = this.CommentValidationPattern;
        this.$scope.isNew = (this.formState === FormState.CREATE);
        this.$scope.componentMetadata = {
            isService: this.workspaceService.metadata.isService(),
            isVfc: this.workspaceService.metadata.isVfc()
        }
        this.$scope.modalInstanceProperty = this.$uibModalInstance;
        this.$scope.currentPropertyIndex = _.findIndex(this.filteredProperties, i=> i.name == this.property.name);
        this.$scope.isLastProperty = this.$scope.currentPropertyIndex == (this.filteredProperties.length - 1);
        const property = new PropertyModel(this.property);
        this.$scope.editPropertyModel = {
            'property': property,
            types: PROPERTY_DATA.TYPES,
            simpleTypes: PROPERTY_DATA.SIMPLE_TYPES,
            hasGetFunctionValue: property.isToscaFunction(),
            isGetFunctionValid: true,
        };
        this.$scope.isPropertyValueOwner = this.isPropertyValueOwner;
        this.$scope.propertyOwnerType = this.propertyOwnerType;
        this.$scope.modelNameFilter = this.workspaceService.metadata.model;
        //check if property of VnfConfiguration
        this.$scope.isVnfConfiguration = false;
        if(this.propertyOwnerType == "component" && angular.isArray(this.compositionService.componentInstances)) {
            const componentPropertyOwner:ComponentInstance = this.compositionService.componentInstances.find((ci:ComponentInstance) => {
                return ci.uniqueId === this.property.resourceInstanceUniqueId;
            });
            if (componentPropertyOwner && componentPropertyOwner.componentName === 'vnfConfiguration') {
                this.$scope.isVnfConfiguration = true;
            }
        }
        this.initResource();
        this.initForNotSimpleType();
        this.initComponentInstanceMap();

        this.$scope.validateJson = (json:string):boolean => {
            if (!json) {
                return true;
            }
            return this.ValidationUtils.validateJson(json);
        };

        this.DataTypesService.fetchDataTypesByModel(this.workspaceService.metadata.model).then(response => {
            this.$scope.dataTypes = response.data as DataTypesMap;
            this.$scope.nonPrimitiveTypes = _.filter(Object.keys(this.$scope.dataTypes), (type:string)=> {
                return this.$scope.editPropertyModel.types.indexOf(type) == -1;
            });

            this.$scope.isLoading = false;
        });

        //scope methods
        this.$scope.save = (doNotCloseModal?:boolean):void => {
            let property:PropertyModel = this.$scope.editPropertyModel.property;
            this.$scope.editPropertyModel.property.description = this.ValidationUtils.stripAndSanitize(this.$scope.editPropertyModel.property.description);
            //if read only - or no changes made - just closes the modal
            //need to check for property.value changes manually to detect if map properties deleted
            if ((this.$scope.editPropertyModel.property.readonly && !this.$scope.isPropertyValueOwner)
                || (!this.$scope.forms.editForm.$dirty && angular.equals(JSON.stringify(this.$scope.myValue), this.$scope.editPropertyModel.property.value))) {
                this.$uibModalInstance.close();
                return;
            }

            this.$scope.isLoading = true;

            let onPropertyFailure = (response):void => {
                console.error('Failed to update property', response);
                this.$scope.isLoading = false;
            };

            let onPropertySuccess = (propertyFromBE:PropertyModel):void => {
                this.$scope.isLoading = false;
                this.filteredProperties[this.$scope.currentPropertyIndex] = propertyFromBE;
                if (!doNotCloseModal) {
                    this.$uibModalInstance.close(propertyFromBE);
                } else {
                    this.$scope.forms.editForm.$setPristine();
                    this.$scope.editPropertyModel.property = new PropertyModel();
                }
            };

            //Not clean, but doing this as a temporary fix until we update the property right panel modals
            if (this.propertyOwnerType === "group"){
                this.ComponentInstanceServiceNg2.updateComponentGroupInstanceProperties(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.propertyOwnerId, [property])
                    .subscribe((propertiesFromBE) => { onPropertySuccess(<PropertyModel>propertiesFromBE[0])}, error => onPropertyFailure(error));
            } else if (this.propertyOwnerType === "policy"){
                if (!this.$scope.editPropertyModel.property.simpleType &&
                    !this.$scope.isSimpleType(this.$scope.editPropertyModel.property.type) &&
                    !_.isNil(this.$scope.myValue)) {
                    property.value = JSON.stringify(this.$scope.myValue);
                }
                this.ComponentInstanceServiceNg2.updateComponentPolicyInstanceProperties(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.propertyOwnerId, [property])
                    .subscribe((propertiesFromBE) => { onPropertySuccess(<PropertyModel>propertiesFromBE[0])}, error => onPropertyFailure(error));
            } else {
                //in case we have uniqueId we call update method
                if (this.$scope.isPropertyValueOwner) {
                    if (!this.$scope.editPropertyModel.property.simpleType && !this.$scope.isSimpleType(property.type)) {
                        property.value = JSON.stringify(this.$scope.myValue);
                    }
                    this.updateInstanceProperties(property.resourceInstanceUniqueId, [property]).subscribe((propertiesFromBE) => onPropertySuccess(propertiesFromBE[0]),
                        error => onPropertyFailure(error));
                } else {
                    if (!this.$scope.editPropertyModel.property.simpleType && !this.$scope.isSimpleType(property.type)) {
                        property.defaultValue = JSON.stringify(this.$scope.myValue);
                    } else {
                        this.$scope.editPropertyModel.property.defaultValue = this.$scope.editPropertyModel.property.value;
                    }
                    this.addOrUpdateProperty(property).subscribe(onPropertySuccess, error => onPropertyFailure(error));
                }
            }
        };

        this.$scope.getPrev = ():void=> {
            this.property = this.filteredProperties[--this.$scope.currentPropertyIndex];
            this.initResource();
            this.initForNotSimpleType();
            this.$scope.isLastProperty = false;
        };

        this.$scope.getNext = ():void=> {
            this.property = this.filteredProperties[++this.$scope.currentPropertyIndex];
            this.initResource();
            this.initForNotSimpleType();
            this.$scope.isLastProperty = this.$scope.currentPropertyIndex == (this.filteredProperties.length - 1);
        };

        this.$scope.isSimpleType = (typeName:string):boolean=> {
            return typeName && this.$scope.editPropertyModel.simpleTypes.indexOf(typeName) != -1;
        };

        this.$scope.showSchema = ():boolean => {
            return [PROPERTY_TYPES.LIST, PROPERTY_TYPES.MAP].indexOf(this.$scope.editPropertyModel.property.type) > -1;
        };

        this.$scope.getValidationPattern = (type:string):RegExp => {
            return this.ValidationUtils.getValidationPattern(type);
        };

        this.$scope.validateIntRange = (value:string):boolean => {
            return !value || this.ValidationUtils.validateIntRange(value);
        };

        this.$scope.close = ():void => {
            this.$uibModalInstance.close();
        };

        // put default value when instance value is empty
        this.$scope.onValueChange = ():void => {
            if (!this.$scope.editPropertyModel.property.value) {
                if (this.$scope.isPropertyValueOwner) {
                    this.$scope.editPropertyModel.property.value = this.$scope.editPropertyModel.property.defaultValue;
                }
            }
        };

        // Add the done button at the footer.
        this.$scope.footerButtons = [
            {'name': 'Save', 'css': 'blue', 'callback': this.$scope.save},
            {'name': 'Cancel', 'css': 'grey', 'callback': this.$scope.close}
        ];

        this.$scope.$watch("forms.editForm.$invalid", (newVal) => {
            if (this.$scope.editPropertyModel.hasGetFunctionValue) {
                this.$scope.footerButtons[0].disabled = newVal || !this.$scope.editPropertyModel.property.toscaFunction;
            } else {
                this.$scope.footerButtons[0].disabled = newVal;
            }
        });

        this.$scope.$watch("forms.editForm.$valid", (newVal) => {
            if (this.$scope.editPropertyModel.hasGetFunctionValue) {
                this.$scope.footerButtons[0].disabled = !newVal || !this.$scope.editPropertyModel.property.toscaFunction;
            } else {
                this.$scope.footerButtons[0].disabled = !newVal;
            }
        });

        this.$scope.getDefaultValue = ():any => {
            return this.$scope.isPropertyValueOwner ? this.$scope.editPropertyModel.property.defaultValue : null;
        };

        this.$scope.onTypeChange = ():void => {
            this.$scope.editPropertyModel.property.value = '';
            this.$scope.editPropertyModel.property.defaultValue = '';
            this.setMaxLength();
            this.initForNotSimpleType();
        };

        this.$scope.onSchemaTypeChange = ():void => {
            if (this.$scope.editPropertyModel.property.type == PROPERTY_TYPES.MAP) {
                this.$scope.myValue = {'': null};
            } else if (this.$scope.editPropertyModel.property.type == PROPERTY_TYPES.LIST) {
                this.$scope.myValue = [];
            }
            this.setMaxLength();
        };

        this.$scope.delete = (property:PropertyModel):void => {
            let onOk: Function = ():void => {
                this.deleteProperty(property.uniqueId).subscribe(
                    this.$scope.close
                );
            };
            let title:string = this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TITLE");
            let message:string = this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TEXT", "{'name': '" + property.name + "'}");
            const okButton = {testId: "OK", text: "OK", type: SdcUiCommon.ButtonType.info, callback: onOk, closeModal: true} as SdcUiComponents.ModalButtonComponent;
            this.modalService.openInfoModal(title, message, 'delete-modal', [okButton]);
        };

        this.$scope.onValueTypeChange = (): void => {
            this.setEmptyValue();
            if (this.$scope.editPropertyModel.hasGetFunctionValue) {
                this.$scope.editPropertyModel.isGetFunctionValid = undefined;
            } else {
                this.$scope.editPropertyModel.property.toscaFunction = undefined;
                this.$scope.editPropertyModel.isGetFunctionValid = true;
            }
        }

        this.$scope.onGetFunctionValidFunction = (toscaGetFunction: ToscaGetFunction): void => {
            this.$scope.editPropertyModel.property.toscaFunction = toscaGetFunction;
        }

        this.$scope.onGetFunctionValidityChange = (isValid): void => {
            if (isValid.isValid) {
                this.$scope.editPropertyModel.isGetFunctionValid = true;
                return;
            }
            this.$scope.editPropertyModel.isGetFunctionValid = undefined;
        }

    };

    private setEmptyValue() {
        const property1 = this.$scope.editPropertyModel.property;
        property1.value = undefined;
        if (this.isComplexType(property1.type)) {
            this.initEmptyComplexValue(property1.type);
            return;
        }
        this.$scope.myValue = '';
    }

    private updateInstanceProperties = (componentInstanceId:string, properties:PropertyModel[]):Observable<PropertyModel[]> => {

        return this.ComponentInstanceServiceNg2.updateInstanceProperties(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, componentInstanceId, properties)
            .map(newProperties => {
                newProperties.forEach((newProperty) => {
                    if (!_.isNil(newProperty.path)) {
                        if (newProperty.path[0] === newProperty.resourceInstanceUniqueId) newProperty.path.shift();
                        // find exist instance property in parent component for update the new value ( find bu uniqueId & path)
                        let existProperty: PropertyModel = <PropertyModel>_.find(this.compositionService.componentInstancesProperties[newProperty.resourceInstanceUniqueId], {
                            uniqueId: newProperty.uniqueId,
                            path: newProperty.path
                        });
                        let index = this.compositionService.componentInstancesProperties[newProperty.resourceInstanceUniqueId].indexOf(existProperty);
                        this.compositionService.componentInstancesProperties[newProperty.resourceInstanceUniqueId][index] = newProperty;
                    }
                });
                return newProperties;
            });
    };

    private addOrUpdateProperty = (property: PropertyModel): Observable<PropertyModel> => {
        if (!property.uniqueId) {
            let onSuccess = (newProperty: PropertyModel): PropertyModel => {
                this.filteredProperties.push(newProperty);
                return newProperty;
            };
            return this.topologyTemplateService.addProperty(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, property)
                .map(onSuccess);
        } else {
            let onSuccess = (newProperty: PropertyModel): PropertyModel => {
                // find exist instance property in parent component for update the new value ( find bu uniqueId )
                let existProperty: PropertyModel = <PropertyModel>_.find(this.filteredProperties, {uniqueId: newProperty.uniqueId});
                let propertyIndex = this.filteredProperties.indexOf(existProperty);
                this.filteredProperties[propertyIndex] = newProperty;
                return newProperty;
            };
            return this.topologyTemplateService.updateProperty(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, property).map(onSuccess);
        }
    };

    public deleteProperty = (propertyId:string):Observable<void> => {
        let onSuccess = ():void => {
            console.debug("Property deleted");
            delete _.remove(this.filteredProperties, {uniqueId: propertyId})[0];
        };
        let onFailed = ():void => {
            console.debug("Failed to delete property");
        };
        return this.topologyTemplateService.deleteProperty(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, propertyId).map(onSuccess, onFailed);
    };

}