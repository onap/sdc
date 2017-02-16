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
module Sdc.ViewModels {
    'use strict';

    interface IAddCategoryModalViewModelScope extends ng.IScope {
        category:Sdc.Services.ICategoryResource;
        modelType:string;
        footerButtons: Array<any>;
        forms:any;

        save():void;
        close():void;
    }

    export class AddCategoryModalViewModel {

        static '$inject' = [
            '$scope',
            'Sdc.Services.CategoryResourceService',
            '$modalInstance',
            'parentCategory',
            'type'
        ];

        constructor(
            private $scope:IAddCategoryModalViewModelScope,
            private categoryResourceService:Sdc.Services.ICategoryResourceClass,
            private $modalInstance:ng.ui.bootstrap.IModalServiceInstance,
            private parentCategory:Sdc.Services.ICategoryResource,
            private type:string
        ){
            this.initScope();
        }

        private initScope = ():void => {
            this.$scope.forms = {};
            this.$scope.modelType = this.parentCategory ? 'sub category' : 'category';
            this.$scope.category = new this.categoryResourceService();

            this.$scope.close = ():void => {
                this.$modalInstance.dismiss();
            };

            this.$scope.save = ():void => {

                let onOk = (newCategory :Sdc.Services.ICategoryResource):void => {
                    this.$modalInstance.close(newCategory);
                };

                let onCancel = ():void => {
                    //error
                };

                if(!this.parentCategory) {
                    this.$scope.category.$save({types: this.type+"s"}, onOk, onCancel);
                }else{
                    this.$scope.category.$saveSubCategory({types: this.type+"s", categoryId: this.parentCategory.uniqueId}, onOk, onCancel);
                }

            };

            this.$scope.footerButtons = [
                {'name': 'OK', 'css': 'blue', 'callback': this.$scope.save, 'disabled': true},
                {'name': 'Cancel', 'css': 'grey', 'callback': this.$scope.close}
            ];

            this.$scope.$watch("forms.editForm.$invalid", (newVal, oldVal) => {
                this.$scope.footerButtons[0].disabled = this.$scope.forms.editForm.$invalid;
            });

        }


    }
}
