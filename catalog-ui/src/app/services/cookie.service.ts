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

import { Inject, Injectable } from '@angular/core';
import { ISdcConfig, SdcConfigToken } from '../config/sdc-config.config';
import { ICookie } from '../models/app-config';

@Injectable()
export class CookieService {

    private cookie: ICookie;
    private cookiePrefix: string;

    constructor(@Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.cookie = sdcConfig.cookie;

        this.cookiePrefix = '';
        const junctionName: string = this.getCookieByName(this.cookie.junctionName);
        if ((junctionName !== null) && (junctionName !== '')) {
            this.cookiePrefix = this.cookie.prefix + junctionName + '!';
        }
    }

    public getUserIdSuffix = (): string => {
        return this.cookie.userIdSuffix;
    }

    public getUserId = (): string => {
        const userIdCookieName: string = this.cookiePrefix + this.cookie.userIdSuffix;
        const userId: string = this.getCookieByName(userIdCookieName);
        return userId;
    }

    public getFirstName = (): string => {
        const firstNameCookieName: string = this.cookiePrefix + this.cookie.userFirstName;
        const firstName: string = this.getCookieByName(firstNameCookieName);
        return firstName;
    }

    public getLastName = (): string => {
        const lastNameCookieName: string = this.cookiePrefix + this.cookie.userLastName;
        const lastName: string = this.getCookieByName(lastNameCookieName);
        return lastName;
    }

    public getEmail = (): string => {
        const emailCookieName: string = this.cookiePrefix + this.cookie.userEmail;
        const email: string = this.getCookieByName(emailCookieName);
        return email;
    }

    private getCookieByName = (cookieName: string): string => {
        cookieName += '=';
        const cookies: string[] = document.cookie.split(';');
        let cookieVal: string = '';
        cookies.forEach((cookie: string) => {
            while (cookie.charAt(0) === ' ') {
                cookie = cookie.substring(1);
            }
            if (cookie.indexOf(cookieName) === 0) {
                cookieVal = cookie.substring(cookieName.length, cookie.length);
                return;
            }
        });
        return cookieVal;
    }
}
