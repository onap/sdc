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

import {ComponentFactory} from "../utils/component-factory";
import {ComponentInstanceFactory} from "../utils/component-instance-factory";
import {ChangeLifecycleStateHandler} from "../utils/change-lifecycle-state-handler";
import {ModalsHandler} from "../utils/modals-handler";
import {MenuHandler} from "../utils/menu-handler";
import {ServiceCsarReader} from "../utils/service-csar-reader";

let moduleName:string = 'Sdc.Utils';
let serviceModule:ng.IModule = angular.module(moduleName, []);

//Utils
serviceModule.service('ComponentFactory', ComponentFactory);
serviceModule.service('ComponentInstanceFactory', ComponentInstanceFactory);
serviceModule.service('ChangeLifecycleStateHandler', ChangeLifecycleStateHandler);
serviceModule.service('ModalsHandler', ModalsHandler);
serviceModule.service('MenuHandler', MenuHandler);
serviceModule.service('ServiceCsarReader', ServiceCsarReader);

