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
 * Created by obarda on 6/29/2016.
 */
'use strict';
import {ImagesUrl} from "../../../../utils/constants";
import {Module} from "../../../modules/base-module";
import {CommonNodeBase} from "../base-common-node";
import {AngularJSBridge} from "../../../../services/angular-js-bridge-service";

export interface IModuleNodeBase {
}

export class ModuleNodeBase extends CommonNodeBase implements IModuleNodeBase {

    module:Module;

    constructor(module:Module) {
        super();
        this.module = module;
        this.init();
    }

    private init() {

        this.id = this.module.uniqueId;
        this.name = this.module.name;
        this.displayName = this.module.name;
        this.isGroup = true;
        this.img = AngularJSBridge.getAngularConfig().imagesPath + ImagesUrl.MODULE_ICON;
        this.classes = "module-node";

    }
}
