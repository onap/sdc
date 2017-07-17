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
 * Created by rc2122 on 7/4/2017.
 */
'use strict';
import {ComponentFactory} from "app/utils";
import {AvailableIconsService} from "app/services";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {IMainCategory, ISubCategory} from "app/models";
import {Component} from "app/models";
import {ResourceType} from "app/utils/constants";

interface IIconsModalViewModelScope {
    modalIcons:ng.ui.bootstrap.IModalServiceInstance;
    icons:Array<string>,
    iconSprite:string,
    selectedIcon:string,
    changeIcon(icon:string):void,
    cancel():void
    updateIcon():void;
}

export class IconsModalViewModel {
    static '$inject' = [
        '$scope',
        'Sdc.Services.AvailableIconsService',
        'ComponentFactory',
        '$state',
        '$uibModalInstance',
        'component'
    ];

    constructor(private $scope:IIconsModalViewModelScope,
                private availableIconsService:AvailableIconsService,
                private ComponentFactory:ComponentFactory,
                private $state:ng.ui.IStateService,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private component: Component) {
        this.initScope();
        this._initIcons();
        this.$scope.iconSprite = this.component.iconSprite;
        this.$scope.selectedIcon = this.component.icon;

        if (this.component.isResource()) {
            this.initVendor();
        }

    }

    private _initIcons = ():void => {

        // For subcategories that where created by admin, there is no icons
        this.$scope.icons = new Array<string>();
        if (this.component.categories && this.component.categories.length > 0) {

            _.forEach(this.component.categories, (category:IMainCategory):void => {
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

        if (this.component.isResource()) {
            let resourceType:string = this.component.getComponentSubType();
            if (resourceType === ResourceType.VL) {
                this.$scope.icons = ['vl'];
            }
            if (resourceType === ResourceType.CP) {
                this.$scope.icons = ['cp'];
            }
        }

        if (this.$scope.icons.length === 0) {
            this.$scope.icons = this.availableIconsService.getIcons(this.component.componentType);
        }
        //we always add the defual icon to the list
        this.$scope.icons.push('defaulticon');
    };

    private initVendor = ():void => {
        let vendors:Array<string> = this.availableIconsService.getIcons(this.component.componentType).slice(5, 19);
        let vendorName = this.component.vendorName.toLowerCase();
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
        this.$scope.modalIcons = this.$uibModalInstance;
        this.$scope.icons = [];
        this.$scope.changeIcon = (icon:string):void => {
            this.$scope.selectedIcon = icon;
        };
        this.$scope.cancel = ():void => {
            this.$uibModalInstance.dismiss();
        };
        this.$scope.updateIcon = ():void => {
            let isDirty:boolean = this.component.icon != this.$scope.selectedIcon;
            this.component.icon = this.$scope.selectedIcon;
            this.$uibModalInstance.close(isDirty);
        }
    }

}



