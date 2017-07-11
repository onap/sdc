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

export interface IPerfectScrollerScope extends ng.IScope {
    //update(event:string): void;
}

export class PerfectScrollerDirective implements ng.IDirective {

    constructor(private $parse:any) {

    }

    replace = true;
    restrict = 'EA';
    transclude = true;

    template = ():string => {
        return '<div><div ng-transclude=""></div></div>';
    };

    link = ($scope:IPerfectScrollerScope, $elem, $attr) => {
        let self = this;
        let options = {};

        let psOptions = [
            'wheelSpeed', 'wheelPropagation', 'minScrollbarLength', 'useBothWheelAxes',
            'useKeyboard', 'suppressScrollX', 'suppressScrollY', 'scrollXMarginOffset',
            'scrollYMarginOffset', 'includePadding'//, 'onScroll', 'scrollDown'
        ];

        for (let i = 0, l = psOptions.length; i < l; i++) {
            let opt = psOptions[i];
            if ($attr[opt] !== undefined) {
                options[opt] = self.$parse($attr[opt])();
            }
        }

        $scope.$evalAsync(function () {
            $elem.perfectScrollbar(options);
            let onScrollHandler = self.$parse($attr.onScroll)
            $elem.scroll(function () {
                let scrollTop = $elem.scrollTop()
                let scrollHeight = $elem.prop('scrollHeight') - $elem.height()
                $scope.$apply(function () {
                    onScrollHandler($scope, {
                        scrollTop: scrollTop,
                        scrollHeight: scrollHeight
                    })
                })
            });
        });

        /*
         $scope.update = (event:string): void => {
         $scope.$evalAsync(function() {
         //if ($attr.scrollDown == 'true' && event != 'mouseenter') {
         if (event != 'mouseenter') {
         setTimeout(function () {
         $($elem).scrollTop($($elem).prop("scrollHeight"));
         }, 100);
         }
         $elem.perfectScrollbar('update');
         });
         };
         */

        // This is necessary when you don't watch anything with the scrollbar
        $elem.bind('mouseenter', function () {
            //console.log("mouseenter");
            $elem.perfectScrollbar('update');
        });

        $elem.bind('mouseleave', function () {
            //console.log("mouseleave");
            setTimeout(function () {
                $(window).trigger('mouseup');
                $elem.perfectScrollbar('update');
            }, 10);
        });

        $elem.bind('click', function () {
            //console.log("click");
            // Wait 500 milliseconds until the collapse finish closing and update.
            setTimeout(function () {
                $elem.perfectScrollbar('update');
            }, 500);
        });

        /**
         * Check if the content of the scroller was changed, and if changed update the scroller.
         * Because DOMSubtreeModified event is fire many time (while filling the content), I'm checking that
         * there is at least 100 milliseconds between DOMSubtreeModified events to update the scrollbar.
         * @type {boolean}
         */
        let insideDOMSubtreeModified = false;
        $elem.bind('DOMSubtreeModified', function () {
            if (insideDOMSubtreeModified == false) {
                insideDOMSubtreeModified = true;
                setTimeout(function () {
                    insideDOMSubtreeModified = false;
                    $elem.perfectScrollbar('update');
                }, 100);
            }
        });

        // Possible future improvement - check the type here and use the appropriate watch for non-arrays
        if ($attr.refreshOnChange) {
            $scope.$watchCollection($attr.refreshOnChange, function () {
                $elem.perfectScrollbar('update');
            });
        }

        $elem.bind('$destroy', function () {
            $elem.perfectScrollbar('destroy');
        });

    };

    public static factory = ($parse:any)=> {
        return new PerfectScrollerDirective($parse);
    };

}

PerfectScrollerDirective.factory.$inject = ['$parse'];
