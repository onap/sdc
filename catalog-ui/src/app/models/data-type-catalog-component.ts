/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

'use strict';

import {DataTypeModel} from "./data-types";
import {Icon, ToscaType} from "../utils/constants";
import {Service} from "./components/service";
import {Resource} from "./components/resource";
import {Model} from "./model";

export class DataTypeCatalogComponent {

    public name:string;
    public uniqueId:string;
    public uuid:string;
    public version:string;
    public model:Model;
    public componentType:string;
    public icon:string;
    public iconSprite:string;

    constructor(dataTypeCatalogComponent?: DataTypeModel) {
        this.name = dataTypeCatalogComponent.name;
        this.uniqueId = dataTypeCatalogComponent.uniqueId;
        if (dataTypeCatalogComponent.model) {
            this.model = dataTypeCatalogComponent.model;
        } else {
            this.model = undefined;
        }
        this.componentType = ToscaType.DATATYPE;
        this.icon = Icon.DATATYPE_ICON;
        this.iconSprite = 'sprite-resource-icons';
    }

    public isService = ():boolean => {
        return this instanceof Service;
    }

    public isResource = ():boolean => {
        return this instanceof Resource;
    }

    public getComponentSubType= ():string => {
        return this.componentType;
    }
}

