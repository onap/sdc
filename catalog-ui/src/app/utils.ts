/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Created by ob0695 on 2/23/2017.
 */
export * from './utils/dictionary/dictionary';
export * from './utils/validation-utils'
export * from './utils/component-factory';
export * from './utils/component-instance-factory';
// change-lifecycle-state-handler is deliberately NOT re-exported here. It became @Injectable in
// SDC-4862, so TypeScript now emits a RUNTIME reference to ServiceServiceNg2 for its
// design:paramtypes metadata — where before the type-only import was elided. Re-exporting it made
// this barrel pull service.service.ts in, closing models -> utils -> handler -> service.service ->
// models; whichever module of that ring loaded second got an undefined base class ("Object prototype
// may only be an Object or null"). Both real consumers (app.module, workspace-container) already
// deep-import it, so nothing needs the re-export. Same reasoning as the deep Dictionary import in
// ng2/services/cache.service.ts.
export * from './utils/modals-handler';
export * from './utils/menu-handler';
export * from './utils/constants';
export * from './utils/common-utils';
export * from './utils/service-csar-reader';
