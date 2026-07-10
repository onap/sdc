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
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';
import {AvailableIconsService} from 'app/services';
import {IconsModalComponent} from './icons-modal.component';

/**
 * AvailableIconsService is a plain AngularJS service (serviceModule.service('Sdc.Services.AvailableIconsService', ...)),
 * not an @Injectable. Bridge it into Angular DI via the '$injector' token so IconsModalComponent can inject it by
 * type — mirroring ng2/utils/ng1-upgraded-provider.ts and PropertyFormModalModule.ValidationUtilsProvider.
 */
export function availableIconsServiceFactory(injector: any): AvailableIconsService {
    return injector.get('Sdc.Services.AvailableIconsService');
}

export const AvailableIconsServiceProvider = {
    provide: AvailableIconsService,
    useFactory: availableIconsServiceFactory,
    deps: ['$injector']
};

@NgModule({
    declarations: [IconsModalComponent],
    imports: [CommonModule, TranslateModule],
    entryComponents: [IconsModalComponent],
    providers: [AvailableIconsServiceProvider],
    exports: [IconsModalComponent]
})
export class IconsModalModule {
}
