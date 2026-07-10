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
import {FormsModule} from '@angular/forms';
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';
import {ValidationUtils} from 'app/utils';
import {ModulePropertyModalComponent} from './module-property-modal.component';
import {ModulePropertyModalService} from './module-property-modal.service';

/**
 * ValidationUtils is a plain AngularJS service (serviceModule.service('ValidationUtils', ...)), not an @Injectable.
 * The modal injects it by type for getValidationPattern/validateJson/validateIntRange (instance methods that need
 * constructor-injected regex), so bridge it into Angular DI via the '$injector' token — mirroring
 * PropertyFormModalModule.ValidationUtilsProvider and ng2/utils/ng1-upgraded-provider.ts.
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
    declarations: [ModulePropertyModalComponent],
    imports: [CommonModule, FormsModule, TranslateModule],
    entryComponents: [ModulePropertyModalComponent],
    providers: [ModulePropertyModalService, ValidationUtilsProvider],
    exports: [ModulePropertyModalComponent]
})
export class ModulePropertyModalModule {
}
