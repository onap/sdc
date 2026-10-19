/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import 'rxjs/add/observable/of';
import {Observable} from 'rxjs/Observable';
import {InterfaceOperationHandlerComponent} from './interface-operation-handler.component';

describe('InterfaceOperationHandlerComponent interface types', () => {

    const interfaceTypes = {
        'tosca.interfaces.Root': [],
        'tosca.interfaces.node.lifecycle.Standard': ['start', 'create']
    };

    function loadInterfaceTypes(currentInterfaceType: string) {
        const componentService: any = {getInterfaceTypesByModel: jest.fn(() => Observable.of(interfaceTypes))};
        const comp: any = new InterfaceOperationHandlerComponent(null, componentService, null);
        comp.interfaceType = currentInterfaceType;
        comp.operationToUpdate = {};
        comp.interfaceOperationDropDown = {};
        comp.loadInterfaceType();
        return comp;
    }

    it('does not offer an interface type that defines no operations', () => {
        const comp = loadInterfaceTypes(undefined);
        expect(comp.interfaceTypeOptions.map(option => option.value))
            .toEqual(['tosca.interfaces.node.lifecycle.Standard']);
    });

    it('keeps the current interface type even when it defines no operations', () => {
        const comp = loadInterfaceTypes('tosca.interfaces.Root');
        expect(comp.interfaceTypeOptions.map(option => option.value))
            .toEqual(['tosca.interfaces.Root', 'tosca.interfaces.node.lifecycle.Standard']);
        expect(comp.selectedInterfaceType.value).toBe('tosca.interfaces.Root');
    });
});
