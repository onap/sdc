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
'use strict'
import {Match, ConnectRelationModel} from "app/models";
import {Component} from "../../../models/components/component";

export interface IRelationMenuScope extends ng.IScope {
    relationMenuDirectiveObj:ConnectRelationModel;
    createRelation:Function;
    isLinkMenuOpen:boolean;
    hideRelationMatch:Function;
    cancel:Function;

    saveRelation();
    showMatch(arr1:Array<Match>, arr2:Array<Match>):boolean;
    hasMatchesToShow(matchesObj:Match, selectedMatch:Array<Match>);
    updateSelectionText():void;

}


export class RelationMenuDirective implements ng.IDirective {

    constructor(private $filter:ng.IFilterService) {
    }

    scope = {
        relationMenuDirectiveObj: '=',
        isLinkMenuOpen: '=',
        createRelation: '&',
        cancel: '&'
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./relation-menu.html');
    };

    link = (scope:IRelationMenuScope, element:JQuery, $attr:ng.IAttributes) => {

        scope.saveRelation = ():void=> {
            let chosenMatches:Array<any> = _.intersection(scope.relationMenuDirectiveObj.rightSideLink.selectedMatch, scope.relationMenuDirectiveObj.leftSideLink.selectedMatch);
            let chosenMatch:Match = chosenMatches[0];
            scope.createRelation()(chosenMatch);
        };


        scope.hideRelationMatch = () => {
            scope.isLinkMenuOpen = false;
            scope.cancel();
        };

        //to show options in link menu
        scope.showMatch = (arr1:Array<Match>, arr2:Array<Match>):boolean => {
            return !arr1 || !arr2 || _.intersection(arr1, arr2).length > 0;
        };

        //to show requirements/capabilities title
        scope.hasMatchesToShow = (matchesObj:Match, selectedMatch:Array<Match>):boolean => {
            let result:boolean = false;
            _.forEach(matchesObj, (matchesArr:Array<Match>) => {
                if (!result) {
                    result = scope.showMatch(matchesArr, selectedMatch);
                }
            });
            return result;
        };


        scope.updateSelectionText = ():void => {
            let left:string = scope.relationMenuDirectiveObj.leftSideLink.selectedMatch ? this.$filter('resourceName')(scope.relationMenuDirectiveObj.leftSideLink.selectedMatch[0].getDisplayText('left')) : '';
            let both:string = scope.relationMenuDirectiveObj.leftSideLink.selectedMatch && scope.relationMenuDirectiveObj.rightSideLink.selectedMatch ? ' - ' +
            this.$filter('resourceName')(scope.relationMenuDirectiveObj.leftSideLink.selectedMatch[0].requirement.relationship) + ' - ' : '';
            let right:string = scope.relationMenuDirectiveObj.rightSideLink.selectedMatch ? this.$filter('resourceName')(scope.relationMenuDirectiveObj.rightSideLink.selectedMatch[0].getDisplayText('right')) : '';
            scope.relationMenuDirectiveObj.selectionText = left + both + right;
        };


    }
    public static factory = ($filter:ng.IFilterService)=> {
        return new RelationMenuDirective($filter);
    };
}

RelationMenuDirective.factory.$inject = ['$filter'];
