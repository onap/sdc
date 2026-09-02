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
import {ModulePropertyModalService} from './module-property-modal.service';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';

describe('ModulePropertyModalService', () => {
    let service: ModulePropertyModalService;
    let ciServiceMock: any;

    beforeEach(() => {
        ciServiceMock = {
            updateComponentGroupInstanceProperties: jest.fn(() => Observable.of([{name: 'p', value: '1'}])),
            updateGroupInstanceProperties: jest.fn(() => Observable.of([{name: 'p', value: '1'}]))
        };
        service = new ModulePropertyModalService(ciServiceMock);
    });

    it('routes a RESOURCE save to updateComponentGroupInstanceProperties with the module uniqueId', (done) => {
        const component: any = {isResource: () => true, isService: () => false, componentType: 'RESOURCE', uniqueId: 'vf1'};
        const selectedModule: any = {uniqueId: 'g1'};
        const property: any = {name: 'initial_count', value: '2'};
        service.save(component, selectedModule, property).subscribe(() => {
            expect(ciServiceMock.updateComponentGroupInstanceProperties)
                .toHaveBeenCalledWith('RESOURCE', 'vf1', 'g1', [property]);
            expect(ciServiceMock.updateGroupInstanceProperties).not.toHaveBeenCalled();
            done();
        });
    });

    it('routes a SERVICE save to updateGroupInstanceProperties with the resolved resource-instance id', (done) => {
        const component: any = {
            isResource: () => false, isService: () => true, componentType: 'SERVICE', uniqueId: 'svc1',
            componentInstances: [{uniqueId: 'ri1', groupInstances: [{uniqueId: 'gi1'}]}]
        };
        const selectedModule: any = {groupInstanceUniqueId: 'gi1'};
        const property: any = {name: 'max_vf_module_instances', value: '5'};
        service.save(component, selectedModule, property).subscribe(() => {
            expect(ciServiceMock.updateGroupInstanceProperties)
                .toHaveBeenCalledWith('SERVICE', 'svc1', 'ri1', 'gi1', [property]);
            expect(ciServiceMock.updateComponentGroupInstanceProperties).not.toHaveBeenCalled();
            done();
        });
    });

    it('merges the saved property back into selectedModule.properties so the hierarchy list is not stale', (done) => {
        ciServiceMock.updateComponentGroupInstanceProperties = jest.fn(() =>
            Observable.of([{uniqueId: 'p1', value: '7'}]));
        const component: any = {isResource: () => true, isService: () => false, componentType: 'RESOURCE', uniqueId: 'vf1'};
        const staleProperty: any = {uniqueId: 'p1', name: 'initial_count', value: '2'};
        const selectedModule: any = {uniqueId: 'g1', properties: [staleProperty]};
        const editedCopy: any = {uniqueId: 'p1', name: 'initial_count', value: '7'};
        service.save(component, selectedModule, editedCopy).subscribe(() => {
            expect(selectedModule.properties[0].value).toBe('7');
            done();
        });
    });
});
