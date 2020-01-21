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

import * as _ from "lodash";
import {Component, Inject, Input, Output, EventEmitter, OnInit, OnDestroy, OnChanges} from "@angular/core";
import {IHostedApplication, IUserProperties} from "app/models";
import {MenuItemGroup, MenuItem} from "app/utils";
import {AuthenticationService} from "../../../services/authentication.service";
import {SdcConfigToken, ISdcConfig} from "../../../config/sdc-config.config";
import {TranslateService} from "../../../shared/translator/translate.service";
import {PluginsConfiguration, Plugin} from "app/models";
import { Subscription } from "rxjs";
// import { Store } from "@ngrx/store";
// import { AppState } from "app/ng2/store/app.state";
// import * as unsavedChangesReducer from 'app/ng2/store/reducers/unsaved-changes.reducer';

declare const window:any;
@Component({
    selector: 'top-nav',
    templateUrl: './top-nav.component.html',
    styleUrls:['./top-nav.component.less']
})
export class TopNavComponent implements OnInit, OnChanges {
    @Input() public version:string;
    @Input() public menuModel:Array<MenuItemGroup>;
    @Input() public topLvlSelectedIndex:number;
    @Input() public hideSearch:boolean;
    @Input() public searchTerm:string;
    @Input() public notificationIconCallback:Function;
    @Input() public unsavedChanges: boolean;
    @Input() public unsavedChangesCallback: (completeCallback:Function)=> Promise<any>;
    @Output() public searchTermChange:EventEmitter<string> = new EventEmitter<string>();
    emitSearchTerm(event:string) {
        this.searchTermChange.emit(event);
    }

    private subscription: Subscription;
    private hasUnsavedChanges: boolean;
    public topLvlMenu:MenuItemGroup;
    public user:IUserProperties;
    private topNavPlugins: Array<Plugin>;

    constructor(private translateService:TranslateService,
                @Inject('$state') private $state:ng.ui.IStateService,
                private authService:AuthenticationService,
                @Inject(SdcConfigToken) private sdcConfig:ISdcConfig) {
        window.nav = this;

    }

    private _getTopLvlSelectedIndexByState = ():number => {
        if (!this.topLvlMenu.menuItems) {
            return 0;
        }

        let result = -1;

        //set result to current state
        this.topLvlMenu.menuItems.every((item:MenuItem, index:number)=> {
            if (item.state === this.$state.current.name) {
                if (this.$state.current.name === 'plugins') {
                    const pluginIdx = _.findIndex(this.topNavPlugins, (plugin: Plugin) => plugin.pluginStateUrl === this.$state.params.path);
                    if (pluginIdx !== -1) {
                        result = index + pluginIdx;
                        return false;
                    }
                } else {
                    result = index;
                    return false;
                }
            }
            return true;
        });

        //if it's a different state
        if (result === -1) {
            //if in 'workspace' -  checking previous state param
            if (this.$state.includes('workspace')) {
                // if previous state is 'dashboard' or 'catalog', then select it - otherwise, use 'catalog' as default for 'workspace'
                const selectedStateName = (['dashboard', 'catalog'].indexOf(this.$state.params['previousState']) !== -1)
                    ? this.$state.params['previousState']
                    : 'catalog';
                result = this.topLvlMenu.menuItems.findIndex((item:MenuItem) => item.state === selectedStateName);
            }

            //if yet, none is selected, then select the first as default
            if (result === -1) {
                result = 0;
            }
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
        this.user = this.authService.getLoggedinUser();
        this.topNavPlugins = _.filter(PluginsConfiguration.plugins, (plugin: Plugin) => {
            return plugin.pluginDisplayOptions["tab"] !== undefined;
        });

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

            // Adding plugins to top-nav only if they can be displayed for the current connected user role
            _.each(PluginsConfiguration.plugins, (plugin: Plugin) => {
                if (plugin.pluginDisplayOptions["tab"] && (this.user && plugin.pluginDisplayOptions["tab"].displayRoles.includes(this.user.role))) {
                    tmpArray.push(new MenuItem(plugin.pluginDisplayOptions["tab"].displayName, null, "plugins", "goToState", {path: plugin.pluginStateUrl}, null));
                }
            });

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

    goToState(state:string, params:any):Promise<boolean> {
        return new Promise((resolve, reject) => {
            this.$state.go(state, params || undefined);
            resolve(true);
        });
    }


    menuItemClick(itemGroup:MenuItemGroup, item:MenuItem) {

        let onSuccessFunction = () => {
            this.navigate(itemGroup, item);
        }
        if (this.unsavedChanges && this.unsavedChangesCallback){
            this.unsavedChangesCallback(onSuccessFunction).then((onSuccess)=> {
                this.navigate(itemGroup, item);
            }, (onReject) => {});
        } else {
            this.navigate(itemGroup, item);
        }
    }

    navigate(itemGroup:MenuItemGroup, item:MenuItem) {
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
