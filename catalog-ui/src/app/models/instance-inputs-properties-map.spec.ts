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

import {InputModel} from './inputs';
import {InstancesInputsOrPropertiesMapData, InstancesInputsPropertiesMap} from './instance-inputs-properties-map';
import {PropertyModel} from './properties';

// cleanUnnecessaryDataBeforeSending() builds the POST body for createInputsFromInstancesInputs.
// It used to deep-copy via angular.copy(this, map); it now uses _.cloneDeep(this). The two are
// equivalent ONLY as a whole-object clone: a per-key _.cloneDeep loop would serialize this class's
// two arrow-function fields as {} and ship them in the request body (angular.copy kept them as live
// function references, which JSON.stringify skips). These tests pin the wire payload.
describe('InstancesInputsPropertiesMap', () => {

    const makeInput = (name: string, isAlreadySelected: boolean): InputModel => {
        const input = new InputModel();
        input.name = name;
        input.isAlreadySelected = isAlreadySelected;
        return input;
    };

    const makeProperty = (name: string, isAlreadySelected: boolean): PropertyModel => {
        const property = new PropertyModel();
        property.name = name;
        property.isAlreadySelected = isAlreadySelected;
        return property;
    };

    const makeMap = (): InstancesInputsPropertiesMap => {
        const inputsMap = new InstancesInputsOrPropertiesMapData();
        inputsMap['inst-1'] = [makeInput('keptInput', false), makeInput('selectedInput', true)];
        const propertiesMap = new InstancesInputsOrPropertiesMapData();
        propertiesMap['inst-2'] = [makeProperty('keptProp', false)];
        return new InstancesInputsPropertiesMap(inputsMap, propertiesMap);
    };

    describe('cleanUnnecessaryDataBeforeSending', () => {

        it('drops already-selected inputs and keeps the rest', () => {
            const cleaned = makeMap().cleanUnnecessaryDataBeforeSending();
            expect(cleaned.componentInstanceInputsMap['inst-1'].map((i) => i.name)).toEqual(['keptInput']);
            expect(cleaned.componentInstanceProperties['inst-2'].map((p) => p.name)).toEqual(['keptProp']);
        });

        it('removes an instance key entirely when every entry was already selected', () => {
            const map = makeMap();
            map.componentInstanceInputsMap['inst-1'] = [makeInput('selectedInput', true)];
            const cleaned = map.cleanUnnecessaryDataBeforeSending();
            expect(cleaned.componentInstanceInputsMap['inst-1']).toBeUndefined();
        });

        it('does not mutate the original map', () => {
            const map = makeMap();
            map.cleanUnnecessaryDataBeforeSending();
            expect(map.componentInstanceInputsMap['inst-1'].map((i) => i.name))
                .toEqual(['keptInput', 'selectedInput']);
        });

        // The load-bearing assertion: a per-key clone loop turns the arrow-function fields into
        // enumerable {} and they end up in the JSON the BE receives. Assert on the serialized body,
        // not on the object, because that is the only place the difference is observable.
        it('never serializes its own helper methods into the request body', () => {
            const body = JSON.parse(JSON.stringify(makeMap().cleanUnnecessaryDataBeforeSending()));
            expect(Object.keys(body).sort()).toEqual(['componentInstanceInputsMap', 'componentInstanceProperties']);
        });

        it('returns an object that still has its prototype methods (a live InstancesInputsPropertiesMap)', () => {
            const cleaned = makeMap().cleanUnnecessaryDataBeforeSending();
            expect(typeof cleaned.cleanUnnecessaryDataBeforeSending).toBe('function');
            expect(cleaned instanceof InstancesInputsPropertiesMap).toBe(true);
        });
    });
});
