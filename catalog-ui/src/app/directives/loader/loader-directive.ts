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
'use strict';
import {EVENTS} from "app/utils";
import {EventListenerService} from "app/services";

export interface ILoaderScope extends ng.IScope {
    display:boolean;        // Toggle show || hide scroll
    size:string;            // small || medium || large
    elementSelector:string; // Jquery selector to hide and scroll inside
    relative:boolean;       // Will use the parent of <loader> element and hide it and scroll inside
    loaderType:string;
}

export class LoaderDirective implements ng.IDirective {

    constructor(private EventListenerService:EventListenerService) {
    }

    /*
     * relative is used when inserting the HTML loader inside some div <loader data-display="isLoading" relative="true"></loader>
     * elementSelector when we want to pass the Jquery selector of the loader.
     */
    scope = {
        display: '=',
        size: '@?',
        elementSelector: '@?',
        relative: '=?',
        loaderType: '@?'
    };

    public replace = false;
    public restrict = 'E';
    template = ():string => {
        return require('./loader-directive.html');
    }

    link = (scope:ILoaderScope, element:any) => {

        let interval;

        if(scope.loaderType) {
            this.EventListenerService.registerObserverCallback(EVENTS.SHOW_LOADER_EVENT + scope.loaderType, (loaderType)=> {
                scope.display = true;
            });
            this.EventListenerService.registerObserverCallback(EVENTS.HIDE_LOADER_EVENT + scope.loaderType, (loaderType)=> {
                scope.display = false;
            });
        }
        let calculateSizesForFixPosition = (positionStyle:string):void => {
            // This is problematic, I do not want to change the parent position.
            // set the loader on all the screen
            let parentPosition = element.parent().position();
            let parentWidth = element.parent().width();
            let parentHeight = element.parent().height();
            element.css('position', positionStyle);
            element.css('top', parentPosition.top);
            element.css('left', parentPosition.left);
            element.css('width', parentWidth);
            element.css('height', parentHeight);
        };

        let setStyle = (positionStyle:string):void => {

            switch (positionStyle) {
                case 'absolute':
                case 'fixed':
                    // The parent size is not set yet, still loading, so need to use interval to update the size.
                    interval = window.setInterval(()=> {
                        calculateSizesForFixPosition(positionStyle);
                    }, 2000);
                    break;
                default:
                    // Can change the parent position to relative without causing style issues.
                    element.parent().css('position', 'relative');
                    break;
            }
        };

        // This should be executed after the dom loaded
        window.setTimeout(():void => {

            element.css('display', 'none');

            if (scope.elementSelector) {
                let elemParent = angular.element(scope.elementSelector);
                let positionStyle:string = elemParent.css('position');
                setStyle(positionStyle);
            }

            if (scope.relative === true) {
                let positionStyle:string = element.parent().css('position');
                setStyle(positionStyle);
            }

            if (!scope.size) {
                scope.size = 'large';
            }

        }, 0);

        if (scope.elementSelector) {

        }

        function cleanUp() {
            clearInterval(interval);
        }

        scope.$watch("display", (newVal, oldVal) => {
            element.css('display', 'none');
            if (newVal === true) {
                window.setTimeout(():void => {
                    element.css('display', 'block');
                }, 500);
            } else {
                window.setTimeout(():void => {
                    element.css('display', 'none');
                }, 0);
            }
        });

        scope.$on('$destroy', cleanUp);

    };

    public static factory = (EventListenerService:EventListenerService)=> {
        return new LoaderDirective( EventListenerService);
    };

}

LoaderDirective.factory.$inject = ['EventListenerService'];
