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
import {WorkspaceMode, ComponentState} from "./constants";
import {IAppConfigurtaion, IAppMenu, Component} from "../models";
import {ComponentFactory} from "./component-factory";
import {ModalsHandler} from "./modals-handler";

export class MenuItem {
    text:string;
    callback:(...args:Array<any>) => ng.IPromise<boolean>;
    state:string;
    action:string;
    params:Array<any>;
    isDisabled:boolean;
    disabledRoles:Array<string>;
    blockedForTypes:Array<string>;  // This item will not be shown for specific components types.

    //TODO check if needed
    alertModal:string;
    conformanceLevelModal: boolean; // Call validateConformanceLevel API and shows conformanceLevelModal if necessary, then continue with action or invokes another action
    confirmationModal:string;       // Open confirmation modal (user should select "OK" or "Cancel"), and continue with the action.
    emailModal:string;              // Open email modal (user should fill email details), and continue with the action.
    url:string;                     // Data added to menu item, in case the function need to use it, example: for function "changeLifecycleState", I need to pass also the state "CHECKOUT" that I want the state to change to.


    constructor(text:string, callback:(...args:Array<any>) => ng.IPromise<boolean>, state:string, action:string, params?:Array<any>, blockedForTypes?:Array<string>) {
        this.text = text;
        this.callback = callback;
        this.state = state;
        this.action = action;
        this.params = params;
        this.blockedForTypes = blockedForTypes;
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
        this.menuItems[this.selectedIndex].text = newText;
    }
}


export class MenuHandler {

    static '$inject' = [
        'sdcConfig',
        'sdcMenu',
        'ComponentFactory',
        '$filter',
        'ModalsHandler',
        '$state',
        '$q'
    ];

    constructor(private sdcConfig:IAppConfigurtaion,
                private sdcMenu:IAppMenu,
                private ComponentFactory:ComponentFactory,
                private $filter:ng.IFilterService,
                private ModalsHandler:ModalsHandler,
                private $state:ng.ui.IStateService,
                private $q:ng.IQService) {

    }


    generateBreadcrumbsModelFromComponents = (components:Array<Component>, selected:Component):MenuItemGroup => {
        let result = new MenuItemGroup(0, [], false);
        if (components) {

            // Search the component in all components by uuid (and not uniqueid, gives access to an assets's minor versions).
            let selectedItem = _.find(components, (item:Component) => {
                return item.uuid === selected.uuid;
            });

            // If not found search by invariantUUID
            if (undefined == selectedItem) {
                selectedItem = _.find(components, (item:Component) => {
                    //invariantUUID && Certified State matches between major versions
                    return item.invariantUUID === selected.invariantUUID && item.lifecycleState === ComponentState.CERTIFIED;
                });
            }

            // If not found search by name (name is unique).
            if (undefined == selectedItem) {
                selectedItem = _.find(components, (item:Component) => {
                    return item.name === selected.name;
                });
            }

            if(!selectedItem){
                result.selectedIndex = components.length;
            }else{
                result.selectedIndex = components.indexOf(selectedItem);
            }
            components[result.selectedIndex] = selected;
            let clickItemCallback = (component:Component):ng.IPromise<boolean> => {
                this.$state.go('workspace.general', {
                    id: component.uniqueId,
                    type: component.componentType.toLowerCase(),
                    mode: WorkspaceMode.VIEW
                });
                return this.$q.when(true);
            };

            components.forEach((component:Component) => {
                let menuItem = new MenuItem(
                    //  component.name,
                    component.getComponentSubType() + ': ' + this.$filter('resourceName')(component.name),
                    clickItemCallback,
                    null,
                    null,
                    [component]
                );
                //  menuItem.text = component.name;
                result.menuItems.push(menuItem);
            });
        }
        return result;
    };
}
