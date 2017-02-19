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
 * Created by obarda on 7/28/2016.
 */
/**
 * Created by obarda on 4/4/2016.
 */
/// <reference path="../../../references"/>
module Sdc.ViewModels {
    'use strict';
    import Module = Sdc.Models.Module;

    export interface IHierarchyScope extends ng.IScope {
        component:Models.Components.Component;
        selectedIndex: number;
        selectedModule:Models.DisplayModule;
        singleTab:Models.Tab;
        templateUrl:string;
        isLoading:boolean;

        onModuleSelected(moduleId:string, selectedIndex: number):void;
        onModuleNameChanged(module:Models.DisplayModule):void;
        updateHeatName():void;
    }

    export class HierarchyViewModel {

        static '$inject' = [
            '$scope'
        ];

        constructor(private $scope:IHierarchyScope) {
            this.$scope.component = this.$scope.singleTab.data;
            this.$scope.isLoading = false;
            this.initScopeMethods();
        }

        private initScopeMethods():void {

            this.$scope.templateUrl = '/app/scripts/view-models/tabs/hierarchy/edit-module-name-popover.html';
            this.$scope.onModuleSelected = (moduleId:string, selectedIndex: number):void => {

                let onSuccess = (module:Models.DisplayModule) => {
                    console.log("Module Loaded: ", module);
                    this.$scope.selectedModule = module;
                    this.$scope.isLoading = false;
                };

                let onFailed = () => {
                    this.$scope.isLoading = false;
                };

                this.$scope.selectedIndex = selectedIndex;
                if( !this.$scope.selectedModule || (this.$scope.selectedModule && this.$scope.selectedModule.uniqueId != moduleId)) {
                    this.$scope.isLoading = true;
                    this.$scope.component.getModuleForDisplay(moduleId).then(onSuccess, onFailed);
                }
            };

            this.$scope.updateHeatName = () => {
                this.$scope.isLoading = true;

                let originalName:string = this.$scope.selectedModule.name;

                 let onSuccess = (module:Models.Module) => {
                    console.log("Module name updated:", module.name);
                    this.$scope.selectedModule.name = module.name;
                    this.$scope.isLoading = false;
                };

                let onFailed = () => {
                    this.$scope.isLoading = false;
                    this.$scope.selectedModule.name = originalName;
                };

                this.$scope.selectedModule.updateName();
                this.$scope.component.updateGroupMetadata(new Models.DisplayModule(this.$scope.selectedModule)).then(onSuccess, onFailed);
            };
        }
    }
}
