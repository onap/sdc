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
/// <reference path="../../references"/>
module Sdc.Directives {
    'use strict';
    export interface ITutorialScope extends ng.IScope {
        showTutorial:boolean;
        isFirstTime:boolean;
        templateUrl:string;
        totalPages: number;
        currentPageIndex: number;
        page:number;
        tabs:Array<string>;
        tutorialData:any;
        pageObject:any;

        initPage:Function;
        next:Function;
        previous:Function;
        hasNext():boolean;
        hasPrevious():boolean;
        closeTutorial:Function;
        closeAndShowLastPage:Function;
    }

    export class TutorialDirective implements ng.IDirective {

        constructor(
            private $templateCache:ng.ITemplateCacheService,
            private sdcConfig:Models.IAppConfigurtaion,
            private $state:ng.ui.IStateService
        ) {
        }

        scope = {
            page: '=',
            showTutorial: '=',
            isFirstTime: '='
        };

        replace = false;
        restrict = 'EA';
        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/tutorial/tutorial-directive.html');
        };

        link = (scope:ITutorialScope, $elem:any) => {

            let findPageIndex:Function = (pageId:number):number=> {
                for (let i:number=0;i<scope.totalPages;i++){
                    if (scope.tutorialData.pages[i].id===pageId){
                        return i;
                    }
                }
                return -1;
            }

            let showCurrentPage:Function = ():void=> {
                scope.pageObject = scope.tutorialData.pages[scope.currentPageIndex];
                scope.templateUrl = '/app/scripts/directives/tutorial/' + scope.pageObject.template + '.html';
            }

            scope.tutorialData = this.sdcConfig.tutorial;

            scope.closeTutorial = ()=> {
                scope.showTutorial = false;
                if(scope.isFirstTime){
                    scope.isFirstTime=false;
                }
            }

            scope.closeAndShowLastPage = ()=> {
                if(scope.isFirstTime){
                    this.$state.go('dashboard.tutorial-end');
                }
                scope.closeTutorial();
            }

            let init:Function = ():void => {
                scope.tabs = scope.tutorialData.tabs;
                scope.totalPages = scope.tutorialData.pages.length;
                scope.initPage(scope.page);

            }

            scope.initPage = (pageId) => {
                scope.currentPageIndex = findPageIndex(pageId);
                showCurrentPage();
            }

            scope.next = ():void => {
                if (scope.hasNext()){
                    scope.currentPageIndex++;
                    showCurrentPage();
                }
            }

            scope.previous = ():void => {
                if (scope.hasPrevious()){
                    scope.currentPageIndex--;
                    showCurrentPage();
                }
            }

            scope.hasNext = ():boolean => {
                return (scope.currentPageIndex+1) < scope.totalPages;
            }

            scope.hasPrevious = ():boolean => {
                return scope.currentPageIndex>0;
            }

            angular.element(document).ready(function () {
                init();
            });

            scope.$watch('showTutorial', (showTutorial:any):void => {
                scope.initPage(scope.page);
            });

        };

        public static factory = ($templateCache:ng.ITemplateCacheService, sdcConfig:Models.IAppConfigurtaion, $state:ng.ui.IStateService)=> {
            return new TutorialDirective($templateCache, sdcConfig, $state);
        };

    }

    TutorialDirective.factory.$inject = ['$templateCache', 'sdcConfig', '$state'];
}
