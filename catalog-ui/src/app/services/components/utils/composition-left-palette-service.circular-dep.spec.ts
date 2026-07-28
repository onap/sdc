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

/**
 * Regression guard for the JIT circular-dependency bootstrap failure (failure-catalog §OO).
 *
 * ComponentFactory imports the "app/services" barrel, which re-exports
 * composition-left-palette-service, which imports ComponentFactory back. When the cycle is
 * entered through the barrel, the ComponentFactory binding inside
 * composition-left-palette-service is still undefined at decorator-evaluation time, so
 * LeftPaletteLoaderService's reflected design:paramtypes contains an undefined entry and JIT
 * bootstrap dies with:
 *
 *   Can't resolve all parameters for LeftPaletteLoaderService: ([object Object], [object Object], ?, ...)
 *
 * This is invisible to the sibling spec (which imports the service module directly) and to the
 * AOT production build (static metadata, no runtime reflection), but it kills the app in the
 * browser. The @Inject(forwardRef(() => ComponentFactory)) on the constructor param is the fix.
 *
 * IMPORTANT: the import ORDER below reproduces the cycle and must not be "tidied".
 * "app/utils" must be imported FIRST so component-factory is the module in flight when
 * composition-left-palette-service's decorators are evaluated — that is the direction the app
 * takes (app/utils is pulled in transitively by artifacts.ts and friends). Entering from
 * "app/services" first, or importing './composition-left-palette-service' directly, lets
 * component-factory finish evaluating and the bug does NOT reproduce.
 */
import {Injector, ReflectiveInjector} from '@angular/core';
import {ComponentFactory} from 'app/utils';
import {LeftPaletteLoaderService} from 'app/services';
import {SdcConfigToken} from '../../../ng2/config/sdc-config.config';
import {EventListenerService} from '../../event-listener-service';
import {HttpClient} from '@angular/common/http';

describe('LeftPaletteLoaderService — JIT circular-dep guard (§OO)', () => {

    it('resolves every constructor parameter when reached through the "app/services" barrel', () => {
        const sdcConfig = {
            api: {
                root: 'http://localhost/',
                component_api_root: 'v1/catalog/',
                uicache_root: 'http://localhost/v1/uicache/',
                GET_uicache_left_palette: 'left-palette',
            }
        } as any;

        // ReflectiveInjector performs the same runtime parameter reflection as JIT bootstrap, so a
        // "?" (undefined) paramtype throws here exactly as it does in the browser.
        const injector: Injector = ReflectiveInjector.resolveAndCreate([
            LeftPaletteLoaderService,
            {provide: SdcConfigToken, useValue: sdcConfig},
            {provide: HttpClient, useValue: {get: () => undefined}},
            {provide: ComponentFactory, useValue: {}},
            {provide: EventListenerService, useValue: {notifyObservers: () => undefined}},
        ]);

        const service = injector.get(LeftPaletteLoaderService);
        expect(service).toBeInstanceOf(LeftPaletteLoaderService);
    });
});
