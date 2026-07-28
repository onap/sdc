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

import { downgradeInjectable } from '@angular/upgrade/static';
import {ComponentFactory} from "../utils/component-factory";
import {ChangeLifecycleStateHandler} from "../utils/change-lifecycle-state-handler";
import {ModalsHandler} from "../utils/modals-handler";
import {MenuHandler} from "../utils/menu-handler";

let moduleName:string = 'Sdc.Utils';
let serviceModule:ng.IModule = angular.module(moduleName, []);

// Utils. ComponentInstanceFactory and ServiceCsarReader are intentionally NOT registered here:
// the former is only ever used through its static factory methods, the latter is instantiated
// directly by GeneralTabComponent — neither is resolved by AngularJS DI name anywhere.
serviceModule.factory('ComponentFactory', downgradeInjectable(ComponentFactory));
serviceModule.service('ChangeLifecycleStateHandler', ChangeLifecycleStateHandler);
serviceModule.service('ModalsHandler', ModalsHandler);
serviceModule.service('MenuHandler', MenuHandler);

