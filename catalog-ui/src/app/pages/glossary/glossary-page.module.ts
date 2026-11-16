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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {LayoutModule} from 'app/components/layout/layout.module';
import {GlossaryPageComponent} from './glossary-page.component';

/**
 * Lazy-loaded by the 'glossary' route in app.routes.ts, which keeps the AuthGuard and the route
 * data. GlossaryService and CacheService are root-provided, so this module provides nothing.
 */
@NgModule({
    declarations: [GlossaryPageComponent],
    imports: [
        CommonModule,
        FormsModule,
        LayoutModule,
        RouterModule.forChild([{path: '', component: GlossaryPageComponent}])
    ]
})
export class GlossaryPageModule {}
