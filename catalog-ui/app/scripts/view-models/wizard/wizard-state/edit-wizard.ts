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
module Sdc.ViewModels.Wizard {
    'use strict';

    export class EditWizardViewModel extends WizardCreationBaseViewModel {

        static '$inject' = [
            '$scope',
            'data',
            'ComponentFactory',
            '$modalInstance'
        ];

        constructor(public $scope:IWizardCreationScope,
                    public data:any,
                    public ComponentFactory: Sdc.Utils.ComponentFactory,
                    public $modalInstance: ng.ui.bootstrap.IModalServiceInstance) {

            super($scope, data, ComponentFactory, $modalInstance);
            this.type = WizardCreationTypes.edit;
            this.init();
            this.initCreateAssetScope();

            // Enable all wizard directive steps
            this.enableAllWizardSteps();
        }

        private init = ():void => {
            switch (this.data.component.componentType){
                case Utils.Constants.ComponentType.RESOURCE: {
                    if(this.data.component.isComplex()) {
                        this.assetCreationSteps = [
                            {
                                "name": StepNames.general,
                                "url": '/app/scripts/view-models/wizard/general-step/general-step.html'
                            },
                            {
                                "name": StepNames.icon,
                                "url": '/app/scripts/view-models/wizard/icons-step/icons-step.html'
                            },
                            {
                                "name": StepNames.deploymentArtifact,
                                "url": '/app/scripts/view-models/wizard/artifact-deployment-step/artifact-deployment-step.html'
                            },
                            {
                                "name": StepNames.informationArtifact,
                                "url": '/app/scripts/view-models/wizard/artifact-information-step/artifact-information-step.html'
                            },
                            {
                                "name": StepNames.properties,
                                "url": '/app/scripts/view-models/wizard/properties-step/properties-step.html'
                            }
                        ];
                    }else{
                        this.assetCreationSteps = [
                            {"name": StepNames.general, "url": '/app/scripts/view-models/wizard/general-step/general-step.html'},
                            {"name": StepNames.icon, "url": '/app/scripts/view-models/wizard/icons-step/icons-step.html'},
                            {"name": StepNames.informationArtifact, "url": '/app/scripts/view-models/wizard/artifact-information-step/artifact-information-step.html'},
                            {"name": StepNames.properties, "url": '/app/scripts/view-models/wizard/properties-step/properties-step.html'}
                        ];
                    }
                 break;
                }
                case Utils.Constants.ComponentType.SERVICE: {
                    this.assetCreationSteps = [
                        {"name": StepNames.general, "url": '/app/scripts/view-models/wizard/general-step/general-step.html'},
                        {"name": StepNames.icon, "url": '/app/scripts/view-models/wizard/icons-step/icons-step.html'}
                    ];
                    break;
                }
                case Utils.Constants.ComponentType.PRODUCT: {
                    this.assetCreationSteps = [
                        {"name": StepNames.general, "url": '/app/scripts/view-models/wizard/general-step/general-step.html'},
                        {"name": StepNames.hierarchy, "url": '/app/scripts/view-models/wizard/hierarchy-step/hierarchy-step.html'},
                        {"name": StepNames.icon, "url": '/app/scripts/view-models/wizard/icons-step/icons-step.html'}
                    ];
                    break;
                }
            }
        };

        private initCreateAssetScope = ():void => {
                switch (this.data.component.componentType){
                    case Utils.Constants.ComponentType.RESOURCE: {
                        if(this.data.component.isComplex()) {
                            this.$scope.directiveSteps = [
                                {
                                    "name": StepNames.general, "enabled": true, "callback": ()=> {
                                    return this.setCurrentStepByName(StepNames.general);
                                }
                                },
                                {
                                    "name": StepNames.icon, "enabled": false, "callback": ()=> {
                                    return this.setCurrentStepByName(StepNames.icon);
                                }
                                },
                                {
                                    "name": StepNames.deploymentArtifact, "enabled": false, "callback": ()=> {
                                    return this.setCurrentStepByName(StepNames.deploymentArtifact);
                                }
                                },
                                {
                                    "name": StepNames.informationArtifact, "enabled": false, "callback": ()=> {
                                    return this.setCurrentStepByName(StepNames.informationArtifact);
                                }
                                },
                                {
                                    "name": StepNames.properties, "enabled": false, "callback": ()=> {
                                    return this.setCurrentStepByName(StepNames.properties);
                                }
                                }
                            ];
                        }else{
                            this.$scope.directiveSteps = [
                                {"name": StepNames.general, "enabled": true, "callback": ()=> {return this.setCurrentStepByName(StepNames.general);}},
                                {"name": StepNames.icon, "enabled": false, "callback": ()=> {return this.setCurrentStepByName(StepNames.icon);}},
                                {"name": StepNames.informationArtifact, "enabled": false, "callback": ()=> {return this.setCurrentStepByName(StepNames.informationArtifact);}},
                                {"name": StepNames.properties, "enabled": false, "callback": ()=> {return this.setCurrentStepByName(StepNames.properties);}}
                            ];
                        }
                        this.$scope.modalTitle = "Edit " + this.data.component.resourceType;
                        break;
                     }
                    case Utils.Constants.ComponentType.SERVICE: {
                        this.$scope.directiveSteps = [
                            {"name": StepNames.general, "enabled": true, "callback": ()=> {return this.setCurrentStepByName(StepNames.general);}},
                            {"name": StepNames.icon, "enabled": false, "callback": ()=> {return this.setCurrentStepByName(StepNames.icon);}}
                        ];
                        this.$scope.modalTitle = "Edit Service";
                        break;
                    }
                    case Utils.Constants.ComponentType.PRODUCT: {
                        this.$scope.directiveSteps = [
                            {"name": StepNames.general, "enabled": true, "callback": ()=> {return this.setCurrentStepByName(StepNames.general);}},
                            {"name": StepNames.hierarchy, "enabled":false, "callback": ()=> {return this.setCurrentStepByName(StepNames.hierarchy);}},
                            {"name": StepNames.icon, "enabled": false, "callback": ()=> {return this.setCurrentStepByName(StepNames.icon);}}
                        ];
                        this.$scope.modalTitle = "Edit Product";
                        break;
                    }
                }
        }
    }
}

