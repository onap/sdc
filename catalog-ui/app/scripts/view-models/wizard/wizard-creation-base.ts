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
module Sdc.ViewModels.Wizard {
    'use strict';

    export class StepNames {
        static general = "General";
        static icon = "Icon";
        static deploymentArtifact = "Deployment Artifact";
        static informationArtifact = "Information Artifact";
        static properties = "Properties";
        static hierarchy = "Hierarchy";
    }

    class FooterButtons {
        static cancel = "Cancel";
        static back = "Back";
        static next = "Next";
        static finish = "Finish";
    }

    export class WizardCreationTypes {
        static importAsset = "importAsset";
        static create = "create";
        static edit = "edit";
    }

    export class _CreationStep {
        name:string;
        url:string;
    }

    export class CurrentStep {
        assetCreationStep:_CreationStep;
        reference:IWizardCreationStep;
        index:number;
        valid: boolean;
    }

    export interface IWizardCreationStep {
        save(callback:Function):void;
        back(callback:Function):void;
    }

    export interface IWizardCreationStepScope extends ng.IScope {
        data:any;
        getComponent():Sdc.Models.Components.Component;
        setComponent(component: Sdc.Models.Components.Component):void;
        registerChild(child:IWizardCreationStep):void; // Called from the step
        setValidState(valid:boolean):void; // Called from the step
    }

    export interface IWizardCreationScope extends ng.IScope {
        isLoading: boolean;
        data:any; // data passed from dashboard (opener), need it on the scope, because general step will use this (extends the scope)
        directiveSteps: Array<Sdc.Directives.IWizardStep>; // Steps for the directive, on the scope (on the scope because need to pass to directive via HTML)
        templateUrl: string; // On the scope because need to pass to <ng-include> via HTML
        footerButtons: Array<Sdc.Directives.ISdcModalButton>; // Wizard footer buttons (on the scope because need to pass to directive via HTML)
        assetCreationControl:any; // Link to wizard directive functions.
        modalInstance:ng.ui.bootstrap.IModalServiceInstance; // Reference to the modal, so we can close it (on the scope because need to pass to directive via HTML)
        assetName:string;
        assetType:string;
        modalTitle:string;
        getComponent():Sdc.Models.Components.Component;
        setComponent(component: Sdc.Models.Components.Component):void;
        registerChild(child:IWizardCreationStep):void; // Called from the step
        setValidState(valid:boolean):void; // Called from the step
    }

    export class WizardCreationBaseViewModel {

        component: Sdc.Models.Components.Component;
        protected assetCreationSteps: Array<_CreationStep>; // Contains URL and name so we can replace them
        currentStep:CurrentStep;
        protected type:string;

        constructor(protected $scope:IWizardCreationScope,
                    protected data:any,
                    protected ComponentFactory: Utils.ComponentFactory,
                    protected $modalInstance: ng.ui.bootstrap.IModalServiceInstance
          ) {

            this.$scope.data = data;
            this.currentStep = new CurrentStep();
            this.currentStep.valid=false;
            this.$scope.modalInstance = this.$modalInstance;
            this.initScope();
            this.noBackspaceNav();

            // In case the modal was opened with filled resource (edit mode).
            if (data.component){
                    this.$scope.setComponent(data.component);
                    data.componentType = this.$scope.getComponent().componentType;
                    window.setTimeout(()=>{
                        this.safeApply(this.setCurrentStepByIndex(0, false));
                    },100);
            } else {
                // Default step to start with
                window.setTimeout(()=>{
                    this.safeApply(this.setCurrentStepByIndex(0, false));
                },100);
            }

        }

        private safeApply = (fn:any) => {
            let phase = this.$scope.$root.$$phase;
            if (phase == '$apply' || phase == '$digest') {
                if (fn && (typeof(fn) === 'function')) {
                    fn();
                }
            } else {
                this.$scope.$apply(fn);
            }
        };

        private initScope = ():void => {

            // Control to call functions on wizard step directive
            this.$scope.assetCreationControl = {};

            // Footer buttons definitions for the modal directive.
            this.$scope.footerButtons = [
                {"name":FooterButtons.cancel,   "css":'white cancel',"callback": ()=>{this.btnCancelClicked();}},
                {"name":FooterButtons.back,     "disabled":true, "css":'white back',"callback": ()=>{this.btnBackClicked();}},
                {"name":FooterButtons.next,     "disabled":true, "css":'blue next',"callback": ()=>{this.btnNextClicked();}},
                {"name":FooterButtons.finish,   "disabled":true, "css":'white finish',"callback": ()=>{this.btnFinishedClicked();}}
            ];

            // Will be called from step constructor to register him.
            // So the current step will be the reference.
            this.$scope.registerChild=(child:IWizardCreationStep):void => {
                this.currentStep.reference=child;
            };

            // Will be called from each step to notify if the step is valid
            // The wizard will set the "Next", "Finish" buttons accordingly.
            this.$scope.setValidState = (valid:boolean):void => {
                this.currentStep.valid=valid;
                let currentDirectiveStep:Sdc.Directives.IWizardStep = this.$scope.directiveSteps[this.currentStep.index];
                this.$scope.assetCreationControl.setStepValidity(currentDirectiveStep, valid);
                this.footerButtonsStateMachine();
                this.wizardButtonsIconsStateMachine();
            };

            /**
             * Will be called from each step to get current entity.
             * This will return copy of the entity (not reference), because I do not want that the step will change entity parameters.
             * If the step need to update the entity it can call setComponent function.
             * @returns {Sdc.Models.IEntity}
             */
            this.$scope.getComponent = ():Sdc.Models.Components.Component => {
                return this.component;
            };

            // Will be called from each step after save to update the resource.
            this.$scope.setComponent = (component:Sdc.Models.Components.Component):void => {
                this.component = component;
            };

        };

        protected setCurrentStepByName = (stepName:string):boolean => {
            let stepIndex:number = this.getStepIndex(stepName);
            return this.setCurrentStepByIndex(stepIndex);
        };

        // Set the current step, change the URL in ng-include.
        protected setCurrentStepByIndex = (index:number, doSave:boolean=true):boolean => {
            let result:boolean = false;
            if (this.currentStep.index!==index) { // Check that not pressing on same step, also the first time currentStepIndex=undefined
                if (doSave===true) {
                    this.callStepSave(() => {
                        // This section will be executed only if success save.

                        // Set current step in the left wizard directive = valid
                        let currentDirectiveStep:Sdc.Directives.IWizardStep = this.$scope.directiveSteps[this.currentStep.index];
                        this.$scope.assetCreationControl.setStepValidity(currentDirectiveStep, true);

                        // Move to next step
                        let step:_CreationStep = this.assetCreationSteps[index];
                        this.currentStep.index = index;
                        this.currentStep.assetCreationStep = step;
                        this.$scope.templateUrl = step.url;

                        // Update the next/back buttons and steps buttons.
                        this.footerButtonsStateMachine();
                        this.wizardButtonsIconsStateMachine();

                        // Can not perform step click without enabling the step
                        this.$scope.directiveSteps[index].enabled = true; // Need to set the step enabled, before clicking it.
                        this.$scope.assetCreationControl.stepClicked(step.name);

                        // After saving the asset name and type will be shown in the top right of the screen.
                        this.fillAssetNameAndType();

                        result=true;
                    });
                } else {
                    // For the first time
                    let step:_CreationStep = this.assetCreationSteps[index];
                    this.currentStep.index = index;
                    this.currentStep.assetCreationStep=step;
                    this.$scope.templateUrl = step.url;
                    this.$scope.directiveSteps[index].enabled = true; // Need to set the step enabled, before clicking it.
                    this.$scope.assetCreationControl.stepClicked(step.name);
                    result=true;
                }

                //this.updateFooterButtonsStates();

            } else {
                result=true;
            }
            return result;
        };

        // Save the current step
        private callStepSave = (successCallback:Function):void => {
            this.$scope.isLoading = true;
            this.currentStep.reference.save((result:boolean)=>{
                this.$scope.isLoading = false;
                if (result===true){
                    successCallback();
                } else {
                    // Set the next and finish button enabled.
                    //this.updateFooterButtonsStates(true);
                }
            });
        };

        // Save the current step
        private callStepBack = (successCallback:Function):void => {
            this.$scope.isLoading = true;
            this.currentStep.reference.back((result:boolean)=>{
                this.$scope.isLoading = false;
                if (result===true){
                    successCallback();
                } else {
                    // Set the next and finish button enabled.
                    //this.updateFooterButtonsStates(true);
                }
            });
        };

        private getStepIndex = (stepName:string):number => {
            let index:number=-1;
            let tmp = _.find(this.assetCreationSteps, function (item, indx) {
                index = indx;
                return item.name === stepName;
            });
            return index;
        };

        private btnNextClicked = ():void => {
            if (this.hasNext()===true) {
                let tmp = this.currentStep.index+1;
                this.setCurrentStepByIndex(tmp);
            }
        };

        private btnBackClicked = ():void => {
            if (this.hasBack()===true) {
                this.callStepBack(() => {
                    let tmp = this.currentStep.index-1;
                    this.setCurrentStepByIndex(tmp, false);
                });
            }
        };

        private btnCancelClicked = ():void => {
            this.$modalInstance.dismiss(this.$scope.getComponent());
        };

        private btnFinishedClicked = ():void => {
            this.callStepSave(() => {
                this.$modalInstance.close(this.$scope.getComponent());
            });
        };

        // Check if we can move next
        private hasNext = ():boolean => {
            if (this.assetCreationSteps.length-1>this.currentStep.index){
                return true;
            } else {
                return false;
            }
        };

        // Check if we can move back
        private hasBack = ():boolean => {
            if (this.currentStep.index===0){
                return false;
            } else {
                return true;
            }
        };

        private fillAssetNameAndType=():void => {
            this.$scope.assetName = this.$scope.getComponent().name;
            this.$scope.assetType = this.$scope.getComponent().getComponentSubType();

        };

        protected enableAllWizardSteps=():void => {
            this.$scope.directiveSteps.forEach((step:Sdc.Directives.IWizardStep) => {
                step.enabled=true;
            });
        };

        protected disableAllWizardSteps=():void => {
            this.$scope.directiveSteps.forEach((step:Sdc.Directives.IWizardStep) => {
                step.enabled=false;
            });
        };

        private footerButtonsStateMachine = ():void => {
            //console.log("footerButtonsStateMachine, current step validity: " + this.currentStep.valid);
            let stepIndex:number = this.currentStep.index;
            let cancelButton = this.$scope.footerButtons[0];
            let backButton = this.$scope.footerButtons[1];
            let nextButton = this.$scope.footerButtons[2];
            let finishButton = this.$scope.footerButtons[3];

            // NEXT button
            // Disable next button if it is the last step, and if not check the validity of the step.
            if (this.hasNext()){
                nextButton.disabled = !this.currentStep.valid;
            } else {
                nextButton.disabled = true;
            }

            // BACK button
            backButton.disabled = !this.hasBack();

            // FINISH button
            // If step 2 is valid show the finish button.
            if (stepIndex>=1 && this.currentStep.valid===true) {
                finishButton.disabled = false;
            }
            if (this.currentStep.valid===false){
                finishButton.disabled = true;
            }

            // EDIT
            if (this.type===WizardCreationTypes.edit && this.currentStep.valid===true){
                finishButton.disabled = false;
            }

        };



        private wizardButtonsIconsStateMachine = ():void => {

            // Enable or disable wizard directive next step, in case the current step form is valid or not.
            let stepIndex:number = this.currentStep.index;
            if (this.$scope.directiveSteps[stepIndex + 1]) {
                this.$scope.directiveSteps[stepIndex + 1].enabled = this.currentStep.valid;
            }

            // In case step 1 and 2 are valid, we can open all other steps.
            if (this.$scope.directiveSteps[0].valid===true && this.$scope.directiveSteps[1].valid===true){
                // Enable all wizard directive steps
                this.enableAllWizardSteps();
            } else if (this.currentStep.valid===false) {
                // Disable all steps
                this.disableAllWizardSteps();
            }
        };

        private noBackspaceNav:Function = ():void => {
            this.$scope.$on('$locationChangeStart', (event, newUrl, oldUrl):void =>{
                event.preventDefault();
            })
        };


    }

}
