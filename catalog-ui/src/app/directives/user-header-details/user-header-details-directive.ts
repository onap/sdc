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
import {User, IUser, IAppConfigurtaion} from "app/models";
import {IUserResourceClass, IUserResource} from "app/services";
export interface IUserHeaderDetailsScope extends ng.IScope {
    name:string;
    role:string;
    iconUrl:string;
    UserResourceClass:IUserResourceClass;
    user:IUser;
    sdcConfig:IAppConfigurtaion;
    initUser:Function;
}

export class UserHeaderDetailsDirective implements ng.IDirective {

    constructor(private $http:ng.IHttpService, private sdcConfig:IAppConfigurtaion, private UserResourceClass:IUserResourceClass) {
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
            let user:IUserResource = this.UserResourceClass.getLoggedinUser();
            if (!user) {
                defaultUserId = this.$http.defaults.headers.common[this.sdcConfig.cookie.userIdSuffix];
                user = this.UserResourceClass.get({id: defaultUserId}, ():void => {
                    scope.user = new User(user);
                });
            } else {
                scope.user = new User(user);
            }
        };
        scope.initUser();
    };

    public static factory = ($http:ng.IHttpService, sdcConfig:IAppConfigurtaion, UserResourceClass:IUserResourceClass)=> {
        return new UserHeaderDetailsDirective($http, sdcConfig, UserResourceClass);
    };

}

UserHeaderDetailsDirective.factory.$inject = ['$http', 'sdcConfig', 'Sdc.Services.UserResourceService'];
