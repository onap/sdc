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
/// <reference path="../../../../../../references"/>>
module Sdc.ViewModels {
    'use strict';

    interface IRelationsViewModelScope extends ICompositionViewModelScope {
        isLoading: boolean;
        $parent: ICompositionViewModelScope;
        getRelation(requirement:any): any;
    }

    export class RelationsViewModel {

        static '$inject' = [
            '$scope',
            '$filter'
        ];

        constructor(private $scope:IRelationsViewModelScope,
                    private $filter:ng.IFilterService) {
            this.initScope();
        }


        private updateRC = ():void =>{
            if(this.$scope.currentComponent) {
                this.$scope.currentComponent.updateRequirementsCapabilities();
            }
        };

        private initScope = ():void => {

            this.$scope.isLoading = this.$scope.$parent.isLoading;

            this.$scope.getRelation = (requirement:any):any => {

                if(this.$scope.isComponentInstanceSelected() && this.$scope.currentComponent.componentInstancesRelations ) {
                    let relationItem = _.filter(this.$scope.currentComponent.componentInstancesRelations, (relation:any) => {
                        return relation.fromNode === this.$scope.currentComponent.selectedInstance.uniqueId &&
                                _.some(relation.relationships, {'requirement': requirement.name,
                                                                'requirementOwnerId': requirement.ownerId});
                    });

                    if (relationItem && relationItem.length) {
                        return {
                            type: requirement.relationship.split('.').pop(),
                            requirementName: this.$filter('resourceName')(this.$scope.currentComponent.componentInstances[_.map
                            (this.$scope.currentComponent.componentInstances, "uniqueId").indexOf(relationItem[0].toNode)].name)
                        };
                    }
                }
                return null;
            };

            if(!this.$scope.isComponentInstanceSelected()) {
                this.$scope.$watch('currentComponent.componentInstances + currentComponent.componentInstancesRelations', ():void => {
                    this.updateRC();
                });
                
            }
        }
    }
}
