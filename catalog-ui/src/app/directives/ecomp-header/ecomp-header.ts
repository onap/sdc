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
import {IAppConfigurtaion, User, IUser} from "app/models";
import {IUserProperties} from "../../models/user";
import {UserService} from "../../ng2/services/user.service";

export class MenuItem {
    menuId:number;
    column:number;
    text:string;
    parentMenuId:number;
    url:string;
    children:Array<MenuItem>
}

export interface IEcompHeaderDirectiveScope extends ng.IScope {
    menuData:Array<MenuItem>;
    version:string;
    clickableLogo:string;
    contactUsUrl:string;
    getAccessUrl:string;
    megaMenuDataObjectTemp:Array<any>;
    megaMenuDataObject:Array<any>;

    selectedTopMenu:MenuItem;
    selectedSubMenu:MenuItem;

    firstMenuLevelClick:Function;
    subMenuEnterAction:Function;
    subMenuLeaveAction:Function;

    memuItemClick:Function;
    user:IUser;
}

export class EcompHeaderDirective implements ng.IDirective {

    constructor(private $http:ng.IHttpService,
                private sdcConfig:IAppConfigurtaion,
                private userService:UserService) {

    }

    scope = {
        menuData: '=',
        version: '@',
        clickableLogo: '@?'
    };

    public replace = true;
    public restrict = 'E';
    public controller = EcompHeaderController;

    template = ():string => {
        return 'src/app/directives/ecomp-header/ecomp-header.html';
    };

    link = ($scope:IEcompHeaderDirectiveScope, $elem:JQuery, attr:any) => {

        if (!$scope.clickableLogo) {
            $scope.clickableLogo = "true";
        }

        let findMenuItemById = (menuId):MenuItem => {
            let selectedMenuItem:MenuItem = _.find($scope.menuData, (item:MenuItem)=> {
                if (item.menuId === menuId) {
                    return item;
                }
            });
            return selectedMenuItem;
        };

        let initUser = ():void => {
            let defaultUserId:string;
            let userInfo:IUserProperties = this.userService.getLoggedinUser();
            if (!userInfo) {
                defaultUserId = this.$http.defaults.headers.common[this.sdcConfig.cookie.userIdSuffix];
                this.userService.getUser(defaultUserId).subscribe((defaultUserInfo):void => {
                    $scope.user = new User(defaultUserInfo);
                });
            } else {
                $scope.user = new User(userInfo);
            }
        };

        $scope.firstMenuLevelClick = (menuId:number):void => {
            let selectedMenuItem:MenuItem = _.find($scope.megaMenuDataObjectTemp, (item:MenuItem)=> {
                if (item.menuId === menuId) {
                    return item;
                }
            });
            if (selectedMenuItem) {
                $scope.selectedTopMenu = selectedMenuItem;
                //console.log("Selected menu item: " + selectedMenuItem.text);
            }
        };

        $scope.subMenuEnterAction = (menuId:number):void => {
            $scope.selectedSubMenu = findMenuItemById(menuId);
        };

        $scope.subMenuLeaveAction = (menuId:number):void => {
            $scope.selectedTopMenu = undefined;
        };

        $scope.memuItemClick = (menuItem:MenuItem):void => {
            if (menuItem.url) {
                window.location.href = menuItem.url;
            } else {
                console.log("Menu item: " + menuItem.text + " does not have defined URL!");
            }
        };

        initUser();

    };

    public static factory = ($http:ng.IHttpService,
                             sdcConfig:IAppConfigurtaion,
                             userService:UserService)=> {
        return new EcompHeaderDirective($http, sdcConfig, userService);
    };

}

export class EcompHeaderController {

    messages:any;
    getAttachId:Function;
    render:any;
    reRender:Function;
    register:Function;
    deregister:Function;
    head:any;

    static '$inject' = [
        '$element',
        '$scope',
        '$attrs',
        '$animate'
    ];

    constructor(private $element:JQuery,
                private $scope:IEcompHeaderDirectiveScope,
                private $attrs:ng.IAttributes,
                private $animate:any) {

        this.$scope = $scope;

        this.$scope.$watch('menuData', (newVal, oldVal) => {
            if (newVal) {
                this.init();
            }
        });

    }

    init = ():void => {

        this.$scope.contactUsUrl = "https://wiki.web.att.com/display/EcompPortal/ECOMP+Portal+Home";
        this.$scope.getAccessUrl = "http://ecomp-tlv-dev2.uccentral.att.com:8080/ecompportal/get_access";

        let unflatten = (array, parent?, tree?) => {
            tree = typeof tree !== 'undefined' ? tree : [];
            parent = typeof parent !== 'undefined' ? parent : {menuId: null};
            let children = _.filter(array, function (child) {
                return child["parentMenuId"] == parent.menuId;
            });
            if (!_.isEmpty(children)) {
                if (parent.menuId === null) {
                    tree = children;
                } else {
                    parent['children'] = children
                }
                _.each(children, function (child) {
                    unflatten(array, child)
                });
            }
            return tree;
        };

        let menuStructureConvert = (menuItems) => {
            console.log(menuItems);
            this.$scope.megaMenuDataObjectTemp = [
                {
                    menuId: 1001,
                    text: "ECOMP",
                    children: menuItems
                },
                {
                    menuId: 1002,
                    text: "Help",
                    children: [
                        {
                            text: "Contact Us",
                            url: this.$scope.contactUsUrl
                        }]
                }
            ];

            /*{
             text:"Get Access",
             url: this.$scope.getAccessUrl
             }*/
            return this.$scope.megaMenuDataObjectTemp;
        };

        let a = unflatten(this.$scope.menuData);
        this.$scope.megaMenuDataObject = menuStructureConvert(a);
        //console.log(this.$scope.megaMenuDataObject);
    };
}

EcompHeaderDirective.factory.$inject = ['$http', 'sdcConfig', 'UserServiceNg2'];





