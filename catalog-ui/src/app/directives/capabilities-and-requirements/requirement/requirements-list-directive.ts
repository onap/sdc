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
import {RequirementsGroup, Component} from "app/models";

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
                let relationItem = _.filter(scope.component.componentInstancesRelations, (relation:any) => {
                    return relation.fromNode === scope.component.selectedInstance.uniqueId &&
                        _.some(relation.relationships, {
                            'requirement': requirement.name,
                            'requirementOwnerId': requirement.ownerId
                        });
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
