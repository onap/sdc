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
/// <reference path="../references"/>
module Sdc.Services {
  'use strict';

  // Define an interface of the object you want to use, providing it's properties
  export interface IUserResource extends Models.IUserProperties,ng.resource.IResource<IUserResource>{

  }

  // Define your resource, adding the signature of the custom actions
  export interface IUserResourceClass extends ng.resource.IResourceClass<IUserResource>{
    authorize(): IUserResource;
    getLoggedinUser(): IUserResource;
    setLoggedinUser(user: IUserResource): void;
    getAllUsers(success?: Function, error?: Function): Array<IUserResource>;
    createUser(IResourceResource, success?: Function, error?: Function): void;
    editUserRole(IResourceResource, success?: Function, error?: Function): void;
    deleteUser(IResourceResource, success?: Function, error?: Function): void;
  }

  export class UserResourceService{

    public static getResource = (
      $resource: ng.resource.IResourceService,
      sdcConfig: Models.IAppConfigurtaion,
      cookieService: Services.CookieService
    ): IUserResourceClass => {

      let url: string = sdcConfig.api.root+sdcConfig.api.GET_user;
      let authorizeUrl: string = sdcConfig.api.root+sdcConfig.api.GET_user_authorize;
      let authorizeActionHeaders: any = {};
      let cookie: Models.ICookie = sdcConfig.cookie;
      authorizeActionHeaders[cookie.userFirstName] = cookieService.getFirstName();
      authorizeActionHeaders[cookie.userLastName] = cookieService.getLastName();
      authorizeActionHeaders[cookie.userEmail] = cookieService.getEmail();
      authorizeActionHeaders[cookie.userIdSuffix] = cookieService.getUserId();

      // Define your custom actions here as IActionDescriptor
      let authorizeAction : ng.resource.IActionDescriptor = {
         method: 'GET',
         isArray: false,
         url: authorizeUrl,
         headers: authorizeActionHeaders
      };

        let getAllUsers : ng.resource.IActionDescriptor = {
            method: 'GET',
            isArray: true,
            url: sdcConfig.api.root + sdcConfig.api.GET_all_users
        };

        let editUserRole : ng.resource.IActionDescriptor = {
            method: 'POST',
            isArray: false,
            url: sdcConfig.api.root + sdcConfig.api.POST_edit_user_role,
            transformRequest: (data, headers)=>{
                data.payloadData = undefined;
                data.payloadName = undefined;
                return JSON.stringify(data);
            }
        };

        let deleteUser : ng.resource.IActionDescriptor = {
            method: 'DELETE',
            isArray: false,
            url: sdcConfig.api.root + sdcConfig.api.DELETE_delete_user
        };

        let createUser : ng.resource.IActionDescriptor = {
            method: 'POST',
            isArray: false,
            url: sdcConfig.api.root + sdcConfig.api.POST_create_user,
            transformRequest: (data, headers)=>{
                data.payloadData = undefined;
                data.payloadName = undefined;
                return JSON.stringify(data);
            }
        };
      let userResource: IUserResourceClass = <IUserResourceClass>$resource(
        url,
        { id: '@id'},
        {
         authorize: authorizeAction,
         getAllUsers: getAllUsers,
         createUser: createUser,
         editUserRole:editUserRole,
         deleteUser:deleteUser}
      );

      let _loggedinUser: IUserResource;

      userResource.getLoggedinUser = () => {
        return _loggedinUser;
      };

      userResource.setLoggedinUser = (loggedinUser: IUserResource) => {
        _loggedinUser = loggedinUser;
      };

      return userResource;
    }
  }
  UserResourceService.getResource.$inject = ['$resource', 'sdcConfig', 'Sdc.Services.CookieService'];
}
