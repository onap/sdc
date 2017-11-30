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
import {ModalsHandler} from "app/utils";
import {User, IUserProperties, IUser, IAppConfigurtaion} from "app/models";
import {UserService} from "../../../ng2/services/user.service";

interface IUserManagementViewModelScope extends ng.IScope {
    sdcConfig:IAppConfigurtaion;
    usersList:Array<IUserProperties>;
    isLoading:boolean;
    isNewUser:boolean;
    sortBy:string;
    reverse:boolean;
    tableHeadersList:any;
    roles:Array<string>;
    newUser:IUser;
    currentUser:IUserProperties;
    userIdValidationPattern:RegExp;
    editForm:ng.IFormController;
    getAllUsers():void;
    editUserRole(user:IUserProperties);
    sort(sortBy:string):void;
    createUser():void;
    deleteUser(userId:string):void;
    onEditUserPressed(user:IUserProperties):void;
    saveUserChanges(user:IUserProperties):void;
    getTitle(role:string):string;
    clearForm():void;

}


export class UserManagementViewModel {
    static '$inject' = [
        '$scope',
        'sdcConfig',
        'UserServiceNg2',
        'UserIdValidationPattern',
        '$filter',
        'ModalsHandler'
    ];

    constructor(private $scope:IUserManagementViewModelScope,
                private sdcConfig:IAppConfigurtaion,
                private userService:UserService,
                private UserIdValidationPattern:RegExp,
                private $filter:ng.IFilterService,
                private ModalsHandler:ModalsHandler) {

        this.initScope();

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

    private updateUserFilterTerm = (user:IUserProperties):void => {
        user.filterTerm = user.firstName + ' ' + user.lastName + ' ' + user.userId + ' ' + user.email + ' ' + user.role + ' ' + this.$filter('date')(user.lastLoginTime, "MM/dd/yyyy");
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
        this.$scope.userIdValidationPattern = this.UserIdValidationPattern;
        this.$scope.sortBy = 'lastLoginTime';
        this.$scope.reverse = false;
        this.$scope.roles = this.sdcConfig.roles;
        this.$scope.isNewUser = false;
        this.$scope.currentUser = this.userService.getLoggedinUser();
        this.getAllUsers();

        let userInfo:IUserProperties = <IUserProperties>{};
        this.$scope.newUser = new User(userInfo);

        this.$scope.sort = (sortBy:string):void => {//default sort by descending last update. default for alphabetical = ascending
            this.$scope.isNewUser = false;
            this.$scope.reverse = (this.$scope.sortBy === sortBy) ? ( !this.$scope.reverse) : this.$scope.reverse = false;
            this.$scope.sortBy = sortBy;
        };

        this.$scope.createUser = ():void => {

            let onError = (response) => {
                this.$scope.isLoading = false;
                console.info('onFaild', response);
            };

            let onSuccess = (response:IUserProperties) => {
                this.$scope.newUser.userInfo.lastLoginTime = "0";
                this.$scope.newUser.userInfo.status = response.status;
                this.updateUserFilterTerm(this.$scope.newUser.userInfo);
                this.$scope.usersList.push(this.$scope.newUser.userInfo);
                this.$scope.isNewUser = true;
                this.$scope.sortBy = null;
                this.$scope.reverse = true;
                this.$scope.isLoading = false;
                this.$scope.newUser = new User(null);
                this.$scope.editForm.$setPristine();
                let _self = this;
                setTimeout(function () {
                    _self.$scope.isNewUser = false;
                }, 7000);
            };
            this.userService.createUser({
                userId: this.$scope.newUser.userInfo.userId,
                role: this.$scope.newUser.userInfo.role
            }).subscribe(onSuccess, onError);
        };


        this.$scope.onEditUserPressed = (user:IUserProperties):void => {
            user.isInEditMode = true;
            user.tempRole = user.role;
        };

        this.$scope.editUserRole = (user:IUserProperties):void => {
            let roleBeforeUpdate:string = user.role;
            user.role = user.tempRole;

            let onError = (response) => {
                this.$scope.isLoading = false;
                user.role = roleBeforeUpdate;
                console.info('onFaild', response);
            };
            let onSuccess = (response:any) => {
                this.$scope.isLoading = false;
                user.tempRole = user.role;
                this.updateUserFilterTerm(user);
            };

            this.userService.editUserRole(user.userId, user.role).subscribe(onSuccess, onError);
        };

        this.$scope.saveUserChanges = (user:IUserProperties):void => {
            if (user.tempRole != user.role) {
                this.$scope.editUserRole(user)
            }
            user.isInEditMode = false;
        };

        this.$scope.deleteUser = (userId:string):void => {

            let onOk = ():void => {
                this.$scope.isLoading = true;

                let onError = (response):void => {
                    this.$scope.isLoading = false;
                    console.info('onFaild', response);
                };

                let onSuccess = (response:any):void => {
                    _.remove(this.$scope.usersList, {userId: userId});
                    this.$scope.isLoading = false;
                };
                this.userService.deleteUser(userId).subscribe(onSuccess, onError);
            };

            let title:string = this.$filter('translate')("USER_MANAGEMENT_VIEW_DELETE_MODAL_TITLE");
            let message:string = this.$filter('translate')("USER_MANAGEMENT_VIEW_DELETE_MODAL_TEXT");
            this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
        };

        this.$scope.getTitle = (role:string):string => {
            return role.toLowerCase().replace('governor', 'governance_Rep').replace('_', ' ');
        };

        this.$scope.clearForm = ():void => {
            if (!this.$scope.editForm['contactId'].$viewValue && !this.$scope.editForm['role'].$viewValue) {
                this.$scope.editForm.$setPristine();
            }
            //if(this.$scope.editForm['contactId'].$viewValue === '' && this.$scope.editForm['role'].$viewValue){
            //    this.$scope.editForm.$setPristine();
            //}
        };
    }
}
