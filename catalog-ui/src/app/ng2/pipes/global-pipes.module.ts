/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

import {ContentAfterLastDotPipe} from "./contentAfterLastDot.pipe";
import {SearchFilterPipe} from "./searchFilter.pipe";
import {KeysPipe} from "./keys.pipe";
import {GroupByPipe} from "./groupBy.pipe";
import {ResourceNamePipe} from "./resource-name.pipe";
import {NgModule} from "@angular/core";
import {SafeUrlSanitizerPipe} from "./safeUrlSanitizer.pipe";
import {EntityFilterPipe} from "./entity-filter.pipe";
import {KeyValuePipe} from "./key-value.pipe";
import {PropertiesOrderByPipe} from "./properties-order-by.pipe";
import {OrderByPipe} from "./order-by.pipe";

@NgModule({
    declarations: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SafeUrlSanitizerPipe,
        SearchFilterPipe,
        ResourceNamePipe,
        EntityFilterPipe,
        KeyValuePipe,
        PropertiesOrderByPipe,
        OrderByPipe
    ],
    exports: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SafeUrlSanitizerPipe,
        SearchFilterPipe,
        ResourceNamePipe,
        EntityFilterPipe,
        PropertiesOrderByPipe,
        OrderByPipe,
        KeyValuePipe
    ],
    providers: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SafeUrlSanitizerPipe,
        SearchFilterPipe,
        ResourceNamePipe,
        EntityFilterPipe,
        KeyValuePipe
    ]
})

export class GlobalPipesModule {}
