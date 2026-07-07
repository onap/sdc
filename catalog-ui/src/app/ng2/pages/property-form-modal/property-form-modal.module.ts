/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2026 Deutsche Telekom AG
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
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';
import {PropertyTableModule} from 'app/ng2/components/logic/properties-table/property-table.module';
import {ToscaFunctionModule} from '../properties-assignment/tosca-function/tosca-function.module';
import {ConstraintsModule} from '../properties-assignment/constraints/constraints.module';
import {PropertyMetadataModule} from '../properties-assignment/property-metadata/property-metadata.module';
import {ValidationUtils} from 'app/utils';
import {PropertyFormModalComponent} from './property-form-modal.component';
import {PropertyFormModalService} from './property-form-modal.service';

/**
 * ValidationUtils is a plain AngularJS service (serviceModule.service('ValidationUtils', ...)), not
 * an @Injectable. The modal injects it by type for getValidationPattern/validateJson/validateIntRange
 * (instance methods that need constructor-injected regex), so bridge it into Angular DI via the
 * '$injector' token, mirroring ng2/utils/ng1-upgraded-provider.ts and the AdminDashboard module.
 */
export function validationUtilsFactory(injector: any): ValidationUtils {
    return injector.get('ValidationUtils');
}

export const ValidationUtilsProvider = {
    provide: ValidationUtils,
    useFactory: validationUtilsFactory,
    deps: ['$injector']
};

@NgModule({
    declarations: [
        PropertyFormModalComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        PropertyTableModule,
        ToscaFunctionModule,
        ConstraintsModule,
        PropertyMetadataModule
    ],
    entryComponents: [
        PropertyFormModalComponent
    ],
    providers: [
        PropertyFormModalService,
        ValidationUtilsProvider
    ],
    exports: [
        PropertyFormModalComponent
    ]
})
export class PropertyFormModalModule {
}
