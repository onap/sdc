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
/**
 * Created by obarda on 2/11/2016.
 */
/// <reference path="../references"/>
module Sdc.Utils {

    export interface IModalsHandler {

        openViewerModal(component:Models.Components.Component):void;
        openDistributionStatusModal(distribution: Models.Distribution,status:string):void;
        openConfirmationModal (title:string, message:string, showComment:boolean, size?: string):ng.IPromise<any>;
        openAlertModal (title:string, message:string, size?: string):ng.IPromise<any>;
        openStandardModal (title:string, message:string, size?: string):ng.IPromise<any>;
        openErrorModal (title:string, message:string, size?: string):ng.IPromise<any>;
        openEmailModal(emailModel:ViewModels.IEmailModalModel) :ng.IPromise<any>;
        openServerMessageModal(data:Sdc.ViewModels.IServerMessageModalModel): ng.IPromise<any>;
        openClientMessageModal(data:Sdc.ViewModels.IClientMessageModalModel):  ng.IPromise<ng.ui.bootstrap.IModalServiceInstance>;
        openWizardArtifactModal(artifact: Models.ArtifactModel, component:Models.Components.Component): ng.IPromise<any>;
        openWizard(componentType: Utils.Constants.ComponentType,  component?:Models.Components.Component, importedFile?: any): ng.IPromise<any>;
    }

    export class ModalsHandler implements IModalsHandler{

        static '$inject' = [
            '$templateCache',
            '$modal',
            '$q'
        ];

        constructor(private $templateCache:ng.ITemplateCacheService,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $q:ng.IQService) {
        }

        openViewerModal = (component:Models.Components.Component):void => {

            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get('/app/scripts/view-models/component-viewer/component-viewer.html'),
                controller: 'Sdc.ViewModels.ComponentViewerViewModel',
                size: 'lg',
                backdrop: 'static',
                resolve: {
                    component: ():Models.Components.Component=> {
                        return component;
                    }
                }
            };
            this.$modal.open(modalOptions);
        };


        openDistributionStatusModal = (distribution: Models.Distribution,status:string): ng.IPromise<any>  => {
            let deferred = this.$q.defer();
            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get('/app/scripts/view-models/workspace/tabs/distribution/disribution-status-modal/disribution-status-modal-view.html'),
                controller: 'Sdc.ViewModels.DistributionStatusModalViewModel',
                size: 'sdc-xl',
                backdrop: 'static',
                resolve: {
                    data: ():any => {
                        return {
                            'distribution': distribution,
                            'status': status
                        };
                    }
                }
            };
            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            deferred.resolve(modalInstance.result);
            return deferred.promise;
        };



        openAlertModal = (title:string, message:string, size?: string):ng.IPromise<any> => {
            return this.openConfirmationModalBase(title, message, false, Utils.Constants.ModalType.ALERT, size);
        };

        openStandardModal = (title:string, message:string, size?: string):ng.IPromise<any> => {
            return this.openConfirmationModalBase(title, message, false, Utils.Constants.ModalType.STANDARD, size);
        };

        openErrorModal = (title:string, message:string, size?: string):ng.IPromise<any> => {
            return this.openConfirmationModalBase(title, message, false, Utils.Constants.ModalType.ERROR, size);
        };

        openConfirmationModal = (title:string, message:string, showComment:boolean, size?: string):ng.IPromise<any> => {
            return this.openConfirmationModalBase(title, message, showComment, Utils.Constants.ModalType.STANDARD, size);
        };

        private openConfirmationModalBase = (title:string, message:string, showComment:boolean, type:Utils.Constants.ModalType, size?: string):ng.IPromise<any> => {
            let deferred = this.$q.defer();
            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get('/app/scripts/view-models/modals/confirmation-modal/confirmation-modal-view.html'),
                controller: 'Sdc.ViewModels.ConfirmationModalViewModel',
                size: size? size:'sdc-sm',
                backdrop: 'static',
                resolve: {
                    confirmationModalModel: ():Sdc.ViewModels.IConfirmationModalModel => {
                        let model:Sdc.ViewModels.IConfirmationModalModel = {
                            title: title,
                            message: message,
                            showComment: showComment,
                            type: type
                        };
                        return model;
                    }
                }
            };

            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            deferred.resolve(modalInstance.result);
            return deferred.promise;
        };

        openEmailModal = (emailModel:ViewModels.IEmailModalModel):ng.IPromise<any> => {

            let deferred = this.$q.defer();
            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get('/app/scripts/view-models/modals/email-modal/email-modal-view.html'),
                controller: 'Sdc.ViewModels.EmailModalViewModel',
                size: 'sdc-sm',
                backdrop: 'static',
                resolve: {
                    emailModalModel: ():ViewModels.IEmailModalModel => {
                        return emailModel;
                    }
                }
            };
            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            deferred.resolve(modalInstance.result);
            return deferred.promise;

        };

        openServerMessageModal = (data:Sdc.ViewModels.IServerMessageModalModel):ng.IPromise<any> => {
            let deferred = this.$q.defer();
            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get('/app/scripts/view-models/modals/message-modal/message-server-modal/server-message-modal-view.html'),
                controller: 'Sdc.ViewModels.ServerMessageModalViewModel',
                size: 'sdc-sm',
                backdrop: 'static',
                resolve: {
                    serverMessageModalModel: ():Sdc.ViewModels.IServerMessageModalModel => {
                        return data;
                    }
                }
            };

            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            deferred.resolve(modalInstance.result);
            return deferred.promise;
        };

        openClientMessageModal = (data:Sdc.ViewModels.IClientMessageModalModel):ng.IPromise<any> => {
            let deferred = this.$q.defer();
            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get('/app/scripts/view-models/modals/message-modal/message-client-modal/client-message-modal-view.html'),
                controller: 'Sdc.ViewModels.ClientMessageModalViewModel',
                size: 'sdc-sm',
                backdrop: 'static',
                resolve: {
                    clientMessageModalModel: ():Sdc.ViewModels.IClientMessageModalModel => {
                        return data;
                    }
                }
            };
            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            deferred.resolve(modalInstance);
            return deferred.promise;
        };

        openOnboadrdingModal = (okButtonText:string,currentCsarUUID?:string): ng.IPromise<any>  => {
            let deferred = this.$q.defer();
            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get('/app/scripts/view-models/modals/onboarding-modal/onboarding-modal-view.html'),
                controller: 'Sdc.ViewModels.OnboardingModalViewModel',
                size: 'sdc-xl',
                backdrop: 'static',
                resolve: {
                    okButtonText:():string=>{
                        return okButtonText;
                    },
                    currentCsarUUID:():string=>{
                        return currentCsarUUID||null;
                    }
                }
            };
            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            deferred.resolve(modalInstance.result);
            return deferred.promise;
        };


        openWizard = (componentType: Utils.Constants.ComponentType,  component?:Models.Components.Component, importedFile?: any): ng.IPromise<any>  => {
            let deferred = this.$q.defer();
            let template = this.$templateCache.get('/app/scripts/view-models/wizard/wizard-creation-base.html');

            let controller:string;
            if(component){
                controller = 'Sdc.ViewModels.Wizard.EditWizardViewModel'; //Edit mode
            } else {
                if (importedFile){
                    controller = 'Sdc.ViewModels.Wizard.ImportWizardViewModel';  // Import Mode
                } else {
                    controller = 'Sdc.ViewModels.Wizard.CreateWizardViewModel'; // Create Mode
                }
            }
            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: template,
                controller: controller,
                size:   'sdc-xl',
                backdrop: 'static',
                keyboard: false,
                resolve: {
                    data: ():any => {
                        return {
                            'componentType': componentType,
                            'component': component,
                            'importFile':importedFile
                        };
                    }
                }
            };

            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            deferred.resolve(modalInstance.result);
            return deferred.promise;
        };

        openWizardArtifactModal = (artifact: Models.ArtifactModel, component:Models.Components.Component): ng.IPromise<any>  => {
            let deferred = this.$q.defer();
            let viewModelsHtmlBasePath:string = '/app/scripts/view-models/';
            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get(viewModelsHtmlBasePath + 'wizard/artifact-form-step/artifact-form-step-view.html'),
                controller: 'Sdc.ViewModels.Wizard.ArtifactResourceFormStepViewModel',
                size: 'sdc-md',
                backdrop: 'static',
                keyboard: false,
                resolve: {
                    artifact: ():Models.ArtifactModel => {
                        return artifact;
                    },
                    component: (): Models.Components.Component => {
                        return  component;
                    }
                }
            };

            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            deferred.resolve(modalInstance.result);
            return deferred.promise;
        };

    }
}
