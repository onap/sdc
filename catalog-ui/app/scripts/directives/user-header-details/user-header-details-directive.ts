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
/// <reference path="../../references"/>
module Sdc.Directives {
    'use strict';
    export interface IUserHeaderDetailsScope extends ng.IScope {
        name: string;
        role: string;
        iconUrl: string;
        UserResourceClass:Services.IUserResourceClass;
        user: Models.IUser;
        sdcConfig:Models.IAppConfigurtaion;
        initUser:Function;
    }

    export class UserHeaderDetailsDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService, private $http:ng.IHttpService, private sdcConfig:Models.IAppConfigurtaion, private UserResourceClass:Services.IUserResourceClass) {
        }

        scope = {
            iconUrl: '=?'
        };

        replace = true;
        restrict = 'E';
        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/user-header-details/user-header-details-directive.html');
        };

        link = (scope:IUserHeaderDetailsScope) => {

            scope.initUser = ():void => {
                let defaultUserId:string;
                let user:Services.IUserResource = this.UserResourceClass.getLoggedinUser();
                if (!user) {
                    defaultUserId = this.$http.defaults.headers.common[this.sdcConfig.cookie.userIdSuffix];
                    user = this.UserResourceClass.get({id: defaultUserId}, ():void => {
                        scope.user = new Models.User(user);
                    });
                } else {
                    scope.user = new Models.User(user);
                }
            };
            scope.initUser();
        };

        public static factory = ($templateCache:ng.ITemplateCacheService, $http:ng.IHttpService, sdcConfig:Models.IAppConfigurtaion, UserResourceClass:Services.IUserResourceClass)=> {
            return new UserHeaderDetailsDirective($templateCache, $http, sdcConfig, UserResourceClass);
        };

    }

    UserHeaderDetailsDirective.factory.$inject = ['$templateCache', '$http', 'sdcConfig', 'Sdc.Services.UserResourceService'];
}
