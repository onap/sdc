'use strict';
import {ModalsHandler} from "app/utils";
import {IUserResource, IUserResourceClass} from "app/services";
import {User, IUserProperties, IUser, IAppConfigurtaion} from "app/models";

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
    currentUser:IUserResource;
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
        'Sdc.Services.UserResourceService',
        'UserIdValidationPattern',
        '$filter',
        'ModalsHandler'
    ];

    constructor(private $scope:IUserManagementViewModelScope,
                private sdcConfig:IAppConfigurtaion,
                private userResourceService:IUserResourceClass,
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
        this.userResourceService.getAllUsers(onSuccess, onError);
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
        this.$scope.currentUser = this.userResourceService.getLoggedinUser();
        this.getAllUsers();

        let resource:IUserResource = <IUserResource>{};
        this.$scope.newUser = new User(resource);

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
                this.$scope.newUser.resource['index'] = this.$scope.usersList.length;
                this.$scope.newUser.resource.lastLoginTime = "0";
                this.$scope.newUser.resource.status = response.status;
                this.updateUserFilterTerm(this.$scope.newUser.resource);
                this.$scope.usersList.unshift(this.$scope.newUser.resource);
                this.$scope.isNewUser = true;
                this.$scope.sortBy = 'index';
                this.$scope.reverse = true;
                this.$scope.isLoading = false;
                this.$scope.newUser = new User(null);
                this.$scope.editForm.$setPristine();
                let _self = this;
                setTimeout(function () {
                    _self.$scope.isNewUser = false;
                }, 7000);
            };
            this.userResourceService.createUser({
                userId: this.$scope.newUser.resource.userId,
                role: this.$scope.newUser.resource.role
            }, onSuccess, onError);
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

            this.userResourceService.editUserRole({id: user.userId, role: user.role}, onSuccess, onError);
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
                this.userResourceService.deleteUser({id: userId}, onSuccess, onError);
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
