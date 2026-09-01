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
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
 */

'use strict';
import * as _ from "lodash";
import {Injectable} from "@angular/core";
import {WorkspaceMode, ComponentState} from "./constants";
import {Component} from '../models/components/component';
import {ResourceNamePipe} from "../ng2/pipes/resource-name.pipe";
import {NavigationService} from "../ng2/services/navigation.service";

export class MenuItem {
    text:string;
    callback:(...args:Array<any>) => Promise<boolean>;
    state:string;
    action:string;
    params:any;
    isDisabled:boolean;
    disabledCategory:boolean;
    disabledRoles:Array<string>;
    blockedForTypes:Array<string>;  // This item will not be shown for specific components types.

    //TODO check if needed
    alertModal:string;
    conformanceLevelModal: boolean; // Call validateConformanceLevel API and shows conformanceLevelModal if necessary, then continue with action or invokes another action
    confirmationModal:string;       // Open confirmation modal (user should select "OK" or "Cancel"), and continue with the action.
    url:string;                     // Data added to menu item, in case the function need to use it, example: for function "changeLifecycleState", I need to pass also the state "CHECKOUT" that I want the state to change to.


    constructor(text:string, callback:(...args:Array<any>) => Promise<boolean>, state:string, action:string, params?:any, blockedForTypes?:Array<string>, disabledCategory?:boolean) {
        this.text = text;
        this.callback = callback;
        this.state = state;
        this.action = action;
        this.params = params;
        this.blockedForTypes = blockedForTypes;
        this.disabledCategory = disabledCategory;
    }
}

export class MenuItemGroup {
    selectedIndex:number;
    menuItems:Array<MenuItem>;
    itemClick:boolean;

    constructor(selectedIndex?:number, menuItems?:Array<MenuItem>, itemClick?:boolean) {
        this.selectedIndex = selectedIndex;
        this.menuItems = menuItems;
        this.itemClick = itemClick;
    }

    public updateSelectedMenuItemText(newText:string) {
        const selectedMenuItem = this.menuItems[this.selectedIndex];
        if (selectedMenuItem) {
            this.menuItems[this.selectedIndex].text = newText;
        }
    }
}


@Injectable()
export class MenuHandler {

    constructor(private navigationService:NavigationService) {

    }


    findBreadcrumbComponentIndex = (components:Array<Component>, selected:Component):number => {
        let selectedItemIdx;

        // Search the component in all components by uuid (and not uniqueid, gives access to an assets's minor versions).
        selectedItemIdx = _.findIndex(components, (item:Component) => {
            return item.uuid === selected.uuid;
        });

        // If not found search by invariantUUID
        if (selectedItemIdx === -1) {
            selectedItemIdx = _.findIndex(components, (item:Component) => {
                //invariantUUID && Certified State matches between major versions
                return item.invariantUUID === selected.invariantUUID && item.lifecycleState === ComponentState.CERTIFIED;
            });
        }

        // If not found search by name (name is unique).
        if (selectedItemIdx === -1) {
            selectedItemIdx = _.findIndex(components, (item:Component) => {
                return item.name === selected.name && item.componentType === selected.componentType;
            });
        }

        return selectedItemIdx;
    };

    generateBreadcrumbsModelFromComponents = (components:Array<Component>, selected:Component):MenuItemGroup => {
        let result = new MenuItemGroup(0, [], false);
        if (components) {
            result.selectedIndex = this.findBreadcrumbComponentIndex(components, selected);
            let clickItemCallback = (component:Component):Promise<boolean> => {
                this.navigationService.navigate('workspace.general', {
                    id: component.uniqueId,
                    type: component.componentType.toLowerCase(),
                    mode: WorkspaceMode.VIEW
                });
                return Promise.resolve(true);
            };

            components.forEach((component:Component) => {
                let menuItem = new MenuItem(
                    //  component.name,
                    component.getComponentSubType() + ': ' + ResourceNamePipe.getDisplayName(component.name),
                    clickItemCallback,
                    null,
                    null,
                    [component]
                );
                //  menuItem.text = component.name;
                result.menuItems.push(menuItem);
            });

            result.selectedIndex = this.findBreadcrumbComponentIndex(components, selected);

            // if component does not exist, then add a temporary menu item for the current component
            if (result.selectedIndex === -1) {
                let menuItem = new MenuItem(
                    //  component.name,
                    selected.getComponentSubType() + ': ' + ResourceNamePipe.getDisplayName(selected.name),
                    clickItemCallback,
                    null,
                    null,
                    [selected]
                );
                result.menuItems.unshift(menuItem);
                result.selectedIndex = 0;
            }
        }
        return result;
    };
}
