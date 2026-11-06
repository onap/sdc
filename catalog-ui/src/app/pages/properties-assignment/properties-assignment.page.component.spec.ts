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
import {ComponentInstance} from 'app/models/components-instances/component-instance';
import {PropertiesAssignmentComponent} from './properties-assignment.page.component';
import {EventListenerService} from 'app/services/event-listener.service';
import {EVENTS, WorkspaceMode} from 'app/utils/constants';

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
            {} as any,                                            // componentService
            {} as any,                                            // componentInstanceService
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

describe('PropertiesAssignmentComponent certify', () => {
    const build = () => {
        const componentModeService = {getComponentMode: jest.fn(() => WorkspaceMode.EDIT)};
        const eventListenerService = new EventListenerService();
        const deps: any[] = new Array(17).fill({});
        deps[7] = {component: {uniqueId: 'vf-1', model: null}};
        deps[9] = componentModeService;
        deps[10] = eventListenerService;
        const comp = new (PropertiesAssignmentComponent as any)(...deps) as PropertiesAssignmentComponent;
        return {comp, componentModeService, eventListenerService};
    };

    it('turns read-only when the component is certified while the tab is open', () => {
        const {comp, componentModeService, eventListenerService} = build();
        expect(comp.isReadonly).toBe(false);
        componentModeService.getComponentMode.mockReturnValue(WorkspaceMode.VIEW);
        eventListenerService.notifyObservers(EVENTS.ON_CERTIFY);
        expect(comp.isReadonly).toBe(true);
    });

    it('stops listening for certify on destroy', () => {
        const {comp, eventListenerService} = build();
        comp.ngOnDestroy();
        expect(eventListenerService.observerCallbacks.containsKey(EVENTS.ON_CERTIFY)).toBe(false);
    });
});

describe('PropertiesAssignmentComponent template', () => {
    // tslint:disable-next-line:no-var-requires
    const template: string = require('./properties-assignment.page.component.html');
    const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

    // The placeholder vanishes once the user types, so it cannot be the only name.
    it('names the search box', () => {
        expect(doc.querySelector('[data-tests-id="search-box"]').getAttribute('aria-label')).toBe('Search properties');
    });

    it('renders Clear All as a native button', () => {
        const clear = doc.querySelector('[data-tests-id="clear-filter-button"]');
        expect(clear.tagName).toBe('BUTTON');
        expect(clear.getAttribute('type')).toBe('button');
        expect(clear.textContent.trim()).toBe('Clear All');
    });

    // The global `.disabled` class only stops the mouse; `disabled` also stops the keyboard.
    it('renders Add Property and Add Input as tertiary buttons with their own test ids', () => {
        const adds = Array.from(doc.querySelectorAll('.right-column > sdc-button.right-column-add'));
        expect(adds.map((el) => el.getAttribute('[text]'))).toEqual([`'Add Property'`, `'Add Input'`]);
        expect(adds.map((el) => el.getAttribute('[testId]'))).toEqual([`'properties-add-button'`, `'inputs-add-button'`]);
        expect(adds.map((el) => el.getAttribute('[disabled]'))).toEqual(['!showAddProperties()', '!isSelf()']);
        adds.forEach((el) => expect(el.getAttribute('[type]')).toBe(`'tertiary'`));
    });
});

describe('PropertiesAssignmentComponent save', () => {

    const respond = (response: any[]) => ({subscribe: (onSuccess: (r: any) => void) => onSuccess(response)});

    // The save pairs each response entry with the changed row it belongs to; SDC-5244 is the backend
    // returning them in another order than they were sent.
    const buildComponent = (services: {topologyTemplateService?: any, componentService?: any}, reset: jest.Mock) => {
        const instance = new PropertiesAssignmentComponent(
            {} as any, {} as any,
            {resetPropertyValue: reset} as any,                   // propertiesUtils
            {resetInputDefaultValue: reset} as any,               // inputsUtils
            services.componentService || {} as any,
            {} as any,
            {filterDataTypesByModel: jest.fn()} as any,
            {component: {uniqueId: 'srv-1', model: null}} as any,
            {push: jest.fn()} as any,
            {getComponentMode: () => null} as any,
            {registerObserverCallback: jest.fn(), notifyObservers: jest.fn()} as any,
            {} as any, {} as any, {} as any,
            services.topologyTemplateService || {} as any,
            {} as any, {} as any,
        );
        instance.isValidChangedData = true;
        instance.updateHasChangedData = jest.fn();
        return instance;
    };

    const changedProperty = (name: string, value: string) => ({name, parentUniqueId: 'srv-1', getJSONValue: () => value});
    const changedInput = (uniqueId: string, value: string) => ({uniqueId, name: uniqueId, getJSONDefaultValue: () => value});

    it('resets each saved service property with its own value when the response is in another order', async () => {
        const reset = jest.fn();
        const component = buildComponent({topologyTemplateService: {updateServiceProperties: () =>
            respond([{name: 'designer', value: 'designer1'}, {name: 'descriptor_id', value: 'descriptor_id1'}])}}, reset);
        component.isPropertiesTabSelected = true;
        component.selectedInstanceData = Object.assign(Object.create(ComponentInstance.prototype), {originType: 'Service'});
        component.isSelf = () => true;
        component.changedData = [changedProperty('descriptor_id', 'descriptor_id1'), changedProperty('designer', 'designer1')] as any;

        await component.saveChangedData();

        expect(reset.mock.calls.map(([prop, value]) => [prop.name, value]))
            .toEqual(expect.arrayContaining([['descriptor_id', 'descriptor_id1'], ['designer', 'designer1']]));
        expect(component.changedData).toEqual([]);
    });

    it('resets each saved input with its own default value when the response is in another order', async () => {
        const reset = jest.fn();
        const component = buildComponent({componentService: {updateComponentInputs: () =>
            respond([{uniqueId: 'in2', defaultValue: 'two', required: false}, {uniqueId: 'in1', defaultValue: 'one', required: true}])}}, reset);
        component.isInputsTabSelected = true;
        const first = changedInput('in1', 'one');
        const second = changedInput('in2', 'two');
        component.changedData = [first, second] as any;

        await component.saveChangedData();

        expect(reset.mock.calls.map(([input, value]) => [input.uniqueId, value]))
            .toEqual(expect.arrayContaining([['in1', 'one'], ['in2', 'two']]));
        expect((first as any).required).toBe(true);
        expect((second as any).required).toBe(false);
    });
});
