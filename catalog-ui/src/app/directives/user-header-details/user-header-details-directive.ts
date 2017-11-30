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
import {User, IUser, IAppConfigurtaion, IUserProperties} from "app/models";
import { UserService } from "../../ng2/services/user.service";
export interface IUserHeaderDetailsScope extends ng.IScope {
    iconUrl:string;
    user:IUser;
    initUser:Function;
}

export class UserHeaderDetailsDirective implements ng.IDirective {

    constructor(private $http:ng.IHttpService, private sdcConfig:IAppConfigurtaion, private userService:UserService) {
    }

    scope = {
        iconUrl: '=?'
    };

    replace = true;
    restrict = 'E';
    template = ():string => {
        return require('./user-header-details-directive.html');
    };

    link = (scope:IUserHeaderDetailsScope) => {

        scope.initUser = ():void => {
            let defaultUserId:string;
            let userInfo:IUserProperties = this.userService.getLoggedinUser();
            if (!userInfo) {
                defaultUserId = this.$http.defaults.headers.common[this.sdcConfig.cookie.userIdSuffix];
                this.userService.getUser(defaultUserId).subscribe((defaultUserInfo):void => {
                    scope.user = new User(defaultUserInfo);
                });
            } else {
                scope.user = new User(userInfo);
            }
        };
        scope.initUser();
    };

    public static factory = ($http:ng.IHttpService, sdcConfig:IAppConfigurtaion, userService:UserService)=> {
        return new UserHeaderDetailsDirective($http, sdcConfig, userService);
    };

}

UserHeaderDetailsDirective.factory.$inject = ['$http', 'sdcConfig', 'UserServiceNg2'];
