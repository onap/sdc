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
