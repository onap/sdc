/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2026 Deutsche Telekom AG.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {ButtonModel} from 'app/models/button';
import {ModalModel} from 'app/models/modal';
import {PropertyBEModel} from 'app/models/properties-inputs/property-be-model';
import {PropertiesAssignmentComponent} from './properties-assignment.page.component';

describe('PropertiesAssignmentComponent', () => {

    const componentUniqueId = 'srv-1';

    let capturedModal: ModalModel;
    let modalInstance: any;
    let createdProperty: PropertyBEModel;
    let notifications: any[];

    // The Save button of the Add-Property modal, whose callback is what SDC-4855 is about.
    const saveButton = (): ButtonModel => capturedModal.buttons.find((button) => button.text === 'Save');

    const buildComponent = (): PropertiesAssignmentComponent => {
        capturedModal = null;
        createdProperty = null;
        notifications = [];
        modalInstance = {
            dynamicContent: {instance: {isLoading: false, propertyModel: new PropertyBEModel()}},
            close: jest.fn(),
            open: jest.fn(),
        };

        const modalServiceMock = {
            createCustomModal: (modalModel: ModalModel) => {
                capturedModal = modalModel;
                return {instance: modalInstance};
            },
            addDynamicContentToModal: jest.fn(),
        };
        const topologyTemplateServiceMock = {
            createServiceProperty: (uniqueId: string, property: PropertyBEModel) => ({
                subscribe: (onSuccess: (response: any) => void) => onSuccess(createdProperty),
            }),
        };
        const propertiesUtilsMock = {
            convertAddPropertyBAToPropertyFE: (property: PropertyBEModel) => property,
        };
        const eventListenerServiceMock = {
            registerObserverCallback: jest.fn(),
            notifyObservers: jest.fn(),
        };

        const instance = new PropertiesAssignmentComponent(
            {} as any,                                            // propertiesService
            {} as any,                                            // hierarchyNavService
            propertiesUtilsMock as any,                           // propertiesUtils
            {} as any,                                            // inputsUtils
            {} as any,                                            // componentServiceNg2
            {} as any,                                            // componentInstanceServiceNg2
            {filterDataTypesByModel: jest.fn()} as any,           // propertyCreatorComponent
            {component: {uniqueId: componentUniqueId, model: null}} as any, // workspaceService
            {push: (settings: any) => notifications.push(settings)} as any, // notificationsService
            {getComponentMode: () => null} as any,                // componentModeService
            eventListenerServiceMock as any,                      // eventListenerService
            {} as any,                                            // ModalServiceSdcUI
            modalServiceMock as any,                              // modalService
            {} as any,                                            // keysPipe
            topologyTemplateServiceMock as any,                   // topologyTemplateService
            {} as any,                                            // translateService
            {} as any,                                            // service
        );
        return instance;
    };

    // SDC-4855: an aborted property conversion (or a component whose own property list came back
    // empty) leaves instanceFePropertiesMap without an entry for the component's uniqueId. Appending
    // straight into it threw "Cannot read properties of undefined (reading 'push')" AFTER the BE had
    // already persisted the property, so the modal stayed open on an error the user could not act on.
    it('adds the created property when the map has no entry for the component yet', () => {
        const component = buildComponent();
        createdProperty = new PropertyBEModel({name: 'my_prop', type: 'string'} as any);
        expect(component.instanceFePropertiesMap[componentUniqueId]).toBeUndefined();

        component.addProperty(null);
        expect(() => saveButton().callback()).not.toThrow();

        expect(component.instanceFePropertiesMap[componentUniqueId].map((p) => p.name)).toEqual(['my_prop']);
        expect(modalInstance.close).toHaveBeenCalled();
        expect(notifications).toEqual([]);
    });

    it('appends to the existing list when the component already has properties', () => {
        const component = buildComponent();
        component.instanceFePropertiesMap[componentUniqueId] = [new PropertyBEModel({name: 'existing'} as any)] as any;
        createdProperty = new PropertyBEModel({name: 'my_prop', type: 'string'} as any);

        component.addProperty(null);
        saveButton().callback();

        expect(component.instanceFePropertiesMap[componentUniqueId].map((p) => p.name)).toEqual(['existing', 'my_prop']);
    });
});
