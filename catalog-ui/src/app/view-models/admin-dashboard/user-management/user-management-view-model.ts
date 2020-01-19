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
import * as _ from "lodash";
import { User, IUserProperties, IUser, IAppConfigurtaion } from "app/models";
import { UserService } from "../../../ng2/services/user.service";
import { SdcUiCommon, SdcUiServices, SdcUiComponents } from "onap-ui-angular";
import { AuthenticationService } from "app/ng2/services/authentication.service";

interface IUserManagementViewModelScope extends ng.IScope {
    usersList:Array<IUserProperties>;
    isLoading:boolean;
    sortBy:string;
    reverse:boolean;
    tableHeadersList:any;
    getAllUsers():void;
    sort(sortBy:string):void;
    getTitle(role:string):string;
}


export class UserManagementViewModel {
    static '$inject' = [
        '$scope',
        'UserServiceNg2',
        'AuthenticationServiceNg2',
        '$filter',
        'ModalServiceSdcUI'
    ];

    constructor(private $scope:IUserManagementViewModelScope,
                private userService:UserService,
                private authService:AuthenticationService,
                private $filter:ng.IFilterService,
                private modalService:SdcUiServices.ModalService) {


        setTimeout(this.initScope, 1000);
    }


    private getAllUsers = ():void => {
        this.$scope.isLoading = true;

        let onError = (response) => {
            this.$scope.isLoading = false;
            console.info('onFaild', response);
        };
        let onSuccess = (response:Array<IUserProperties>) => {
            this.$scope.usersList = response;
            _.forEach(this.$scope.usersList, (user:any, i:number)=> {
                user.index = i;
            });
            this.$scope.isLoading = false;
        };
        this.userService.getAllUsers().subscribe(onSuccess, onError);
    };

    private initScope = ():void => {
        let self = this;
        this.$scope.tableHeadersList = [{title: "First Name", property: 'firstName'}, {
            title: "Last Name",
            property: 'lastName'
        },
            {
                title: this.$filter('translate')("USER_MANAGEMENT_TABLE_HEADER_USER_ID"),
                property: 'userId'
            }, {title: "Email", property: 'email'}, {title: "Role", property: 'role'}, {
                title: "Last Active",
                property: 'lastLoginTime'
            }];
        this.$scope.sortBy = 'lastLoginTime';
        this.$scope.reverse = false;
        this.getAllUsers();

        this.$scope.sort = (sortBy:string):void => {//default sort by descending last update. default for alphabetical = ascending
            this.$scope.reverse = (this.$scope.sortBy === sortBy) ? ( !this.$scope.reverse) : this.$scope.reverse = false;
            this.$scope.sortBy = sortBy;
        };

        this.$scope.getTitle = (role:string):string => {
            return role.toLowerCase().replace('governor', 'governance_Rep').replace('_', ' ');
        };

    }
}
