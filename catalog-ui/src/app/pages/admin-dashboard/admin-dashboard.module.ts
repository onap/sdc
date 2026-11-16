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
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {TranslateModule} from 'app/shared/translator/translate.module';

import {AdminDashboardComponent} from './admin-dashboard.component';
import {UserManagementComponent} from './user-management/user-management.component';
import {CategoryManagementComponent} from './category-management/category-management.component';
import {AddCategoryModalComponent} from './add-category-modal/add-category-modal.component';

/**
 * Lazy-loaded by the 'adminDashboard' route in app.routes.ts, which keeps the AuthGuard and the
 * route data; nothing may import this module eagerly.
 *
 * Every service these components use is root-provided, so the module provides none: a provider
 * here would create a second instance in the lazy module's injector. For the same reason it does
 * not import SdcUiComponentsModule — no template here uses it, and the onap-ui-angular modules
 * provide their own ModalService, NotificationsService and LoaderService.
 *
 * AddCategoryModalComponent is created through ModalService, which must be handed
 * CategoryManagementComponent's resolver (see DynamicComponentService.createDynamicComponent).
 */
@NgModule({
    declarations: [
        AdminDashboardComponent,
        UserManagementComponent,
        CategoryManagementComponent,
        AddCategoryModalComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        RouterModule.forChild([{path: '', component: AdminDashboardComponent}])
    ],
    entryComponents: [AddCategoryModalComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AdminDashboardModule {}
