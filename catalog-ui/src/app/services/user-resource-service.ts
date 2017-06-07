'use strict';
import {IUserProperties} from "../models/user";
import {ICookie, IAppConfigurtaion} from "../models/app-config";
import {CookieService} from "./cookie-service";

// Define an interface of the object you want to use, providing it's properties
export interface IUserResource extends IUserProperties,ng.resource.IResource<IUserResource> {

}

// Define your resource, adding the signature of the custom actions
export interface IUserResourceClass extends ng.resource.IResourceClass<IUserResource> {
    authorize():IUserResource;
    getLoggedinUser():IUserResource;
    setLoggedinUser(user:IUserResource):void;
    getAllUsers(success?:Function, error?:Function):Array<IUserResource>;
    createUser(IResourceResource, success?:Function, error?:Function):void;
    editUserRole(IResourceResource, success?:Function, error?:Function):void;
    deleteUser(IResourceResource, success?:Function, error?:Function):void;
}

export class UserResourceService {

    public static getResource = ($resource:ng.resource.IResourceService,
                                 sdcConfig:IAppConfigurtaion,
                                 cookieService:CookieService):IUserResourceClass => {

        let url:string = sdcConfig.api.root + sdcConfig.api.GET_user;
        let authorizeUrl:string = sdcConfig.api.root + sdcConfig.api.GET_user_authorize;
        let authorizeActionHeaders:any = {};
        let cookie:ICookie = sdcConfig.cookie;
        authorizeActionHeaders[cookie.userFirstName] = cookieService.getFirstName();
        authorizeActionHeaders[cookie.userLastName] = cookieService.getLastName();
        authorizeActionHeaders[cookie.userEmail] = cookieService.getEmail();
        authorizeActionHeaders[cookie.userIdSuffix] = cookieService.getUserId();

        // Define your custom actions here as IActionDescriptor
        let authorizeAction:ng.resource.IActionDescriptor = {
            method: 'GET',
            isArray: false,
            url: authorizeUrl,
            headers: authorizeActionHeaders
        };

        let getAllUsers:ng.resource.IActionDescriptor = {
            method: 'GET',
            isArray: true,
            url: sdcConfig.api.root + sdcConfig.api.GET_all_users
        };

        let editUserRole:ng.resource.IActionDescriptor = {
            method: 'POST',
            isArray: false,
            url: sdcConfig.api.root + sdcConfig.api.POST_edit_user_role,
            transformRequest: (data, headers)=> {
                data.payloadData = undefined;
                data.payloadName = undefined;
                return JSON.stringify(data);
            }
        };

        let deleteUser:ng.resource.IActionDescriptor = {
            method: 'DELETE',
            isArray: false,
            url: sdcConfig.api.root + sdcConfig.api.DELETE_delete_user
        };

        let createUser:ng.resource.IActionDescriptor = {
            method: 'POST',
            isArray: false,
            url: sdcConfig.api.root + sdcConfig.api.POST_create_user,
            transformRequest: (data, headers)=> {
                data.payloadData = undefined;
                data.payloadName = undefined;
                return JSON.stringify(data);
            }
        };
        let userResource:IUserResourceClass = <IUserResourceClass>$resource(
            url,
            {id: '@id'},
            {
                authorize: authorizeAction,
                getAllUsers: getAllUsers,
                createUser: createUser,
                editUserRole: editUserRole,
                deleteUser: deleteUser
            }
        );

        let _loggedinUser:IUserResource;

        userResource.getLoggedinUser = () => {
            return _loggedinUser;
        };

        userResource.setLoggedinUser = (loggedinUser:IUserResource) => {
            _loggedinUser = loggedinUser;
        };

        return userResource;
    }
}
UserResourceService.getResource.$inject = ['$resource', 'sdcConfig', 'Sdc.Services.CookieService'];
