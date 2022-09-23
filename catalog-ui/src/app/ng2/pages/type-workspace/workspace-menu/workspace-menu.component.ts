/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {Component, EventEmitter, Inject, Input, OnInit, Output} from '@angular/core';
import {MenuItem, MenuItemGroup} from "../../../../utils/menu-handler";
import {CacheService} from "../../../services/cache.service";
import {IAppMenu} from "../../../../models/app-config";
import {IUserProperties} from "../../../../models/user";
import {SdcMenuToken} from "../../../config/sdc-menu.config";

@Component({
    selector: 'app-workspace-menu',
    templateUrl: './workspace-menu.component.html',
    styleUrls: ['./workspace-menu.component.less']
})
export class WorkspaceMenuComponent implements OnInit {

    @Input() menuHeader: string = '';
    @Output() onMenuUpdate: EventEmitter<MenuItemGroup> = new EventEmitter<MenuItemGroup>();
    @Output() onClick: EventEmitter<MenuItem> = new EventEmitter<MenuItem>();

    private role: string;
    private user: IUserProperties;
    private $state: ng.ui.IStateService;
    private $q: ng.IQService;

    leftBarTabs: MenuItemGroup = new MenuItemGroup();

    constructor(private cacheService: CacheService, @Inject(SdcMenuToken) private sdcMenu: IAppMenu, @Inject('$injector') $injector) {
        this.$state = $injector.get('$state');
        this.$q = $injector.get('$q');
    }

    ngOnInit(): void {
        this.user = this.cacheService.get('user');
        this.role = this.user.role;
        this.initMenuItems();
    }

    private initMenuItems(): void {
        this.leftBarTabs = new MenuItemGroup();
        const menuItemsObjects: MenuItem[] = this.updateMenuItemByRole(this.sdcMenu.component_workspace_menu_option["DataType"], this.role);

        this.leftBarTabs.menuItems = menuItemsObjects.map((item: MenuItem) => {
            const menuCallback = () => this[menuItem.action](menuItem);
            const menuItem = new MenuItem(item.text, menuCallback, item.state, item.action, item.params, item.blockedForTypes, item.disabledCategory);
            if (menuItem.params) {
                menuItem.params.state = menuItem.state;
            } else {
                menuItem.params = {state: menuItem.state};
            }
            return menuItem;
        });
        this.updateSelectedMenuItem();
        this.onMenuUpdate.emit(this.leftBarTabs);
    }

    isSelected(menuItem: MenuItem): boolean {
        return this.leftBarTabs.selectedIndex === this.leftBarTabs.menuItems.indexOf(menuItem);
    }

    private onMenuItemPressed(menuItem: MenuItem): angular.IPromise<boolean> {
        this.leftBarTabs.selectedIndex = this.leftBarTabs.menuItems.indexOf(menuItem);
        this.onClick.emit(this.leftBarTabs.menuItems[this.leftBarTabs.selectedIndex]);
        const deferred: ng.IDeferred<boolean> = this.$q.defer();
        deferred.resolve(true);
        return deferred.promise;
    }

    private updateMenuItemByRole(menuItems: MenuItem[], role: string): MenuItem[] {
        const filteredMenuItems: MenuItem[] = [];
        menuItems.forEach((item: any) => {
            if (!(item.disabledRoles && item.disabledRoles.indexOf(role) > -1)) {
                filteredMenuItems.push(item);
            }
        });
        return filteredMenuItems;
    }

    private updateSelectedMenuItem(): void {
        const stateArray: Array<string> = this.$state.current.name.split('.', 2);
        const stateWithoutInternalNavigate: string = stateArray[0] + '.' + stateArray[1];
        const selectedItem: MenuItem = this.leftBarTabs.menuItems.find((item: MenuItem) => {
            let itemStateArray: Array<string> = item.state.split('.', 2);
            let itemStateWithoutNavigation: string = itemStateArray[0] + '.' + itemStateArray[1];
            return (itemStateWithoutNavigation === stateWithoutInternalNavigate);
        });
        this.leftBarTabs.selectedIndex = selectedItem ? this.leftBarTabs.menuItems.indexOf(selectedItem) : 0;
        this.onClick.emit(this.leftBarTabs.menuItems[this.leftBarTabs.selectedIndex]);
    }

}
