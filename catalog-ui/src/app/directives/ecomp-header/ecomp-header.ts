'use strict';
import {IAppConfigurtaion, User, IUser} from "app/models";
import {IUserResourceClass, IUserResource} from "app/services";

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
                private UserResourceClass:IUserResourceClass) {

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
            let user:IUserResource = this.UserResourceClass.getLoggedinUser();
            if (!user) {
                defaultUserId = this.$http.defaults.headers.common[this.sdcConfig.cookie.userIdSuffix];
                user = this.UserResourceClass.get({id: defaultUserId}, ():void => {
                    $scope.user = new User(user);
                });
            } else {
                $scope.user = new User(user);
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
                             UserResourceClass:IUserResourceClass)=> {
        return new EcompHeaderDirective($http, sdcConfig, UserResourceClass);
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

EcompHeaderDirective.factory.$inject = ['$http', 'sdcConfig', 'Sdc.Services.UserResourceService'];





