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

import {ComponentFactory} from './component-factory';
import {ComponentType} from './constants';

describe('ComponentFactory', () => {
    let factory: ComponentFactory;

    beforeEach(() => {
        // ResourceService / ServiceService test doubles. They carry a NON-enumerable http property
        // to mimic the shape produced by Tasks 1 & 2 (§SS fix). The factory itself never reads http,
        // but the double faithfully represents the runtime object it would receive.
        const mkSvc = () => {
            const s: any = {};
            Object.defineProperty(s, 'http', {value: {}, enumerable: false});
            return s;
        };
        factory = new ComponentFactory(
            mkSvc(),                        // ResourceService double
            mkSvc(),                        // ServiceService double
            {get: () => []} as any,         // CacheService double
            {} as any                       // ComponentServiceNg2 double
        );
    });

    it('is created with no static $inject property', () => {
        // Regression guard: AngularJS-style $inject must be removed in the @Injectable migration
        expect((ComponentFactory as any)['$inject']).toBeUndefined();
    });

    it('createEmptyComponent(RESOURCE) returns a component whose componentType is RESOURCE', () => {
        // new Resource(service) with no data arg skips the deep-copy branch in the
        // Component base constructor (the `component?` guard).
        const c = factory.createEmptyComponent(ComponentType.RESOURCE);
        expect(c.componentType).toBe(ComponentType.RESOURCE);
    });

    it('createEmptyComponent(SERVICE) returns a component whose componentType is SERVICE', () => {
        // Likewise: new Service(service) with no data skips the deep copy.
        const c = factory.createEmptyComponent(ComponentType.SERVICE);
        expect(c.componentType).toBe(ComponentType.SERVICE);
    });

    // §SS regression guard: ComponentFactory is NOT stored inside a model class's enumerable graph
    // (unlike ResourceService/ServiceService which ARE held as model.componentService). Therefore
    // ComponentFactory's own private fields do NOT need to be non-enumerable — the factory is never
    // traversed by a deep copy of a model. The §SS guarantee here is indirect: the services the factory
    // HOLDS (ResourceService/ServiceService doubles above) already have non-enumerable http — meaning
    // even IF something deep-copied the factory, it would NOT traverse into service.http.
    //
    // We verify the factory exposes the expected public API surface (all ten methods are present),
    // proving the migration did not accidentally remove any method.
    it('exposes all ten public factory methods', () => {
        const methods = [
            'createComponent', 'createService', 'createResource', 'updateComponentFromCsar',
            'createFromCsarComponent', 'createEmptyComponent', 'getComponentFromServer',
            'createComponentOnServer', 'importComponentOnServer', 'getComponentWithMetadataFromServer',
        ];
        for (const m of methods) {
            expect(typeof (factory as any)[m]).toBe('function');
        }
    });
});
