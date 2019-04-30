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

export interface IWelcomeViewMode {
    onCloseButtonClick():void;
}

export class WelcomeViewModel {

    firstLoad:boolean = true;
    alreadyAnimated:Array<number> = [];

    static '$inject' = [
        '$scope',
        '$state'
    ];

    constructor(private $scope:IWelcomeViewMode,
                private $state:ng.ui.IStateService
               ) {
        this.init();
        this.initScope();
        window.setTimeout(():void => {
            this.loadImages(():void=> {
                window.setTimeout(():void =>{
                    $(".sdc-welcome-new-page").addClass("animated fadeIn");
                    window.setTimeout(():void =>{
                      $(".sdc-welcome-new-page").addClass("animated fadeOut");
                      window.setTimeout(():void =>{
                        this.$state.go("dashboard", {});
                      },1000);
                    },3000);
                },0);
            });
        },0);
    }

    private initScope = ():void => {
        this.$scope.onCloseButtonClick = ():void => {
            this.$state.go("dashboard", {});
        };
    };

    private init = ():void => {
        let viewModelsHtmlBasePath:string = 'src/app/view-models/';
        $('body').keyup((e):void=> {
            if (e.keyCode == 27) { // escape key maps to keycode `27`
                this.$state.go('dashboard');
            }
        });
    };

    private loadImages = (callback:Function):void => {
        let src = $('.sdc-welcome-wrapper').css('background-image');
        let url = src.match(/\((.*?)\)/)[1].replace(/('|")/g,'');

        let img = new Image();
        img.onload = function() {
            callback();
        };
        img.src = url;
    };

}
