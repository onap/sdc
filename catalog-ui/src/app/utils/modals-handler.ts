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

import {  ButtonModel, Component, DisplayModule , ModalModel, PropertyModel, InputFEModel } from '../models';
import { ComponentMetadata } from '../models/component-metadata';
import { ModalService } from 'app/ng2/services/modal.service';
import { PropertyFormModalComponent } from 'app/ng2/pages/property-form-modal/property-form-modal.component';

export interface IModalsHandler {

    openEditPropertyModal(property: PropertyModel, component: Component, filteredProperties: PropertyModel[], isPropertyOwnValue: boolean,
                          propertyOwnerType: string, propertyOwnerId: string, isViewOnly?: boolean): ng.IPromise<any>;
}

export class ModalsHandler implements IModalsHandler {

    static '$inject' = [
        '$uibModal',
        '$q',
        'ModalServiceNg2'
    ];

    constructor(private $uibModal: ng.ui.bootstrap.IModalService,
                private $q: ng.IQService,
                private modalServiceNg2: ModalService) {
    }

    openUpdateIconModal = (component: Component): ng.IPromise<any> => {
        const deferred = this.$q.defer();
        const modalOptions: ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/modals/icons-modal/icons-modal-view.html',
            controller: 'Sdc.ViewModels.IconsModalViewModel',
            size: 'sdc-auto',
            backdrop: 'static',
            resolve: {
                component: (): Component => {
                    return component;
                }
            }
        };
        const modalInstance: ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    }

    /**
     *
     * This function openes up the edit property modal
     *
     * @param property - the property to edit
     * @param component - the component who is the owner of the property
     * @param filteredProperties - the filtered properties list to scroll between in the edit modal
     * @param isPropertyValueOwner - boolean telling if the component is eligible of editing the property
     * @returns {IPromise<T>} - Promise telling if the modal has opened or not
     */
    openEditPropertyModal = (property: PropertyModel, component: Component | ComponentMetadata, filteredProperties: PropertyModel[],
                             isPropertyValueOwner: boolean, propertyOwnerType: string, propertyOwnerId: string, isViewOnly: boolean = false): ng.IPromise<any> => {
        const deferred = this.$q.defer();

        const inputs = {
            property,
            component: component as Component,
            filteredProperties,
            isPropertyValueOwner,
            propertyOwnerType,
            propertyOwnerId,
            isViewOnly,
            inputProperty: null as InputFEModel
        };

        const isNew: boolean = !property.name;
        let dyn: PropertyFormModalComponent;

        const okBtn = new ButtonModel('Save', 'blue', () => {
            dyn.save().subscribe((saved) => {
                // save() returns Observable<PropertyModel | void>; the deferred is untyped ($q.defer<any>).
                deferred.resolve(saved as any);
                this.modalServiceNg2.closeCurrentModal();
            });
        }, () => !dyn.isValid());
        const cancelBtn = new ButtonModel('Cancel', 'grey', () => {
            deferred.resolve();
            this.modalServiceNg2.closeCurrentModal();
        });

        const modal = this.modalServiceNg2.createCustomModal(
            new ModalModel('l', (isNew ? 'Add' : 'Update') + ' Property', null, [okBtn, cancelBtn], 'standard'));
        this.modalServiceNg2.addDynamicContentToModal(modal, PropertyFormModalComponent, inputs);
        dyn = modal.instance.dynamicContent.instance as PropertyFormModalComponent;
        // Resolve the deferred (so the caller's .then(reloadProperties) runs) and close the modal on an
        // in-modal delete-success. Mirrors the Cancel button; the component invokes this from deleteCurrent().
        dyn.deleteCallback = () => {
            deferred.resolve();
            this.modalServiceNg2.closeCurrentModal();
        };
        modal.instance.open();

        return deferred.promise;
    }

    /**
     *
     * This function openes up the edit property modal
     *
     * @param property - the property to edit
     * @param filteredProperties - the filtered properties list to scroll between in the edit modal
     * @param isPropertyValueOwner - boolean telling if the component is eligible of editing the property
     * @returns {IPromise<T>} - Promise telling if the modal has opened or not
     */
    newOpenEditPropertyModal = (property: PropertyModel, filteredProperties: PropertyModel[], isPropertyValueOwner: boolean, propertyOwnerType: string, propertyOwnerId: string, component: Component, inputProperty: InputFEModel): ng.IPromise<any> => {
        const deferred = this.$q.defer();

        const inputs = {
            property,
            component: component as Component,
            filteredProperties,
            isPropertyValueOwner,
            propertyOwnerType,
            propertyOwnerId,
            isViewOnly: false,
            inputProperty
        };

        const isNew: boolean = !property.name;
        let dyn: PropertyFormModalComponent;

        const okBtn = new ButtonModel('Save', 'blue', () => {
            dyn.save().subscribe((saved) => {
                // save() returns Observable<PropertyModel | void>; the deferred is untyped ($q.defer<any>).
                deferred.resolve(saved as any);
                this.modalServiceNg2.closeCurrentModal();
            });
        }, () => !dyn.isValid());
        const cancelBtn = new ButtonModel('Cancel', 'grey', () => {
            deferred.resolve();
            this.modalServiceNg2.closeCurrentModal();
        });

        const modal = this.modalServiceNg2.createCustomModal(
            new ModalModel('l', (isNew ? 'Add' : 'Update') + ' Property', null, [okBtn, cancelBtn], 'standard'));
        this.modalServiceNg2.addDynamicContentToModal(modal, PropertyFormModalComponent, inputs);
        dyn = modal.instance.dynamicContent.instance as PropertyFormModalComponent;
        // Resolve the deferred (so the caller's .then(reloadProperties) runs) and close the modal on an
        // in-modal delete-success. Mirrors the Cancel button; the component invokes this from deleteCurrent().
        dyn.deleteCallback = () => {
            deferred.resolve();
            this.modalServiceNg2.closeCurrentModal();
        };
        modal.instance.open();

        return deferred.promise;
    }

    openEditModulePropertyModal = (property: PropertyModel, component: Component, selectedModule: DisplayModule, filteredProperties: PropertyModel[]): ng.IPromise<any> => {
        const deferred = this.$q.defer();

        const modalOptions: ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/forms/property-forms/base-property-form/property-form-base-view.html',
            controller: 'Sdc.ViewModels.ModulePropertyView',
            size: 'sdc-l',
            backdrop: 'static',
            keyboard: false,
            resolve: {
                originalProperty: (): PropertyModel => {
                    return property;
                },
                component: (): Component => {
                    return component as Component;
                },
                selectedModule: (): DisplayModule => {
                    return selectedModule;
                },
                filteredProperties: (): PropertyModel[] => {
                    return filteredProperties;
                }
            }
        };

        const modalInstance: ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    }

}
