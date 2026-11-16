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

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TranslateModule} from 'app/shared/translator/translate.module';

import {Error403PageComponent} from './error-403/error-403.component';

/**
 * Declares the page routed to by the 'error-403' route in app.routes.ts. It stays eager: it is
 * where a failed permission check lands, so it must not depend on a chunk download.
 *
 * The glossary and onboardVendor pages are lazy-loaded through GlossaryPageModule and
 * OnboardVendorPageModule.
 */
@NgModule({
    declarations: [Error403PageComponent],
    imports: [
        CommonModule,
        TranslateModule
    ],
    exports: [Error403PageComponent]
})
export class StandalonePagesModule {}
