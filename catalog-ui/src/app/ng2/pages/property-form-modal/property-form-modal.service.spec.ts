/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2026 Deutsche Telekom AG
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
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import {PropertyModel, InputFEModel} from 'app/models';
import {PropertyFormModalService, PropertyFormModalSaveContext} from './property-form-modal.service';

function makeProperty(overrides: any = {}): PropertyModel {
    return new PropertyModel(Object.assign({
        uniqueId: 'prop-1',
        name: 'myProp',
        type: 'string',
        description: 'a description'
    }, overrides));
}

function makeWorkspaceService(): any {
    return {
        metadata: {
            componentType: 'SERVICE',
            uniqueId: 'comp-1',
            model: null,
            isService: () => false,
            isVfc: () => false
        }
    };
}

function makeCtx(overrides: any = {}): PropertyFormModalSaveContext {
    return Object.assign({
        property: makeProperty(),
        component: {uniqueId: 'comp-1'} as any,
        filteredProperties: [],
        currentPropertyIndex: 0,
        isPropertyValueOwner: false,
        propertyOwnerType: 'component',
        propertyOwnerId: 'owner-1',
        inputProperty: null,
        myValueJson: undefined
    }, overrides);
}

describe('PropertyFormModalService', () => {

    let service: PropertyFormModalService;
    let componentService: any;
    let componentInstanceService: any;
    let topologyTemplateService: any;
    let compositionService: any;
    let workspaceService: any;

    beforeEach(() => {
        componentService = {
            updateComponentInputs: jest.fn(() => Observable.of([]))
        };
        componentInstanceService = {
            updateComponentGroupInstanceProperties: jest.fn(() => Observable.of([makeProperty()])),
            updateComponentPolicyInstanceProperties: jest.fn(() => Observable.of([makeProperty()])),
            updateInstanceProperties: jest.fn(() => Observable.of([makeProperty()]))
        };
        topologyTemplateService = {
            addProperty: jest.fn(() => Observable.of(makeProperty())),
            updateProperty: jest.fn(() => Observable.of(makeProperty())),
            deleteProperty: jest.fn(() => Observable.of(undefined))
        };
        compositionService = {
            componentInstancesProperties: {}
        };
        workspaceService = makeWorkspaceService();
        service = new PropertyFormModalService(
            componentService, componentInstanceService, topologyTemplateService,
            compositionService, workspaceService);
    });

    it('save() routes an input-property to updateComponentInputs', () => {
        const input = new InputFEModel(makeProperty());
        const property = makeProperty({propertyView: true, constraints: [{c: 1}], metadata: {m: 1} as any});
        const ctx = makeCtx({property, inputProperty: input});
        service.save(ctx).subscribe();
        expect(componentService.updateComponentInputs).toHaveBeenCalledTimes(1);
        const args = componentService.updateComponentInputs.mock.calls[0];
        expect(args[0]).toBe(ctx.component);
        // input carries the property's constraints + metadata (old VM lines 359-360)
        expect(args[1][0]).toBe(input);
        expect(input.constraints).toBe(property.constraints);
        expect(input.metadata).toBe(property.metadata);
        // no other BE call fired
        expect(componentInstanceService.updateComponentGroupInstanceProperties).not.toHaveBeenCalled();
    });

    it('save() routes a group owner to updateComponentGroupInstanceProperties', () => {
        const property = makeProperty();
        const ctx = makeCtx({property, propertyOwnerType: 'group', propertyOwnerId: 'grp-1'});
        service.save(ctx).subscribe();
        expect(componentInstanceService.updateComponentGroupInstanceProperties).toHaveBeenCalledTimes(1);
        const args = componentInstanceService.updateComponentGroupInstanceProperties.mock.calls[0];
        expect(args[0]).toBe('SERVICE');   // componentType from workspace metadata
        expect(args[1]).toBe('comp-1');    // uniqueId from workspace metadata
        expect(args[2]).toBe('grp-1');     // propertyOwnerId
        expect(args[3]).toEqual([property]);
    });

    it('save() routes a policy owner to updateComponentPolicyInstanceProperties', () => {
        const property = makeProperty();
        const ctx = makeCtx({property, propertyOwnerType: 'policy', propertyOwnerId: 'pol-1'});
        service.save(ctx).subscribe();
        expect(componentInstanceService.updateComponentPolicyInstanceProperties).toHaveBeenCalledTimes(1);
        const args = componentInstanceService.updateComponentPolicyInstanceProperties.mock.calls[0];
        expect(args[0]).toBe('SERVICE');
        expect(args[1]).toBe('comp-1');
        expect(args[2]).toBe('pol-1');
        expect(args[3]).toEqual([property]);
    });

    it('save() routes an instance value-owner to updateInstanceProperties', () => {
        const property = makeProperty({resourceInstanceUniqueId: 'inst-1'});
        const ctx = makeCtx({property, isPropertyValueOwner: true, propertyOwnerType: 'component'});
        service.save(ctx).subscribe();
        expect(componentInstanceService.updateInstanceProperties).toHaveBeenCalledTimes(1);
        const args = componentInstanceService.updateInstanceProperties.mock.calls[0];
        expect(args[0]).toBe('SERVICE');
        expect(args[1]).toBe('comp-1');
        expect(args[2]).toBe('inst-1');  // resourceInstanceUniqueId
        expect(args[3]).toEqual([property]);
    });

    it('save() routes a plain property to topologyTemplate.addProperty when no uniqueId, updateProperty otherwise', () => {
        // no uniqueId -> addProperty
        const newProp = makeProperty({uniqueId: undefined});
        const filtered: PropertyModel[] = [];
        const addCtx = makeCtx({property: newProp, isPropertyValueOwner: false, propertyOwnerType: 'component', filteredProperties: filtered});
        service.save(addCtx).subscribe();
        expect(topologyTemplateService.addProperty).toHaveBeenCalledTimes(1);
        const addArgs = topologyTemplateService.addProperty.mock.calls[0];
        expect(addArgs[0]).toBe('SERVICE');
        expect(addArgs[1]).toBe('comp-1');
        expect(addArgs[2]).toBe(newProp);
        expect(topologyTemplateService.updateProperty).not.toHaveBeenCalled();

        // with uniqueId -> updateProperty
        const existingProp = makeProperty({uniqueId: 'prop-99'});
        const updateCtx = makeCtx({property: existingProp, isPropertyValueOwner: false, propertyOwnerType: 'component', filteredProperties: [existingProp]});
        service.save(updateCtx).subscribe();
        expect(topologyTemplateService.updateProperty).toHaveBeenCalledTimes(1);
        const updArgs = topologyTemplateService.updateProperty.mock.calls[0];
        expect(updArgs[0]).toBe('SERVICE');
        expect(updArgs[1]).toBe('comp-1');
        expect(updArgs[2]).toBe(existingProp);
    });

    it('deleteProperty() delegates to topologyTemplate.deleteProperty', () => {
        service.deleteProperty('SERVICE', 'comp-1', 'prop-1').subscribe();
        expect(topologyTemplateService.deleteProperty).toHaveBeenCalledWith('SERVICE', 'comp-1', 'prop-1');
    });
});
