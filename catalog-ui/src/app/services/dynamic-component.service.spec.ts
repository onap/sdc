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

import {DynamicComponentService} from './dynamic-component.service';

class SomeComponent {
}

describe('DynamicComponentService', () => {
    function setup() {
        const factory = {};
        const componentRef = {};
        const rootResolver: any = {resolveComponentFactory: jest.fn(() => factory)};
        const viewContainerRef: any = {clear: jest.fn(), createComponent: jest.fn(() => componentRef)};
        const applicationRef: any = {components: [{instance: {viewContainerRef}}]};
        const service = new DynamicComponentService(rootResolver, applicationRef);
        return {service, rootResolver, viewContainerRef, factory, componentRef};
    }

    it('resolves through the root resolver and appends to the app root by default', () => {
        const {service, rootResolver, viewContainerRef, factory, componentRef} = setup();

        expect(service.createDynamicComponent(SomeComponent)).toBe(componentRef);

        expect(rootResolver.resolveComponentFactory).toHaveBeenCalledWith(SomeComponent);
        expect(viewContainerRef.clear).toHaveBeenCalled();
        expect(viewContainerRef.createComponent).toHaveBeenCalledWith(factory);
    });

    // A lazy module's entry components are invisible to the root resolver under View Engine.
    it('resolves through a caller-supplied resolver, e.g. one from a lazy-loaded module', () => {
        const {service, rootResolver} = setup();
        const lazyFactory = {};
        const lazyResolver: any = {resolveComponentFactory: jest.fn(() => lazyFactory)};
        const container: any = {clear: jest.fn(), createComponent: jest.fn(() => ({}))};

        service.createDynamicComponent(SomeComponent, container, lazyResolver);

        expect(lazyResolver.resolveComponentFactory).toHaveBeenCalledWith(SomeComponent);
        expect(rootResolver.resolveComponentFactory).not.toHaveBeenCalled();
        expect(container.createComponent).toHaveBeenCalledWith(lazyFactory);
    });

    it('falls back to the root resolver when the resolver argument is undefined', () => {
        const {service, rootResolver} = setup();
        const container: any = {clear: jest.fn(), createComponent: jest.fn(() => ({}))};

        service.createDynamicComponent(SomeComponent, container, undefined);

        expect(rootResolver.resolveComponentFactory).toHaveBeenCalledWith(SomeComponent);
    });
});
