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

    export interface SlideData {
        url: string;
        id: string;
        index: number;
        callback: Function;
    }

    export interface ISdcPageScrollDirectiveScope extends ng.IScope {
        slidesData:Array<SlideData>;
        showNav: boolean;
        showPreviousNext: boolean;
        currentSlide:SlideData;
        showCloseButton:boolean;
        closeButtonCallback:Function;
        startSlideIndex:number;

        onNavButtonClick(slideName):void;
        onCloseButtonClick():void;
        goToPrevSlide():void;
        goToNextSlide():void;
        goToSlide(slide:SlideData):void;
        onSlideChangeEnd():void;
        onMouseWheel(event):void;
        onKeyDown(event):void;
        onResize(event):void;
        gotoSlideIndex(index):void;
    }

    export class SdcPageScrollDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService) {

        }

        scope = {
            slidesData: '=',
            showNav: '=',
            showPreviousNext: '=',
            showCloseButton: '=',
            closeButtonCallback: '=',
            startSlideIndex: '=?'
        };

        public replace = true;
        public restrict = 'E';
        private delayExec:any;

        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/page-scroller/page-scroller.html');
        };

        link = ($scope:ISdcPageScrollDirectiveScope, $elem:JQuery, attr:any) => {
            let isAnimating = false; //Animating flag - is our app animating
            let pageHeight = $(window).innerHeight(); //The height of the window
            let slidesContainer;
            let navButtons;
            let slides:any; //Only graph-links that starts with

            //Key codes for up and down arrows on keyboard. We'll be using this to navigate change slides using the keyboard
            let keyCodes = {
                UP  : 38,
                DOWN: 40
            };

            $scope.onCloseButtonClick = ():void => {
                if ($scope.closeButtonCallback){
                    $scope.closeButtonCallback();
                };
            };

            // Wait for the dom to load (after ngRepeat).
            $scope.$on('onRepeatLast', (scope, element, attrs) => {
                slides = $(".slide", slidesContainer);
                slidesContainer = $(".slides-container");
                navButtons = $("nav a").filter("[href^='#']");

                // Adding event listeners
                $(window).on("resize", (e) => {$scope.onResize(e);}).resize();
                $(window).on("mousewheel DOMMouseScroll", (e) => {$scope.onMouseWheel(e);});
                $(document).on("keydown", (e) => {$scope.onKeyDown(e);});

                //Going to the first slide
                if ($scope.startSlideIndex){
                    $scope.gotoSlideIndex($scope.startSlideIndex);
                } else {
                    $scope.gotoSlideIndex(0);
                }

            });

            $scope.gotoSlideIndex = (index) => {
                $scope.goToSlide($scope.slidesData[index]);
            };

            // When a button is clicked - first get the button href, and then slide to the container, if there's such a container
            $scope.onNavButtonClick = (slide:SlideData):void => {
                $scope.goToSlide(slide);
            };

            // If there's a previous slide, slide to it
            $scope.goToPrevSlide = ():void => {
                let previousSlide = $scope.slidesData[$scope.currentSlide.index-1];
                if (previousSlide) {
                    $scope.goToSlide(previousSlide);
                }
            };

            // If there's a next slide, slide to it
            $scope.goToNextSlide = ():void => {
                let nextSlide = $scope.slidesData[$scope.currentSlide.index+1];
                if (nextSlide) {
                    $scope.goToSlide(nextSlide);
                }
            };

            // Actual transition between slides
            $scope.goToSlide = (slide:SlideData):void => {
                //console.log("start goToSlide");
                //If the slides are not changing and there's such a slide
                if(!isAnimating && slide) {
                    //setting animating flag to true
                    isAnimating = true;
                    $scope.currentSlide = slide;
                    $scope.currentSlide.callback();

                    //Sliding to current slide
                    let calculatedY = pageHeight * ($scope.currentSlide.index);
                    //console.log("$scope.currentSlide.index: " + $scope.currentSlide.index + " | calculatedY: " + calculatedY);

                    $('.slides-container').animate(
                        {
                            scrollTop: calculatedY + 'px'
                        },
                        {
                            duration: 1000,
                            specialEasing: {
                                width: "linear",
                                height: "easeInOutQuart"
                            },
                            complete: function() {
                                $scope.onSlideChangeEnd();
                            }
                        }
                    );

                    //Animating menu items
                    $(".sdc-page-scroller nav a.active").removeClass("active");
                    $(".sdc-page-scroller nav [href='#" + $scope.currentSlide.id + "']").addClass("active");
                }
            };

            // Once the sliding is finished, we need to restore "isAnimating" flag.
            // You can also do other things in this function, such as changing page title
            $scope.onSlideChangeEnd = ():void => {


                
                isAnimating = false;
            };

            // When user scrolls with the mouse, we have to change slides
            $scope.onMouseWheel = (event):void => {
                //Normalize event wheel delta
                let delta = event.originalEvent.wheelDelta / 30 || -event.originalEvent.detail;

                //If the user scrolled up, it goes to previous slide, otherwise - to next slide
                if(delta < -1) {
                    this.delayAction($scope.goToNextSlide);
                } else if(delta > 1) {
                    this.delayAction($scope.goToPrevSlide);
                }
                event.preventDefault();
            };

            // Getting the pressed key. Only if it's up or down arrow, we go to prev or next slide and prevent default behaviour
            // This way, if there's text input, the user is still able to fill it
            $scope.onKeyDown = (event):void => {
                let PRESSED_KEY = event.keyCode;

                if(PRESSED_KEY == keyCodes.UP){
                    $scope.goToPrevSlide();
                    event.preventDefault();
                } else if(PRESSED_KEY == keyCodes.DOWN){
                    $scope.goToNextSlide();
                    event.preventDefault();
                }
            };

            // When user resize it's browser we need to know the new height, so we can properly align the current slide
            $scope.onResize = (event):void => {
                //This will give us the new height of the window
                let newPageHeight = $(window).innerHeight();

                // If the new height is different from the old height ( the browser is resized vertically ), the slides are resized
                if(pageHeight !== newPageHeight) {
                    pageHeight = newPageHeight;
                }
            };
        };

        private initSlides = ():void => {
            //pageHeight
        };

        private delayAction = (action:Function):void => {
            clearTimeout(this.delayExec);
            this.delayExec = setTimeout(function () {
                action();
            }, 100);
        };

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new SdcPageScrollDirective($templateCache);
        };

    }

    SdcPageScrollDirective.factory.$inject = ['$templateCache'];

}




