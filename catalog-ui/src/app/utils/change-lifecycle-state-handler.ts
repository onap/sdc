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

import {ComponentFactory} from "./component-factory";
import {Component, Service,IAppMenu, IAppConfigurtaion} from "../models";
import {IEmailModalModel, IEmailModalModel_Email, IEmailModalModel_Data} from "../view-models/modals/email-modal/email-modal-view-model";
import {AsdcComment} from "../models/comments";
import {ModalsHandler} from "./modals-handler";
import {ServiceServiceNg2} from "../ng2/services/component-services/service.service";

/**
 * Created by obarda on 2/11/2016.
 */

export class ChangeLifecycleStateHandler {

    static '$inject' = [
        'sdcConfig',
        'sdcMenu',
        'ComponentFactory',
        '$filter',
        'ModalsHandler',
        'ServiceServiceNg2'
    ];

    constructor(private sdcConfig:IAppConfigurtaion,
                private sdcMenu:IAppMenu,
                private ComponentFactory:ComponentFactory,
                private $filter:ng.IFilterService,
                private ModalsHandler:ModalsHandler,
                private ServiceServiceNg2:ServiceServiceNg2) {

    }

    private actualChangeLifecycleState = (component:Component, data:any, scope:any, onSuccessCallback?:Function, onErrorCallback?:Function):void => {

        let self = this;

        let getContacts = (component:Component):string => {
            let testers = this.sdcConfig.testers;
            let result:string = testers[component.componentType][component.categories[0].name] ?
                testers[component.componentType][component.categories[0].name] :
                testers[component.componentType]['default'];
            return result;
        };

        let onSuccess = (newComponent:Component):void => {
            //scope.isLoading = false;
            console.info(component.componentType.toLowerCase + ' change state ', newComponent);
            if (onSuccessCallback) {
                onSuccessCallback(self.ComponentFactory.createComponent(newComponent), data.url);
            }
        };

        let onError = (error):void => {
            scope.isLoading = false;
            console.info('Failed to changeLifecycleState to ', data.url);
            if (onErrorCallback) {
                onErrorCallback(error);
            }
        };

        let comment:AsdcComment = new AsdcComment();
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
                if (resource) {
                    onSuccess(resource);
                } else {
                    onError("Error changing life cycle state");
                }
            };

            let onCancel = ():void => {
                scope.isLoading = false;
            };

            let emailModel:IEmailModalModel = <IEmailModalModel>{};
            emailModel.email = <IEmailModalModel_Email>{};
            emailModel.data = <IEmailModalModel_Data>{};
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

    public changeLifecycleState = (component:Component, data:any, scope:any, onSuccessCallback?:Function, onErrorCallback?:Function):void => {

        if (data.conformanceLevelModal) {
            this.validateConformanceLevel(component, data, scope, onSuccessCallback, onErrorCallback);
        } else {
            this.actualChangeLifecycleState(component, data, scope, onSuccessCallback, onErrorCallback);
        }
    }

    private validateConformanceLevel = (component:Component, data:any, scope:any, onSuccessCallback?:Function, onErrorCallback?:Function):void => {
        // Validate conformance level if defined in menu.json
        //-------------------------------------------------
        this.ServiceServiceNg2.validateConformanceLevel(<Service>component).subscribe((res:boolean) => {
            if (res === true) {
                //conformance level is ok - continue
                this.actualChangeLifecycleState(component, data, scope, onSuccessCallback, onErrorCallback);

            } else {
                //show warning modal
                this.ModalsHandler.openConformanceLevelModal()
                    .then(() => {
                        //continue distribute
                        this.actualChangeLifecycleState(component, data, scope, onSuccessCallback, onErrorCallback);

                    }).catch(() => {
                        //reject distribution
                        this.actualChangeLifecycleState(component, data.conformanceLevelModal, scope, onSuccessCallback, onErrorCallback);
                });
            }
        });
    }
}
