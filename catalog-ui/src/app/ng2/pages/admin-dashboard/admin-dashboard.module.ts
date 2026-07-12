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
import {SdcUiComponentsModule} from 'onap-ui-angular';
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';

import {AdminDashboardComponent} from './admin-dashboard.component';
import {UserManagementComponent} from './user-management/user-management.component';
import {CategoryManagementComponent} from './category-management/category-management.component';
import {AddCategoryModalComponent} from './add-category-modal/add-category-modal.component';
import {CategoryManagementService} from './services/category-management.service';

// ValidationUtils is now a pure Angular @Injectable provided at the AppModule root, so
// CategoryManagementComponent resolves it by type from the root injector — no local $injector bridge needed.

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
        SdcUiComponentsModule,
        TranslateModule
    ],
    exports: [AdminDashboardComponent],
    entryComponents: [AdminDashboardComponent, AddCategoryModalComponent],
    providers: [CategoryManagementService],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AdminDashboardModule {}
