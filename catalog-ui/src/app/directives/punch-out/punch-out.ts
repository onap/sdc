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
import {IUserProperties, IAppConfigurtaion} from "app/models";
let PunchOutRegistry = require('third-party/PunchOutRegistry.js');

export interface IPunchOutScope extends ng.IScope {
    name:string;
    data:any;
    user:IUserProperties;
    onEvent:Function;
}

export class PunchOutDirective implements ng.IDirective {

    constructor(private sdcConfig:IAppConfigurtaion) {
    }

    scope = {
        name: '=',
        data: '=',
        user: '=',
        onEvent: '&'
    };

    replace = false;
    restrict = 'E';

    link = (scope:IPunchOutScope, element:ng.IAugmentedJQuery):void => {
        // global registry object
        let PunchOutRegistry = window['PunchOutRegistry'];

        let render = ():void => {
            let cookieConfig = this.sdcConfig.cookie;
            let props = {
                name: scope.name,
                options: {
                    data: scope.data,
                    apiRoot: this.sdcConfig.api.root,
                    apiHeaders: {
                        userId: {
                            name: cookieConfig.userIdSuffix,
                            value: scope.user.userId
                        },
                        userFirstName: {
                            name: cookieConfig.userFirstName,
                            value: scope.user.firstName
                        },
                        userLastName: {
                            name: cookieConfig.userLastName,
                            value: scope.user.lastName
                        },
                        userEmail: {
                            name: cookieConfig.userEmail,
                            value: scope.user.email
                        }
                    }
                },
                onEvent: (...args) => {
                    scope.$apply(() => {
                        scope.onEvent().apply(null, args);
                    });
                }
            };
            PunchOutRegistry.render(props, element[0]);
        };

        let unmount = ():void => {
            PunchOutRegistry.unmount(element[0]);
        };

        scope.$watch('data', render);
        element.on('$destroy', unmount);
    };

    public static factory = (sdcConfig:IAppConfigurtaion) => {
        return new PunchOutDirective(sdcConfig);
    };

}

PunchOutDirective.factory.$inject = ['sdcConfig'];
