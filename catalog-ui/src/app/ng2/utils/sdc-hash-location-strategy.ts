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

import {HashLocationStrategy} from '@angular/common';
import {Injectable} from '@angular/core';

const NG1_HASH_PREFIX = '!';

/**
 * AngularJS defaulted to the '#!' hash prefix, and every bookmark, Cypress spec and Selenium URL in
 * the tree encodes it. HashLocationStrategy emits a bare '#', so the prefix would silently vanish the
 * moment ng1 stops owning the URL. Reading tolerates both forms so old '#/...' links keep working.
 */
@Injectable()
export class SdcHashLocationStrategy extends HashLocationStrategy {

    public path(includeHash: boolean = false): string {
        const path = super.path(includeHash);
        return path.indexOf(NG1_HASH_PREFIX) === 0 ? path.substring(NG1_HASH_PREFIX.length) : path;
    }

    public prepareExternalUrl(internal: string): string {
        const url = super.prepareExternalUrl(internal);
        // super returns '#' + joinWithSlash(baseHref, internal); re-emit it with the '!' inserted.
        return url.length > 0 ? '#' + NG1_HASH_PREFIX + url.substring(1) : url;
    }
}
