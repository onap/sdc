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

import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';
import {LayoutModule} from 'app/ng2/components/layout/layout.module';

import {Error403PageComponent} from './error-403/error-403.component';
import {OnboardVendorPageComponent} from './onboard-vendor/onboard-vendor.component';

/**
 * StandaloneStatesModule — declares the two Angular components that replace
 * the AngularJS error-403 and onboard-vendor ui-router states (Phase 8
 * migration). Both are exposed as downgraded AngularJS directives via
 * downgradeComponent in directive-module.ts so the existing ui-router state
 * definitions in app.ts can render them without modification (Task 4).
 *
 * SdcConfigToken is NOT provided here — it is already root-provided via
 * SdcConfig in AppModule.providers, so OnboardVendorPageComponent receives
 * it from the root injector.
 */
@NgModule({
    declarations: [
        Error403PageComponent,
        OnboardVendorPageComponent
    ],
    imports: [
        CommonModule,
        TranslateModule,
        LayoutModule
    ],
    exports: [
        Error403PageComponent,
        OnboardVendorPageComponent
    ],
    entryComponents: [
        Error403PageComponent,
        OnboardVendorPageComponent
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class StandaloneStatesModule {}
