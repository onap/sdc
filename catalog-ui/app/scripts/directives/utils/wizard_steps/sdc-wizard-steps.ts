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
/// <reference path="../../../references"/>
module Sdc.Directives {

    'use strict';

    export interface IWizardStep {
        name: string;
        selected?: boolean;
        valid?:boolean;
        enabled?:boolean;
        callback: Function;
    }

    export interface ISdcWizardStepScope extends ng.IScope {
        steps:Array<IWizardStep>;
        control:any;
        internalControl:any;

        stepClicked(stepName:string):void;
        controllerStepClicked(stepName:string):void;

        setStepValidity(stepName:string, valid:boolean):void;
        controllerSetStepValidity(step:IWizardStep, valid:boolean):void;
    }

    export interface SdcWizardStepMethods {
        unSelectAllSteps():void;
        selectStep(step:IWizardStep):void;
    }

    export class SdcWizardStepDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService) {
        }

        scope = {
            steps: '=',
            control: '='
        };

        public replace = false;
        public restrict = 'E';
        public transclude = true;
        public controller = SdcWizardStepDirectiveController;

        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/utils/wizard_steps/sdc-wizard-steps.html');
        };

        link = (scope:ISdcWizardStepScope, $elem:JQuery, attr:any, controller:SdcWizardStepDirectiveController) => {
            scope.internalControl = scope.control || {};
            scope.internalControl.stepClicked = (step:string):void => {
                scope.controllerStepClicked(step);
            };

            scope.internalControl.setStepValidity = (step:IWizardStep, valid:boolean):void => {
                scope.controllerSetStepValidity(step, valid);
            };
        }

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new SdcWizardStepDirective($templateCache);
        };

    }

    SdcWizardStepDirective.factory.$inject = ['$templateCache'];

    export class SdcWizardStepDirectiveController {
        static $inject = ['$element', '$scope'];

        methods:SdcWizardStepMethods = <SdcWizardStepMethods>{};

        constructor(public $element: JQuery,
                    public $scope: ISdcWizardStepScope) {

            this.initMethods();
            this.initScope();
        }

        private initScope = ():void => {

            this.$scope.controllerStepClicked = (stepName:string):void => {
                let selectedStep:IWizardStep = <IWizardStep>_.find(this.$scope.steps, function (item) {
                    return item.name === stepName;
                });

                if (selectedStep && selectedStep.enabled===true){
                    let result:boolean = selectedStep.callback();
                    if (result===true){
                        this.methods.unSelectAllSteps();
                        this.methods.selectStep(selectedStep);
                    }
                }
            };

            this.$scope.controllerSetStepValidity = (step:IWizardStep, valid:boolean):void => {
                step.valid=valid;
            };

        };

        private initMethods = ():void => {

            this.methods.unSelectAllSteps = ():void => {
                this.$scope.steps.forEach(function (step) {
                    step.selected = false;
                });
            }

            this.methods.selectStep = (step:IWizardStep):void => {
                if (step.enabled===true){
                    step.selected=true;
                }
            }
        };

    }

}
