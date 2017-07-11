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

/**
 * Created by obarda on 4/4/2016.
 */
'use strict';
import {ComponentFactory} from "app/utils";
import {AvailableIconsService} from "app/services";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {IMainCategory, ISubCategory} from "app/models";

export interface IIconsScope extends IWorkspaceViewModelScope {
    icons:Array<string>;
    iconSprite:string;
    setComponentIcon(iconSrc:string):void;
}

export class IconsViewModel {

    static '$inject' = [
        '$scope',
        'Sdc.Services.AvailableIconsService',
        'ComponentFactory',
        '$state'
    ];

    constructor(private $scope:IIconsScope,
                private availableIconsService:AvailableIconsService,
                private ComponentFactory:ComponentFactory,
                private $state:ng.ui.IStateService) {


        this.initScope();
        this.initIcons();
        this.$scope.updateSelectedMenuItem();
        this.$scope.iconSprite = this.$scope.component.iconSprite;

        if (this.$scope.component.isResource()) {
            this.initVendor();
        }
    }

    private initialIcon:string = this.$scope.component.icon;
    private initIcons = ():void => {

        // For subcategories that where created by admin, there is no icons
        this.$scope.icons = new Array<string>();
        if (this.$scope.component.categories && this.$scope.component.categories.length > 0) {

            _.forEach(this.$scope.component.categories, (category:IMainCategory):void => {
                if (category.icons) {
                    this.$scope.icons = this.$scope.icons.concat(category.icons);
                }
                if (category.subcategories) {
                    _.forEach(category.subcategories, (subcategory:ISubCategory):void => {
                        if (subcategory.icons) {
                            this.$scope.icons = this.$scope.icons.concat(subcategory.icons);
                        }
                    });
                }
            });
        }

        if (this.$scope.component.isResource()) {
            let resourceType:string = this.$scope.component.getComponentSubType();
            if (resourceType === 'VL') {
                this.$scope.icons = ['vl'];
            }
            if (resourceType === 'CP') {
                this.$scope.icons = ['cp'];
            }
        }

        if (this.$scope.icons.length === 0) {
            this.$scope.icons = this.availableIconsService.getIcons(this.$scope.component.componentType);
        }
        //we always add the defual icon to the list
        this.$scope.icons.push('defaulticon');
    };

    private initVendor = ():void => {
        let vendors:Array<string> = this.availableIconsService.getIcons(this.$scope.component.componentType).slice(5, 19);
        let vendorName = this.$scope.component.vendorName.toLowerCase();
        if ('at&t' === vendorName) {
            vendorName = 'att';
        }
        if ('nokia' === vendorName) {
            vendorName = 'nokiasiemens';
        }
        let vendor:string = _.find(vendors, (vendor:string)=> {
            return vendor.replace(/[_]/g, '').toLowerCase() === vendorName;
        });

        if (vendor && this.$scope.icons.indexOf(vendor) === -1) {
            this.$scope.icons.push(vendor);
        }
    };

    private initScope():void {
        this.$scope.icons = [];
        this.$scope.setValidState(true);
        //if(this.$scope.component.icon === DEFAULT_ICON){
        //    //this.$scope.setValidState(false);
        //}

        this.$scope.setComponentIcon = (iconSrc:string):void => {
            this.$state.current.data.unsavedChanges = !this.$scope.isViewMode() && (iconSrc != this.initialIcon);
            this.$scope.component.icon = iconSrc;
            // this.$scope.setValidState(true);
        };

    }
}
