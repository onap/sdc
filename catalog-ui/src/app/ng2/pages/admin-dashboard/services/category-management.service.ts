/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2024 Deutsche Telekom AG. All rights reserved.
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

import {Injectable, Inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {IMainCategory, ISubCategory} from 'app/models/category';
import {SdcConfigToken, ISdcConfig} from 'app/ng2/config/sdc-config.config';
import {HttpHelperService} from 'app/ng2/services/http-hepler.service';

@Injectable()
export class CategoryManagementService {

    constructor(private http: HttpClient,
                @Inject(SdcConfigToken) private sdcConfig: ISdcConfig) {}

    /**
     * Creates a top-level category.
     * POSTs to /v1/category/{type}s/  (no categoryId segment)
     */
    public createCategory(type: string, category: {name: string}): Observable<IMainCategory> {
        const url = this.sdcConfig.api.root + HttpHelperService.replaceUrlParams(
            this.sdcConfig.api.POST_category,
            {types: type + 's'}
        );
        return this.http.post(url, category).map(resp => <IMainCategory>resp);
    }

    /**
     * Creates a subcategory under an existing category.
     * POSTs to /v1/category/{type}s/{categoryId}/subCategory/  (no subCategoryId segment)
     */
    public createSubCategory(type: string, categoryId: string, subCategory: {name: string}): Observable<ISubCategory> {
        const url = this.sdcConfig.api.root + HttpHelperService.replaceUrlParams(
            this.sdcConfig.api.POST_subcategory,
            {types: type + 's', categoryId: categoryId}
        );
        return this.http.post(url, subCategory).map(resp => <ISubCategory>resp);
    }
}
