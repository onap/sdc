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

import { Injectable } from '@angular/core';
import {ButtonModel} from '../models/button';
import {Component} from '../models/components/component';
import {ModalModel} from '../models/modal';
import {DisplayModule} from '../models/modules/base-module';
import {PropertyModel} from '../models/properties';
import {InputFEModel} from '../models/properties-inputs/input-fe-model';
import { ComponentMetadata } from '../models/component-metadata';
import { ModalService } from 'app/services/modal.service';
import { PropertyFormModalComponent } from 'app/pages/property-form-modal/property-form-modal.component';
import { IconsModalComponent } from 'app/components/modals/icons-modal/icons-modal.component';
import { ModulePropertyModalComponent } from 'app/pages/module-property-modal/module-property-modal.component';

export interface IModalsHandler {

    openEditPropertyModal(property: PropertyModel, component: Component, filteredProperties: PropertyModel[], isPropertyOwnValue: boolean,
                          propertyOwnerType: string, propertyOwnerId: string, isViewOnly?: boolean): Promise<any>;
}

@Injectable()
export class ModalsHandler implements IModalsHandler {

    constructor(private modalService: ModalService) {
    }

    openUpdateIconModal = (component: Component): Promise<any> => {
        return new Promise<any>((resolve) => {
            let dyn: IconsModalComponent;

            const okBtn = new ButtonModel('OK', 'blue', () => {
                const isDirty: boolean = dyn.updateIcon();
                resolve(isDirty);
                this.modalService.closeCurrentModal();
            });
            const cancelBtn = new ButtonModel('Cancel', 'grey', () => {
                resolve(undefined);
                this.modalService.closeCurrentModal();
            });

            const modal = this.modalService.createCustomModal(
                new ModalModel('l', 'Choose Icon', null, [okBtn, cancelBtn], 'standard'));
            // addDynamicContentToModalAndBindInputs sets instance[key]=value, i.e. instance.component — the field
            // IconsModalComponent reads. (addDynamicContentToModal would set instance.input instead, leaving
            // this.component undefined and crashing initIcons.)
            this.modalService.addDynamicContentToModalAndBindInputs(modal, IconsModalComponent, {component});
            dyn = modal.instance.dynamicContent.instance as IconsModalComponent;
            modal.instance.open();
        });
    }

    /**
     *
     * This function openes up the edit property modal
     *
     * @param property - the property to edit
     * @param component - the component who is the owner of the property
     * @param filteredProperties - the filtered properties list to scroll between in the edit modal
     * @param isPropertyValueOwner - boolean telling if the component is eligible of editing the property
     * @returns {Promise<T>} - Promise telling if the modal has opened or not
     */
    openEditPropertyModal = (property: PropertyModel, component: Component | ComponentMetadata, filteredProperties: PropertyModel[],
                             isPropertyValueOwner: boolean, propertyOwnerType: string, propertyOwnerId: string, isViewOnly: boolean = false): Promise<any> => {
        return new Promise<any>((resolve) => {
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
                    resolve(saved as any);
                    this.modalService.closeCurrentModal();
                });
            }, () => !dyn.isValid());
            const cancelBtn = new ButtonModel('Cancel', 'grey', () => {
                resolve(undefined);
                this.modalService.closeCurrentModal();
            });

            const modal = this.modalService.createCustomModal(
                new ModalModel('l', (isNew ? 'Add' : 'Update') + ' Property', null, [okBtn, cancelBtn], 'standard'));
            this.modalService.addDynamicContentToModal(modal, PropertyFormModalComponent, inputs);
            dyn = modal.instance.dynamicContent.instance as PropertyFormModalComponent;
            // Resolve (so the caller's .then(reloadProperties) runs) and close the modal on an
            // in-modal delete-success. Mirrors the Cancel button; the component invokes this from deleteCurrent().
            dyn.deleteCallback = () => {
                resolve(undefined);
                this.modalService.closeCurrentModal();
            };
            modal.instance.open();
        });
    }

    /**
     *
     * This function openes up the edit property modal
     *
     * @param property - the property to edit
     * @param filteredProperties - the filtered properties list to scroll between in the edit modal
     * @param isPropertyValueOwner - boolean telling if the component is eligible of editing the property
     * @returns {Promise<T>} - Promise telling if the modal has opened or not
     */
    newOpenEditPropertyModal = (property: PropertyModel, filteredProperties: PropertyModel[], isPropertyValueOwner: boolean, propertyOwnerType: string, propertyOwnerId: string, component: Component, inputProperty: InputFEModel): Promise<any> => {
        return new Promise<any>((resolve) => {
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
                    resolve(saved as any);
                    this.modalService.closeCurrentModal();
                });
            }, () => !dyn.isValid());
            const cancelBtn = new ButtonModel('Cancel', 'grey', () => {
                resolve(undefined);
                this.modalService.closeCurrentModal();
            });

            const modal = this.modalService.createCustomModal(
                new ModalModel('l', (isNew ? 'Add' : 'Update') + ' Property', null, [okBtn, cancelBtn], 'standard'));
            this.modalService.addDynamicContentToModal(modal, PropertyFormModalComponent, inputs);
            dyn = modal.instance.dynamicContent.instance as PropertyFormModalComponent;
            // Resolve (so the caller's .then(reloadProperties) runs) and close the modal on an
            // in-modal delete-success. Mirrors the Cancel button; the component invokes this from deleteCurrent().
            dyn.deleteCallback = () => {
                resolve(undefined);
                this.modalService.closeCurrentModal();
            };
            modal.instance.open();
        });
    }

    openEditModulePropertyModal = (property: PropertyModel, component: Component, selectedModule: DisplayModule, filteredProperties: PropertyModel[]): Promise<any> => {
        return new Promise<any>((resolve) => {
            let dyn: ModulePropertyModalComponent;

            const okBtn = new ButtonModel('Save', 'blue', () => {
                dyn.save().subscribe((saved) => {
                    resolve(saved as any);
                    this.modalService.closeCurrentModal();
                });
            }, () => !dyn.isValid());
            const cancelBtn = new ButtonModel('Cancel', 'grey', () => {
                resolve(undefined);
                this.modalService.closeCurrentModal();
            });

            const modal = this.modalService.createCustomModal(
                new ModalModel('l', 'Update Property', null, [okBtn, cancelBtn], 'standard'));
            this.modalService.addDynamicContentToModalAndBindInputs(modal, ModulePropertyModalComponent, {
                input: {property, component, selectedModule, filteredProperties}
            });
            dyn = modal.instance.dynamicContent.instance as ModulePropertyModalComponent;
            modal.instance.open();
        });
    }

}
