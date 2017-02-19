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

    export interface IHierarchyStepScope extends IWizardCreationScope {

        categoriesOptions: Array<Models.IMainCategory>;
        product:Models.Components.Product;
        isLoading:boolean;
        showDropDown:boolean;

        onInputTextClicked():void;
        onGroupSelected(category: Models.IMainCategory, subcategory: Models.ISubCategory, group: Models.IGroup):void;
        clickOutside():void;
        deleteGroup(uniqueId:string):void;
    }

    export class HierarchyStepViewModel implements IWizardCreationStep {

        static '$inject' = [
            '$scope',
            'Sdc.Services.CacheService',
            'ComponentFactory'
        ];

        constructor(private $scope:IHierarchyStepScope,
                    private cacheService:Sdc.Services.CacheService,
                    private ComponentFactory: Sdc.Utils.ComponentFactory) {

            this.$scope.registerChild(this);
            this.$scope.setValidState(true);
            this.$scope.product = <Models.Components.Product>this.$scope.getComponent();
            this.initScope();
        }

        private initCategories = () => {
            this.$scope.categoriesOptions = angular.copy(this.cacheService.get('productCategories'));
            let selectedGroup:Array<Models.IGroup>  = [];
            _.forEach(this.$scope.product.categories, (category: Models.IMainCategory) => {
                _.forEach(category.subcategories, (subcategory:Models.ISubCategory) => {
                   selectedGroup = selectedGroup.concat(subcategory.groupings);
                });
            });
            _.forEach(this.$scope.categoriesOptions, (category: Models.IMainCategory) => {
                _.forEach(category.subcategories, (subcategory:Models.ISubCategory) => {
                    _.forEach(subcategory.groupings, (group:Models.ISubCategory) => {
                        let componentGroup:Models.IGroup = _.find(selectedGroup, (componentGroupObj) => {
                            return componentGroupObj.uniqueId == group.uniqueId;
                        });
                        if(componentGroup){
                            group.isDisabled = true;
                        }
                    });
                });
            });
        };

        private setFormValidation = ():void => {
            if(!this.$scope.product.categories || this.$scope.product.categories.length === 0){
                this.$scope.setValidState(false);
            }
            else{
                this.$scope.setValidState(true);
            }

        };

        private initScope = ():void => {
            this.$scope.isLoading= false;
            this.$scope.showDropDown =false;
            this.initCategories();
            this.setFormValidation();

            this.$scope.onGroupSelected = (category: Models.IMainCategory, subcategory: Models.ISubCategory, group: Models.IGroup):void => {
                this.$scope.showDropDown = false;
                this.$scope.product.addGroup(category, subcategory, group);
                group.isDisabled = true;
                this.setFormValidation();
            };

            this.$scope.onInputTextClicked = ():void => {//just edit the component in place, no pop up nor server update ?
                this.$scope.showDropDown = !this.$scope.showDropDown;
            };

            this.$scope.clickOutside = (): any => {
                this.$scope.showDropDown = false;
            };

            this.$scope.deleteGroup = (uniqueId:string) : void => {
                //delete group from component
               this.$scope.product.deleteGroup(uniqueId);
               this.setFormValidation();
                //enabled group
                _.forEach(this.$scope.categoriesOptions, (category: Models.IMainCategory) => {
                    _.forEach(category.subcategories, (subcategory:Models.ISubCategory) => {
                        let groupObj:Models.IGroup = _.find (subcategory.groupings, (group) => {
                            return group.uniqueId === uniqueId;
                        });
                        if(groupObj){
                            groupObj.isDisabled = false;
                        }
                    });
                });
            }
        };

        public save = (callback:Function):void => {
            let onFailed = (response) => {
                callback(false);
            };

            let onSuccess = (component: Models.Components.Component) => {
                this.$scope.product = <Models.Components.Product> this.ComponentFactory.createComponent(component);
                this.$scope.setComponent(this.$scope.product);
                callback(true);
            };

            try {
                this.$scope.product.updateComponent().then(onSuccess, onFailed);
            }catch(e){
                //console.log("ERROR: Error in updating/creating component: " + e);
                callback(false);
            }
        };

        public back = (callback:Function):void => {
            this.save(callback);
        }
    }
}
