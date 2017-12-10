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
 * Created by ob0695 on 5/9/2017.
 */
/**
 * Created by ob0695 on 5/9/2017.
 */
/**
 * Created by obarda on 1/8/2017.
 */
'use strict';
import {RequirementsGroup, Component, Relationship, RelationshipModel} from "app/models";

export interface IRequirementsListScope extends ng.IScope {

    requirements:RequirementsGroup;
    currentComponent: Component;
}


export class RequirementsListDirective implements ng.IDirective {

    constructor(private $filter: ng.IFilterService) {

    }

    scope = {
        requirements: '=',
        component: '='
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./requirements-list-view.html');
    };

    link = (scope:IRequirementsListScope) => {

        scope.isInstanceSelected = () : boolean => {
            return  scope.component && scope.component.selectedInstance != undefined && scope.component.selectedInstance != null;
        }

        scope.getRelation = (requirement:any):any => {
            if (scope.isInstanceSelected() && scope.component.componentInstancesRelations) {
                let relationItem:Array<RelationshipModel> = _.filter((<Component>scope.component).componentInstancesRelations, (relation:RelationshipModel) => {
                    return relation.fromNode === scope.component.selectedInstance.uniqueId &&
                        _.filter(relation.relationships, (relationship:Relationship) => {
                            return relationship.relation.requirement == requirement.name && relationship.relation.requirementOwnerId == requirement.ownerId;
                        }).length;
                });

                if (relationItem && relationItem.length) {
                    return {
                        type: requirement.relationship.split('.').pop(),
                        requirementName: this.$filter('resourceName')(scope.component.componentInstances[_.map
                        (scope.component.componentInstances, "uniqueId").indexOf(relationItem[0].toNode)].name)
                    };
                }
            }
            return null;
        };

    };

    public static factory = ($filter: ng.IFilterService)=> {
        return new RequirementsListDirective($filter);
    };
}

RequirementsListDirective.factory.$inject = ['$filter'];
