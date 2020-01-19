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
import { ServiceServiceNg2 } from 'app/ng2/services/component-services/service.service';
import { EventBusService } from 'app/ng2/services/event-bus.service';
import { EVENTS, ValidationUtils } from 'app/utils';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { Component, IAppConfigurtaion, IAppMenu, Service } from '../models';
import { AsdcComment } from '../models/comments';
import { CommentModalComponent } from '../ng2/components/modals/comment-modal/comment-modal.component';
import { EventListenerService } from '../services/event-listener-service';
import { ComponentFactory } from './component-factory';
import { ModalsHandler } from './modals-handler';

export class ChangeLifecycleStateHandler {

    static '$inject' = [
        'sdcConfig',
        'sdcMenu',
        'ComponentFactory',
        '$filter',
        'ModalsHandler',
        'ServiceServiceNg2',
        'EventBusService',
        'ModalServiceSdcUI',
        'ValidationUtils',
        'EventListenerService'
    ];

    constructor(private sdcConfig: IAppConfigurtaion,
                private sdcMenu: IAppMenu,
                private componentFactory: ComponentFactory,
                private $filter: ng.IFilterService,
                private modalsHandler: ModalsHandler,
                private serviceServiceNg2: ServiceServiceNg2,
                private eventBusService: EventBusService,
                private modalService: SdcUiServices.ModalService,
                private validationUtils: ValidationUtils,
                private eventListenerService: EventListenerService) {
    }

    public changeLifecycleState = (component: Component, data: any, scope: any, onSuccessCallback?: Function, onErrorCallback?: Function) => {
        if (data.conformanceLevelModal) {
            this.validateConformanceLevel(component, data, scope, onSuccessCallback, onErrorCallback);
        } else {
            this.actualChangeLifecycleState(component, data, scope, onSuccessCallback, onErrorCallback);
        }
    }

    private actualChangeLifecycleState = (component: Component, data: any, scope: any, onSuccessCallback?: Function, onErrorCallback?: Function) => {
        const self = this;

        const onSuccess = (newComponent: Component) => {
            if (onSuccessCallback) {
                onSuccessCallback(self.componentFactory.createComponent(newComponent), data.url);
                if (data.url === 'distribution/PROD/activate') {
                    this.eventListenerService.notifyObservers(EVENTS.ON_DISTRIBUTION_SUCCESS);
                }
            }
        };

        const onError = (error) => {
            scope.isLoading = false;
            if (onErrorCallback) {
                onErrorCallback(error);
            }
        };

        const comment: AsdcComment = new AsdcComment();
        if (data.alertModal) {
            // Show alert dialog if defined in menu.json
            const onOk: Function = (confirmationText) => {
                comment.userRemarks = confirmationText;
                scope.isLoading = true;
                component.changeLifecycleState(data.url, comment).then(onSuccess, onError);
            };

            const modalTitle = this.sdcMenu.alertMessages[data.alertModal].title;
            const modalMessage = this.sdcMenu.alertMessages[data.alertModal].message.format([component.componentType.toLowerCase()]);
            const modalButton = {
                testId: 'OK',
                text: this.sdcMenu.alertMessages.okButton,
                type: SdcUiCommon.ButtonType.warning,
                callback: onOk,
                closeModal: true
            } as SdcUiComponents.ModalButtonComponent;
            this.modalService.openWarningModal(modalTitle, modalMessage, 'alert-modal', [modalButton]);
        } else if (data.confirmationModal) {
            // Show confirmation dialog if defined in menu.json
            let commentModalInstance: SdcUiComponents.ModalComponent;
            const onOk = () => {
                const confirmationText: string = commentModalInstance.innerModalContent.instance.comment.text;
                commentModalInstance.closeModal();
                comment.userRemarks = this.validationUtils.stripAndSanitize(confirmationText);

                if (data.url === 'lifecycleState/CHECKIN') {
                    this.eventBusService.notify('CHECK_IN').subscribe(() => {
                        scope.isLoading = true;
                        component.changeLifecycleState(data.url, comment).then(onSuccess, onError);
                    });
                } else {
                    scope.isLoading = true;
                    component.changeLifecycleState(data.url, comment).then(onSuccess, onError);
                }
            };

            const modalTitle = this.sdcMenu.confirmationMessages[data.confirmationModal].title;
            const modalMessage = this.sdcMenu.confirmationMessages[data.confirmationModal].message.format([component.componentType.toLowerCase()]);
            const modalConfig = {
                size: 'md',
                title: modalTitle,
                type: SdcUiCommon.ModalType.custom,
                testId: 'confirm-modal',
                buttons: [
                    { id: 'OK', text: 'OK', callback: onOk, closeModal: false, testId: 'OK' },
                    { id: 'cancel', text: 'Cancel', size: 'x-small', type: 'secondary', closeModal: true, testId: 'Cancel' }
                ] as SdcUiCommon.IModalButtonComponent[]
            } as SdcUiCommon.IModalConfig;
            commentModalInstance = this.modalService.openCustomModal(modalConfig, CommentModalComponent, { message: modalMessage });
            commentModalInstance.innerModalContent.instance.onValidationChange.subscribe((isValid) => {
                commentModalInstance.getButtonById('OK').disabled = !isValid;
            });
        } else {
            // Submit to server only (no modal is shown).
            scope.isLoading = true;
            component.changeLifecycleState(data.url, comment).then(onSuccess, onError);
        }
    }

    private validateConformanceLevel = (component: Component, data: any, scope: any, onSuccessCallback?: Function, onErrorCallback?: Function) => {
        // Validate conformance level if defined in menu.json
        this.serviceServiceNg2.validateConformanceLevel(component as Service).subscribe((res: boolean) => {
            if (res === true) {
                // Conformance level is ok - continue
                this.actualChangeLifecycleState(component, data, scope, onSuccessCallback, onErrorCallback);
            } else {
                // Show warning modal
                const onContinue: Function = () => {
                    this.actualChangeLifecycleState(component, data, scope, onSuccessCallback, onErrorCallback);
                };
                const reject: Function = () => {
                    this.actualChangeLifecycleState(component, data.conformanceLevelModal, scope, onSuccessCallback, onErrorCallback);
                };
                const continueButton = {testId: 'Continue', text: 'Continue', type: SdcUiCommon.ButtonType.primary, callback: onContinue, closeModal: true} as SdcUiComponents.ModalButtonComponent;
                const rejectButton = {testId: 'Reject', text: 'Reject', type: SdcUiCommon.ButtonType.secondary, callback: reject, closeModal: true} as SdcUiComponents.ModalButtonComponent;
                this.modalService.openInfoModal(this.$filter('translate')('CONFORMANCE_LEVEL_MODAL_TITLE'),
                    this.$filter('translate')('CONFORMANCE_LEVEL_MODAL_TEXT'), 'conformance-modal', [continueButton, rejectButton]);
            }
        });
    }
}
