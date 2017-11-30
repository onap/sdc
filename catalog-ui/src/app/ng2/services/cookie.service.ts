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

import {Injectable, Inject} from '@angular/core';
import {IAppConfigurtaion, ICookie} from "../../models/app-config";
import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";

@Injectable()
export class Cookie2Service {

    private cookie:ICookie;
    private cookiePrefix:string;

    constructor(@Inject(SdcConfigToken) sdcConfig:ISdcConfig) {
        this.cookie = sdcConfig.cookie;

        this.cookiePrefix = '';
        let junctionName:string = this.getCookieByName(this.cookie.junctionName);
        if ((junctionName !== null) && (junctionName !== '')) {
            this.cookiePrefix = this.cookie.prefix + junctionName + '!';
        }
        console.log("junctionName:" + junctionName);
    }

    private getCookieByName = (cookieName:string):string => {
        cookieName += '=';
        let cookies:Array<string> = document.cookie.split(';');
        let cookieVal:string = '';
        cookies.forEach((cookie:string) => {
            while (cookie.charAt(0) === ' ') {
                cookie = cookie.substring(1);
            }
            if (cookie.indexOf(cookieName) === 0) {
                cookieVal = cookie.substring(cookieName.length, cookie.length);
                return;
            }
        });
        return cookieVal;
    };

    public getUserIdSuffix = ():string => {
        return this.cookie.userIdSuffix;
    };

    public getUserId = ():string => {
        let userIdCookieName:string = this.cookiePrefix + this.cookie.userIdSuffix;
        let userId:string = this.getCookieByName(userIdCookieName);
        return userId;
    };

    public getFirstName = ():string => {
        let firstNameCookieName:string = this.cookiePrefix + this.cookie.userFirstName;
        let firstName:string = this.getCookieByName(firstNameCookieName);
        return firstName;
    };

    public getLastName = ():string => {
        let lastNameCookieName:string = this.cookiePrefix + this.cookie.userLastName;
        let lastName:string = this.getCookieByName(lastNameCookieName);
        return lastName;
    };

    public getEmail = ():string => {
        let emailCookieName:string = this.cookiePrefix + this.cookie.userEmail;
        let email:string = this.getCookieByName(emailCookieName);
        return email;
    }
}
