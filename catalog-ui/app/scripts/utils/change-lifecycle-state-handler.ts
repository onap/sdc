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

    export class ChangeLifecycleStateHandler {

        static '$inject' = [
            'sdcConfig',
            'sdcMenu',
            'ComponentFactory',
            '$templateCache',
            '$filter',
            '$modal',
            'ModalsHandler'
        ];

        constructor(
                    private sdcConfig:Models.IAppConfigurtaion,
                    private sdcMenu:Models.IAppMenu,
                    private ComponentFactory: Sdc.Utils.ComponentFactory,
                    private $templateCache:ng.ITemplateCacheService,
                    private $filter:ng.IFilterService,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private ModalsHandler: Utils.ModalsHandler

        ) {

        }

        changeLifecycleState  = (component:Models.Components.Component, data:any, scope:any, onSuccessCallback?: Function, onErrorCallback?: Function):void => {

            let self = this;

            let getContacts = (component:Models.Components.Component):string =>{
                let testers = this.sdcConfig.testers;
                let result:string = testers[component.componentType][component.categories[0].name]?
                    testers[component.componentType][component.categories[0].name]:
                    testers[component.componentType]['default'];
                return result;
            };

            let onSuccess = (newComponent:Models.Components.Component):void => {
                //scope.isLoading = false;
                console.info(component.componentType.toLowerCase + ' change state ' , newComponent);
                if(onSuccessCallback) {
                    onSuccessCallback(self.ComponentFactory.createComponent(newComponent));
                }
            };

            let onError = (error):void => {
                scope.isLoading = false;
                console.info('Failed to changeLifecycleState to ', data.url);
                if(onErrorCallback) {
                    onErrorCallback(error);
                }
            };

            let comment:Models.AsdcComment = new Models.AsdcComment();
            if (data.alertModal) {
                // Show alert dialog if defined in menu.json
                //-------------------------------------------------
                let onOk = (confirmationText):void => {
                    comment.userRemarks = confirmationText;
                    scope.isLoading = true;
                    component.changeLifecycleState(data.url, comment).then(onSuccess, onError);
                };

                let onCancel = ():void => {
                    console.info('Cancel pressed');
                    scope.isLoading = false;
                };

                let modalTitle = this.sdcMenu.alertMessages[data.alertModal].title;
                let modalMessage = this.sdcMenu.alertMessages[data.alertModal].message.format([component.componentType.toLowerCase()]);
                this.ModalsHandler.openAlertModal(modalTitle, modalMessage).then(onOk, onCancel);
            } else if (data.confirmationModal) {
                // Show confirmation dialog if defined in menu.json
                //-------------------------------------------------
                let onOk = (confirmationText):void => {
                    comment.userRemarks = confirmationText;
                    scope.isLoading = true;
                    component.changeLifecycleState(data.url, comment).then(onSuccess, onError);
                };

                let onCancel = ():void => {
                    console.info('Cancel pressed');
                    scope.isLoading = false;
                };

                let modalTitle = this.sdcMenu.confirmationMessages[data.confirmationModal].title;
                let modalMessage = this.sdcMenu.confirmationMessages[data.confirmationModal].message.format([component.componentType.toLowerCase()]);
                let modalShowComment = this.sdcMenu.confirmationMessages[data.confirmationModal].showComment;
                this.ModalsHandler.openConfirmationModal(modalTitle, modalMessage, modalShowComment).then(onOk, onCancel);

            } else if (data.emailModal) {
                // Show email dialog if defined in menu.json
                //-------------------------------------------------
                let onOk = (resource):void => {
                    if (resource){
                        onSuccess(resource);
                    } else {
                        onError("Error changing life cycle state");
                    }
                };

                let onCancel = ():void => {
                    scope.isLoading = false;
                };

                let emailModel: ViewModels.IEmailModalModel =  <ViewModels.IEmailModalModel>{};
                emailModel.email =  <ViewModels.IEmailModalModel_Email>{};
                emailModel.data =  <ViewModels.IEmailModalModel_Data>{};
                emailModel.title = this.$filter('translate')("EMAIL_MODAL_TITLE");
                emailModel.email.to = getContacts(component);
                emailModel.email.subject = this.$filter('translate')("EMAIL_MODAL_SUBJECT", "{'entityName': '" + this.$filter('resourceName')(component.name) + "','entityVersion': '" + component.version + "'}");
                emailModel.email.message = '';
                emailModel.data.component = component;
                emailModel.data.stateUrl = data.url;

                this.ModalsHandler.openEmailModal(emailModel).then(onOk, onCancel);

            } else {
                // Submit to server only (no modal is shown).
                scope.isLoading = true;
                component.changeLifecycleState(data.url, comment).then(onSuccess, onError);
            }

        }
    }
}
