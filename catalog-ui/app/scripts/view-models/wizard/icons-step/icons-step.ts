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
/// <reference path="../../../references"/>
module Sdc.ViewModels.Wizard {
    'use strict';

    export interface IIconsStepScope extends IWizardCreationStepScope{
        icons : Array<string>;
        component: Models.Components.Component;
        iconSprite: string;
        setComponentIcon(iconSrc:string): void;
    }

    export class IconsStepViewModel implements IWizardCreationStep {

        static '$inject' = [
            '$scope',
            'Sdc.Services.AvailableIconsService',
            'ComponentFactory'
        ];

        constructor(private $scope:IIconsStepScope,
                    private availableIconsService:Services.AvailableIconsService,
                    private ComponentFactory: Sdc.Utils.ComponentFactory) {

            this.$scope.registerChild(this);
            this.$scope.component = this.$scope.getComponent();
            this.$scope.iconSprite = this.$scope.component.iconSprite;
            this.initScope();
            this.initIcons();

            if(this.$scope.component.isResource()) {
                this.initVendor();
            }
            // In case there is one icons select it.
            if( this.$scope.icons.length == 1 && !this.$scope.component.isAlreadyCertified()){
                this.$scope.setComponentIcon(this.$scope.icons[0]);
            }
        }

        private initIcons = ():void => {

            // For subcategories that where created by admin, there is no icons
            this.$scope.icons = new Array<string>();
            if (this.$scope.component.categories && this.$scope.component.categories.length > 0) {

                _.forEach(this.$scope.component.categories, (category:Models.IMainCategory):void => {
                    if (category.icons) {
                        this.$scope.icons = this.$scope.icons.concat(category.icons);
                    }
                    if (category.subcategories) {
                        _.forEach(category.subcategories, (subcategory:Models.ISubCategory):void => {
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

        };

        private initVendor = ():void => {
            let vendors:Array<string> = this.availableIconsService.getIcons(this.$scope.component.componentType).slice(5, 19);
            let vendorName = this.$scope.component.vendorName.toLowerCase();
            if ('at&t' === vendorName){
                vendorName = 'att';
            }
            if ('nokia' === vendorName){
                vendorName = 'nokiasiemens';
            }

            let vendor:string = _.find(vendors, (vendor:string)=>{
                return vendor.replace(/[_]/g, '').toLowerCase() === vendorName;
            });

            if(vendor && this.$scope.icons.indexOf(vendor)===-1) {
                this.$scope.icons.push(vendor);
            }
        };

        private initScope():void {
            this.$scope.icons = [];

            if(this.$scope.component.icon === Utils.Constants.DEFAULT_ICON){
                this.$scope.setValidState(false);
            }

            this.$scope.setComponentIcon = (iconSrc:string):void => {
                this.$scope.component.icon = iconSrc;
                this.$scope.setValidState(true);
            }
        }

        save(callback:Function):void {
            let onFailed = () => {
                callback(false);
            };

            let onSuccess = (component:Models.Components.Component) => {
                this.$scope.component = component;
                this.$scope.setComponent(this.$scope.component);
                callback(true);
            };

            try {
                this.$scope.component.updateComponent().then(onSuccess, onFailed);
            }catch(e){
                callback(false);
            }
        }

        public back = (callback:Function):void => {
             this.save(callback);
        }

    }

}
