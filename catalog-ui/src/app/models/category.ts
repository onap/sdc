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

'use strict';



export class ICategoryBase {

    //server properties
    name:string;
    normalizedName:string;
    uniqueId:string;
    icons:Array<string>;

    //custom properties
    filterTerms:string;
    isDisabled:boolean;
    filteredGroup:Array<IGroup>;

    constructor(category?:ICategoryBase) {
        if (category) {
            this.name = category.name;
            this.normalizedName = category.normalizedName;
            this.icons = category.icons;
            this.filterTerms = category.filterTerms;
            this.isDisabled = category.isDisabled;
            this.filteredGroup = category.filteredGroup;
        }
    }
}

export class IMainCategory extends ICategoryBase {
    subcategories:Array<ISubCategory>;

    constructor();
    constructor(category?:IMainCategory) {
        super(category);
        if (category) {
            this.subcategories = category.subcategories;
        }
    }
}

export class ISubCategory extends ICategoryBase {
    groupings:Array<ICategoryBase>;
}

export interface IGroup extends ICategoryBase {
}
