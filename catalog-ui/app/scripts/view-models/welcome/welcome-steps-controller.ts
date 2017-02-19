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

    export interface IWelcomeStepsController {
        video_mp4: string;
        video_ogg: string;
        onPlayVideo: Function;
    }

    export class WelcomeStepsControllerViewModel {

        static '$inject' = [
            '$scope',
            '$sce',
            'sdcConfig',
            '$state',
            '$filter'
        ];

        constructor(
            private $scope:IWelcomeStepsController,
            private $sce:any,
            private sdcConfig: Models.IAppConfigurtaion,
            private $state:ng.ui.IStateService,
            private $filter:ng.IFilterService
        ){
            this.init();
            this.initScope();
        }

        private init = ():void => {

        };

        private initScope = ():void => {

            this.$scope.onPlayVideo = ():void => {
                //console.log("onPlayVideo");
                $("#sdc-page-scroller").removeClass("animated fadeIn");
                $("#sdc-page-scroller").addClass("animated fadeOut");
                window.setTimeout(()=>{$("#sdc-page-scroller").css("display","none");},500);

                $("#sdc-welcome-video-wrapper").removeClass("animated fadeOut");
                $("#sdc-welcome-video-wrapper").addClass("animated fadeIn");
                window.setTimeout(()=>{$("#sdc-welcome-video-wrapper").css("display","block");},0);

                let videoElement:any = $("#asdc-welcome-video")[0];
                videoElement.play();
            };

        };

    }
}
