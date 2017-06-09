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
