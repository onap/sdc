/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import {Injectable} from '@angular/core';

@Injectable()
export class ComponentMetadataService {

    private static readonly SEPARATOR = '_#_';

    // Ported verbatim from GeneralViewModel.calculateUnique (general-view-model.ts:373).
    public calculateUnique(mainCategory: string, subCategory: string): string {
        let uniqueId = mainCategory;
        if (subCategory) {
            uniqueId += ComponentMetadataService.SEPARATOR + subCategory;
        }
        return uniqueId;
    }

    public splitUniqueId(uniqueId: string): {main: string, sub: string} {
        const idx = uniqueId.indexOf(ComponentMetadataService.SEPARATOR);
        if (idx === -1) {
            return {main: uniqueId, sub: ''};
        }
        return {
            main: uniqueId.substring(0, idx),
            sub: uniqueId.substring(idx + ComponentMetadataService.SEPARATOR.length)
        };
    }

    public convertCategoryStringToOneArray(category: string, subcategory: string): any[] {
        const result: any = {name: category};
        if (subcategory) {
            result.subcategories = [{name: subcategory}];
        }
        return [result];
    }
}
