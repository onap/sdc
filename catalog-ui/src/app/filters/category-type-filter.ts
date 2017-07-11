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

import {ComponentType} from "../utils/constants";
import {CacheService} from "../services/cache-service";
export class CategoryTypeFilter {

    static $inject = ['Sdc.Services.CacheService'];

    constructor(cacheService:CacheService) {
        let filter = <CategoryTypeFilter>(categories:any, selectedType:Array<string>, selectedSubResourceTypes:Array<string>) => {

            if (selectedType.indexOf(ComponentType.RESOURCE) === -1 && selectedSubResourceTypes.length > 0) {
                selectedType = selectedType.concat([ComponentType.RESOURCE]);
            }

            if (!selectedType.length)
                return categories;

            let filteredCategories:any = [];
            selectedType.forEach((type:string) => {
                filteredCategories = filteredCategories.concat(cacheService.get(type.toLowerCase() + 'Categories'));
            });

            return _.filter(categories, function (category:any) {
                return filteredCategories.indexOf(category) != -1;
            });
        };
        return filter;
    }
}
