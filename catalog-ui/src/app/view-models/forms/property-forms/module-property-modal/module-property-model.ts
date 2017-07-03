/**
 * Created by obarda on 1/18/2017.
 */
'use strict';
import {PropertyModel, DisplayModule, Component, Resource, Service, ComponentInstance} from "app/models";
import {UNIQUE_GROUP_PROPERTIES_NAME} from "app/utils";
import {IPropertyFormBaseViewScope, PropertyFormBaseView} from "../base-property-form/property-form-base-model";
import {DataTypesService} from "app/services/data-types-service";

export interface IModulePropertyViewScope extends IPropertyFormBaseViewScope {
    onValueChange():void;
}

export class ModulePropertyView extends PropertyFormBaseView {

    static '$inject' = [
        '$scope',
        '$templateCache',
        '$uibModalInstance',
        '$injector',
        'originalProperty',
        'component',
        'selectedModule',
        'Sdc.Services.DataTypesService',
        '$q'
    ];

    constructor(protected $scope:IModulePropertyViewScope,
                protected $templateCache:ng.ITemplateCacheService,
                protected $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                protected $injector:ng.auto.IInjectorService,
                protected originalProperty:PropertyModel,
                protected component:Component,
                private selectedModule:DisplayModule,
                protected DataTypesService:DataTypesService,
                private $q:ng.IQService) {
        super($scope, $uibModalInstance, $injector, originalProperty, component, selectedModule.properties, DataTypesService);

        this.$templateCache.put("module-property-view.html", require('app/view-models/forms/property-forms/module-property-modal/module-property-view.html'));
        this.$scope.innerViewSrcUrl = "module-property-view.html";
        this.initChildScope();
    }

    private findPropertyByName = (propertyName:string):PropertyModel => {
        let property:PropertyModel = _.find(this.filteredProperties, (property:PropertyModel) => {
            return property.name === propertyName;
        });
        return property;
    };

    save(isNeedToCloseModal):ng.IPromise<boolean> {

        let deferred = this.$q.defer();

        let onSuccess = (properties:Array<PropertyModel>):void => {
            deferred.resolve(true);
            if (isNeedToCloseModal === true) {
                this.$scope.close();
            }
        };

        let onFailed = ():void => {
            deferred.resolve(false);
        };

        let property = _.find(this.selectedModule.properties, (property) => {
            return property.uniqueId === this.$scope.property.uniqueId;
        });
        if (property.value !== this.$scope.property.value) {
            if (this.component.isResource()) {
                (<Resource>this.component).updateResourceGroupProperties(this.selectedModule, [this.$scope.property]).then(onSuccess, onFailed); // for now we only update one property at a time
            }
            if (this.component.isService()) {
                // Find the component instance of the group instance
                let componentInstance:ComponentInstance = _.find(this.component.componentInstances, (componentInstance:ComponentInstance) => {
                    let groupInstance = _.find(componentInstance.groupInstances, {uniqueId: this.selectedModule.groupInstanceUniqueId});
                    return groupInstance !== undefined;

                });
                (<Service>this.component).updateGroupInstanceProperties(componentInstance.uniqueId, this.selectedModule, [this.$scope.property]).then(onSuccess, onFailed); // for now we only update one property at a time
            }
        } else {
            deferred.resolve(true);
        }
        return deferred.promise;
    }

    onPropertyChange():void {
        this.initValidation();
    }

    protected initValidation = ():void => {

        this.$scope.isDeleteDisable = true;
        this.$scope.isNameDisable = true;
        this.$scope.isTypeSelectorDisable = true;
        this.$scope.isDescriptionDisable = true;

        switch (this.$scope.property.name) {
            case UNIQUE_GROUP_PROPERTIES_NAME.IS_BASE:
            case UNIQUE_GROUP_PROPERTIES_NAME.VF_MODULE_TYPE:
            case UNIQUE_GROUP_PROPERTIES_NAME.VOLUME_GROUP:
            case UNIQUE_GROUP_PROPERTIES_NAME.VF_MODULE_LABEL:
                this.$scope.property.readonly = true;
                break;
            case UNIQUE_GROUP_PROPERTIES_NAME.VF_MODULE_DESCRIPTION:
                if (this.component.isService()) {
                    this.$scope.property.readonly = true;
                } else {
                    this.$scope.property.readonly = false;
                }
                break;
        }
    };

    private isUniqueProperty = ():boolean => {
        return this.$scope.property.name === UNIQUE_GROUP_PROPERTIES_NAME.MIN_VF_MODULE_INSTANCES ||
            this.$scope.property.name === UNIQUE_GROUP_PROPERTIES_NAME.MAX_VF_MODULE_INSTANCES ||
            this.$scope.property.name === UNIQUE_GROUP_PROPERTIES_NAME.INITIAL_COUNT;
    };


    private initChildScope = ():void => {

        this.initValidation();

        // put default value when instance value is empty
        this.$scope.onValueChange = ():void => {

            if (!this.$scope.property.value) { // Resetting to default value
                if (this.isPropertyValueOwner()) {
                    if (this.component.isService()) {
                        this.$scope.property.value = this.$scope.property.parentValue;
                    } else {
                        this.$scope.property.value = this.$scope.property.defaultValue;
                    }
                }
            }

            if (this.isUniqueProperty()) {

                let isValid = true;
                let maxProperty:PropertyModel = this.findPropertyByName(UNIQUE_GROUP_PROPERTIES_NAME.MAX_VF_MODULE_INSTANCES);
                let minProperty:PropertyModel = this.findPropertyByName(UNIQUE_GROUP_PROPERTIES_NAME.MIN_VF_MODULE_INSTANCES);
                let initialCountProperty:PropertyModel = this.findPropertyByName(UNIQUE_GROUP_PROPERTIES_NAME.INITIAL_COUNT);

                let maxPropertyValue = parseInt(maxProperty.value);
                let minPropertyValue = parseInt(minProperty.value);
                let initialCountPropertyValue = parseInt(initialCountProperty.value);
                let propertyValue = parseInt(this.$scope.property.value);
                let parentPropertyValue = parseInt(this.$scope.property.parentValue);

                switch (this.$scope.property.name) {

                    case UNIQUE_GROUP_PROPERTIES_NAME.MIN_VF_MODULE_INSTANCES:
                        if (isNaN(maxPropertyValue) || maxPropertyValue == null) {
                            isValid = propertyValue <= initialCountPropertyValue;
                        }
                        else {
                            isValid = propertyValue && (propertyValue <= maxPropertyValue && propertyValue <= initialCountPropertyValue);
                        }
                        this.$scope.forms.editForm["value"].$setValidity('maxValidation', isValid);
                        if (this.component.isService()) {
                            if (isNaN(parentPropertyValue) || parentPropertyValue == null) {
                                isValid = true;
                            } else {
                                isValid = propertyValue >= parentPropertyValue;
                                this.$scope.forms.editForm["value"].$setValidity('minValidationVfLevel', isValid);
                            }
                        }
                        break;
                    case UNIQUE_GROUP_PROPERTIES_NAME.MAX_VF_MODULE_INSTANCES:
                        if (isNaN(minPropertyValue) || minPropertyValue == null) {
                            isValid = propertyValue >= initialCountPropertyValue;
                        } else {
                            isValid = isNaN(propertyValue) || (propertyValue >= minPropertyValue && propertyValue >= initialCountPropertyValue);
                        }
                        this.$scope.forms.editForm["value"].$setValidity('minValidation', isValid);
                        if (this.component.isService()) {
                            if (isNaN(parentPropertyValue) || parentPropertyValue == null) {
                                isValid = true;
                            }
                            else {
                                isValid = propertyValue <= parentPropertyValue;
                                this.$scope.forms.editForm["value"].$setValidity('maxValidationVfLevel', isValid);
                            }
                        }
                        break;
                    case UNIQUE_GROUP_PROPERTIES_NAME.INITIAL_COUNT:
                        if ((isNaN(minPropertyValue) || minPropertyValue == null) && (isNaN(maxPropertyValue) || maxPropertyValue == null)) {
                            isValid = true;
                        } else if (isNaN(minPropertyValue) || minPropertyValue == null) {
                            isValid = propertyValue <= maxPropertyValue;
                        } else if (isNaN(maxPropertyValue) || maxPropertyValue == null) {
                            isValid = propertyValue >= minPropertyValue;
                        } else {
                            isValid = minPropertyValue <= propertyValue && propertyValue <= maxPropertyValue;
                        }
                        this.$scope.forms.editForm["value"].$setValidity('minOrMaxValidation', isValid);
                        break;
                }
            }
            ;
        }
    }
}
