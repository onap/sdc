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
module Sdc.ViewModels {
    'use strict';

    export interface IWelcomeViewMode {
        slides:Array<Sdc.Directives.SlideData>;
        onCloseButton():void;
        onCloseVideoButton():void;

        video_mp4: string;
        video_ogg: string;
    }

    export class WelcomeViewModel {

        firstLoad:boolean = true;
        alreadyAnimated:Array<number> = [];

        static '$inject' = [
            '$scope',
            '$sce',
            'sdcConfig',
            '$state',
            '$filter'
        ];

        constructor(
            private $scope:IWelcomeViewMode,
            private $sce:any,
            private sdcConfig: Models.IAppConfigurtaion,
            private $state:ng.ui.IStateService,
            private $filter:ng.IFilterService
        ){
            /*this.init();
            this.initScope();
            window.setTimeout(():void => {
                this.loadImages(():void=> {
                    window.setTimeout(():void =>{
                        $(".sdc-welcome-new-page").addClass("animated fadeIn");
                        this.animateGeneral();
                        this.animate1();
                    },1000);
                });
            },0);*/
        }

        private initScope = ():void => {

            this.$scope.onCloseButton = ():void => {
                //console.log("onCloseButton");
                this.$state.go("dashboard", {});
            };

            this.$scope.onCloseVideoButton = ():void => {
                //console.log("onCloseVideoButton");
                $("#sdc-page-scroller").removeClass("animated fadeOut");
                $("#sdc-page-scroller").addClass("animated fadeIn");
                window.setTimeout(()=>{$("#sdc-page-scroller").css("display","block");},0);

                $("#sdc-welcome-video-wrapper").removeClass("animated fadeIn");
                $("#sdc-welcome-video-wrapper").addClass("animated fadeOut");
                window.setTimeout(()=>{$("#sdc-welcome-video-wrapper").css("display","none");},500);

                let videoElement:any = $("#asdc-welcome-video")[0];
                videoElement.pause();
            };

            let url: string = this.sdcConfig.api.welcome_page_video_url;

            this.$scope.video_mp4 = this.$sce.trustAsResourceUrl(url + ".mp4");
            this.$scope.video_ogg = this.$sce.trustAsResourceUrl(url + ".ogg");

        };

        private init = ():void => {
            let viewModelsHtmlBasePath:string = '/app/scripts/view-models/';
            this.$scope.slides = [
                {"url": viewModelsHtmlBasePath + 'welcome/slide0.html', "id": "slide-0", "index": 0, "callback": () => {this.animate0();}},
                {"url": viewModelsHtmlBasePath + 'welcome/slide1.html', "id": "slide-1", "index": 1, "callback": () => {}},
                {"url": viewModelsHtmlBasePath + 'welcome/slide2.html', "id": "slide-2", "index": 2, "callback": () => {this.animate2();}},
                {"url": viewModelsHtmlBasePath + 'welcome/slide3.html', "id": "slide-3", "index": 3, "callback": () => {this.animate3();}},
                {"url": viewModelsHtmlBasePath + 'welcome/slide4.html', "id": "slide-4", "index": 4, "callback": () => {this.animate4();}},
                {"url": viewModelsHtmlBasePath + 'welcome/slide5.html', "id": "slide-5", "index": 5, "callback": () => {this.animate5();}},
                {"url": viewModelsHtmlBasePath + 'welcome/slide6.html', "id": "slide-6", "index": 6, "callback": () => {this.animate6();}}
            ];

            $('body').keyup((e):void=> {
                if (e.keyCode == 27) { // escape key maps to keycode `27`
                    this.$state.go('dashboard');
                }
            });
        };

        private animateGeneral = ():void => {
            //console.log("animateGeneral");

            /*// Animate the right navigation
            if (this.firstLoad===true) {
                //TODO: Israel
                //TweenLite.from('.page-nav', 2, {x: "100px", delay: 2});
            }
            */

            this.firstLoad = false;
        };

        /*private loadImages = (callback: Function):void => {
            let src = $('#slide-1 .asdc-welcome-frame').css('background-image');
            let url = src.match(/\((.*?)\)/)[1].replace(/('|")/g,'');

            let img = new Image();
            img.onload = function() {
                callback();
                //alert('image loaded');
            };
            img.src = url;
            /!*if (img.complete){
                callback;
            }*!/
        };*/

        private animate = (element:any, animation:string, when:number):void => {
            window.setTimeout(()=>{
                element.addClass("animated " + animation);
                if (element[0]) {
                    element[0].style = "visibility: visible;";
                }
            },when);
        };

        private hide = (element:any, animation:string, animationToHide:string, when:number):void => {
            element.addClass("animated " + animation);
            element[0].style="visibility: hidden;";
        };

        private animate0 = ():void => {
            if (this.alreadyAnimated.indexOf(0)!==-1){
                return;
            } else {
                this.alreadyAnimated.push(0);
            }
            //console.log("slide 0 - animate");
            this.animate($('#slide-0 .bg-1'),'fadeInDown',500);
            this.animate($('#slide-0 .bg-2'),'fadeInDown',1000);
            this.animate($('#slide-0 .bg-3'),'fadeInDown',1500);
            this.animate($('#slide-0 .bg-4'),'fadeInDown',2000);

            this.animate($('#slide-0 .bg-5'),'fadeInDown',2500);
            this.animate($('#slide-0 .bg-6'),'fadeInDown',3000);
            this.animate($('#slide-0 .bg-7'),'fadeInDown',3500);
            this.animate($('#slide-0 .bg-8'),'fadeInDown',4000);
        };

        private animate1 = ():void => {
            if (this.alreadyAnimated.indexOf(1)!==-1){
                return;
            } else {
                this.alreadyAnimated.push(1);
            }
            //console.log("slide 1 - animate");

            this.animate($('#slide-1 .asdc-welcome-main-title'),'fadeInUp',1000);
            this.animate($('#slide-1 .asdc-welcome-main-message'),'fadeInUp',2000);

            this.animate($('#slide-1 .asdc-welcome-main-back-btn'),'fadeIn',3000);

            this.animate($('#slide-1 .asdc-welcome-video-icon'),'zoomIn',3000);
            this.animate($('#slide-1 .asdc-welcome-inner-circle'),'zoomIn',3000);

            this.animate($('.welcome-nav'),'slideInRight',2000);
        };

        private animate2 = ():void => {
            if (this.alreadyAnimated.indexOf(2)!==-1){
                return;
            } else {
                this.alreadyAnimated.push(2);
            }
            //console.log("slide 2 - animate");
            this.animate($('#slide-2 .asdc-welcome-frame-shape'),'zoomIn',500);
            this.animate($('#slide-2 .asdc-welcome-slide-text-box-content'),'fadeInUp',2000);
            this.animate($('#slide-2 .asdc-welcome-slide-text-box-title'),'fadeInUp',1000);
        };

        private animate3 = ():void => {
            if (this.alreadyAnimated.indexOf(3)!==-1){
                return;
            } else {
                this.alreadyAnimated.push(3);
            }
            //console.log("slide 3 - animate");
            this.animate($('#slide-3 .asdc-welcome-frame-shape'),'zoomIn',500);
            this.animate($('#slide-3 .asdc-welcome-slide-text-box-content'),'fadeInUp',2000);
            this.animate($('#slide-3 .asdc-welcome-slide-text-box-title'),'fadeInUp',1000);
        };

        private animate4 = ():void => {
            if (this.alreadyAnimated.indexOf(4)!==-1){
                return;
            } else {
                this.alreadyAnimated.push(4);
            }
            //console.log("slide 4 - animate");
            this.animate($('#slide-4 .asdc-welcome-frame-shape'),'zoomIn',500);
            this.animate($('#slide-4 .asdc-welcome-slide-text-box-content'),'fadeInUp',2000);
            this.animate($('#slide-4 .asdc-welcome-slide-text-box-title'),'fadeInUp',1000);
        };

        private animate5 = ():void => {
            if (this.alreadyAnimated.indexOf(5)!==-1){
                return;
            } else {
                this.alreadyAnimated.push(5);
            }
            //console.log("slide 5 - animate");
            this.animate($('#slide-5 .asdc-welcome-frame-shape'),'zoomIn',500);
            this.animate($('#slide-5 .asdc-welcome-slide-text-box-content'),'fadeInUp',2000);
            this.animate($('#slide-5 .asdc-welcome-slide-text-box-title'),'fadeInUp',1000);
        };

        private animate6 = ():void => {
            if (this.alreadyAnimated.indexOf(6)!==-1){
                return;
            } else {
                this.alreadyAnimated.push(6);
            }
            //console.log("slide 6 - animate");
            this.animate($('#slide-6 .asdc-welcome-main-message'),'fadeInUp',2000);
            this.animate($('#slide-6 .asdc-welcome-main-title'),'fadeInUp',1000);
            this.animate($('#slide-6 .asdc-welcome-main-back-btn'),'fadeInUp',3000);
        };

        private animateCss = (element:JQuery, animationName:string):void => {
            let animationEnd = 'webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend';
            element.addClass('animated ' + animationName).one(animationEnd, function() {
                element.removeClass('animated ' + animationName);
            });
        };

        private unAnimateCss = (element:JQuery, animationName:string):void => {
            let animationEnd = 'webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend';
            element.addClass('animated ' + animationName).one(animationEnd, function() {
                element.removeClass('animated ' + animationName);
            });
        };

    }
}
