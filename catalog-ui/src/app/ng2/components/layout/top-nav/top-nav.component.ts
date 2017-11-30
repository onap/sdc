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

import {Component, Inject, Input, Output, EventEmitter} from "@angular/core";
import {IHostedApplication, IUserProperties} from "app/models";
import {MenuItemGroup, MenuItem} from "app/utils";
import {UserService} from "../../../services/user.service";
import {SdcConfigToken, ISdcConfig} from "../../../config/sdc-config.config";
import {TranslateService} from "../../../shared/translator/translate.service";


declare const window:any;
@Component({
    selector: 'top-nav',
    templateUrl: './top-nav.component.html',
    styleUrls:['./top-nav.component.less']
})
export class TopNavComponent {
    @Input() public version:string;
    @Input() public menuModel:Array<MenuItemGroup>;
    @Input() public topLvlSelectedIndex:number;
    @Input() public hideSearch:boolean;
    @Input() public searchTerm:string;
    @Input() public notificationIconCallback:Function;
    @Output() public searchTermChange:EventEmitter<string> = new EventEmitter<string>();
    emitSearchTerm(event:string) {
        this.searchTermChange.emit(event);
    }

    public topLvlMenu:MenuItemGroup;
    public user:IUserProperties;

    constructor(private translateService:TranslateService,
                @Inject('$state') private $state:ng.ui.IStateService,
                private userService:UserService,
                @Inject(SdcConfigToken) private sdcConfig:ISdcConfig) {
        window.nav = this;
    }

    private _getTopLvlSelectedIndexByState = ():number => {
        if (!this.topLvlMenu.menuItems) {
            return 0;
        }

        let result = -1;

        //set result to current state
        this.topLvlMenu.menuItems.forEach((item:MenuItem, index:number)=> {
            if (item.state === this.$state.current.name) {
                result = index;
            }
        });

        //if it's a different state , checking previous state param
        if (result === -1) {
            this.topLvlMenu.menuItems.forEach((item:MenuItem, index:number)=> {
                if (item.state === this.$state.params['previousState']) {
                    result = index;
                }
            });
        }

        if (result === -1) {
            result = 0;
        }

        return result;
    };

    ngOnChanges(changes) {
        if (changes['menuModel']) {
            console.log('menuModel was changed!');
            this.generateMenu();
        }
    }

    ngOnInit() {
        console.log('Nav is init!', this.menuModel);
        this.user = this.userService.getLoggedinUser();

        this.translateService.languageChangedObservable.subscribe((lang) => {
            let tmpArray: Array<MenuItem> = [
                new MenuItem(this.translateService.translate("TOP_MENU_HOME_BUTTON"), null, "dashboard", "goToState", null, null),
                new MenuItem(this.translateService.translate("TOP_MENU_CATALOG_BUTTON"), null, "catalog", "goToState", null, null)
            ];

            // Only designer can perform onboarding
            if (this.user && this.user.role === 'DESIGNER') {
                tmpArray.push(new MenuItem(this.translateService.translate("TOP_MENU_ON_BOARD_BUTTON"), null, "onboardVendor", "goToState", null, null));
                _.each(this.sdcConfig.hostedApplications, (hostedApp: IHostedApplication) => {
                    if (hostedApp.exists) {
                        tmpArray.push(new MenuItem(hostedApp.navTitle, null, hostedApp.defaultState, "goToState", null, null));
                    }
                });
            }

            this.topLvlMenu = new MenuItemGroup(0, tmpArray, true);
            this.topLvlMenu.selectedIndex = isNaN(this.topLvlSelectedIndex) ? this._getTopLvlSelectedIndexByState() : this.topLvlSelectedIndex;

            this.generateMenu();
        });
    }

    generateMenu() {
        if (this.menuModel && this.topLvlMenu && this.menuModel[0] !== this.topLvlMenu) {
            this.menuModel.unshift(this.topLvlMenu);
        }
    }

    goToState(state:string, params:Array<any>):Promise<boolean> {
        return new Promise((resolve, reject) => {
            this.$state.go(state, params && params.length > 0 ? [0] : undefined);
            resolve(true);
        });
    }

    menuItemClick(itemGroup:MenuItemGroup, item:MenuItem) {
        itemGroup.itemClick = false;

        let onSuccess = ():void => {
            itemGroup.selectedIndex = itemGroup.menuItems.indexOf(item);
        };
        let onFailed = ():void => {
        };

        if (item.callback) {
            (item.callback.apply(undefined, item.params)).then(onSuccess, onFailed);
        } else {
            this[item.action](item.state, item.params).then(onSuccess, onFailed);
        }
    }
}
