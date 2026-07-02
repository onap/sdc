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

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {CookieService} from 'app/services/cookie-service';

@Component({
    selector: 'error-403-page',
    templateUrl: './error-403.component.html',
    styleUrls: ['./error-403.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class Error403PageComponent implements OnInit {

    static readonly ADMIN_EMAIL = 'dl-asdcaccessrequest@att.com';
    static readonly SUBJECT_PREFIX = 'SDC Access Request for';

    mailto: string;

    constructor(private cookieService: CookieService) {}

    ngOnInit(): void {
        const firstName = this.cookieService.getFirstName();
        const lastName = this.cookieService.getLastName();
        const userId = this.cookieService.getUserId();
        const userDetails = `${firstName} ${lastName} (${userId})`;
        this.mailto = Error403PageComponent.ADMIN_EMAIL + '?subject=' + encodeURIComponent(Error403PageComponent.SUBJECT_PREFIX + ' ' + userDetails);
    }
}
