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

import {PropertyModel, Component, ArtifactModel, Distribution, InputModel, DisplayModule, InputPropertyBase} from "../models";
import {IEmailModalModel} from "../view-models/modals/email-modal/email-modal-view-model";
import {IClientMessageModalModel} from "../view-models/modals/message-modal/message-client-modal/client-message-modal-view-model";
import {IServerMessageModalModel} from "../view-models/modals/message-modal/message-server-modal/server-message-modal-view-model";
import {IConfirmationModalModel} from "../view-models/modals/confirmation-modal/confirmation-modal-view-model";
import {ModalType} from "./constants";
import {AttributeModel} from "../models/attributes";

export interface IModalsHandler {


    openDistributionStatusModal (distribution:Distribution, status:string, component:Component):ng.IPromise<any>;
    openConfirmationModal (title:string, message:string, showComment:boolean, size?:string):ng.IPromise<any>;
    openAlertModal (title:string, message:string, size?:string):ng.IPromise<any>;
    openEmailModal(emailModel:IEmailModalModel):ng.IPromise<any>;
    openServerMessageModal(data:IServerMessageModalModel):ng.IPromise<any>;
    openClientMessageModal(data:IClientMessageModalModel):ng.IPromise<ng.ui.bootstrap.IModalServiceInstance>;
    openArtifactModal(artifact:ArtifactModel, component:Component):ng.IPromise<any>;
    openEditPropertyModal(property:PropertyModel, component:Component, filteredProperties:Array<PropertyModel>, isPropertyOwnValue:boolean, propertyOwnerType:string, propertyOwnerId:string):ng.IPromise<any>;
}

export class ModalsHandler implements IModalsHandler {

    static '$inject' = [
        '$uibModal',
        '$q'
    ];

    constructor(private $uibModal:ng.ui.bootstrap.IModalService,
                private $q:ng.IQService) {
    }




    openDistributionStatusModal = (distribution:Distribution, status:string, component:Component):ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/workspace/tabs/distribution/disribution-status-modal/disribution-status-modal-view.html',
            controller: 'Sdc.ViewModels.DistributionStatusModalViewModel',
            size: 'sdc-xl',
            backdrop: 'static',
            resolve: {
                data: ():any => {
                    return {
                        'distribution': distribution,
                        'status': status,
                        'component': component
                    };
                }
            }
        };
        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };


    openAlertModal = (title:string, message:string, size?:string):ng.IPromise<any> => {
        return this.openConfirmationModalBase(title, message, false, ModalType.ALERT, size);
    };

    openConfirmationModal = (title:string, message:string, showComment:boolean, size?:string):ng.IPromise<any> => {
        return this.openConfirmationModalBase(title, message, showComment, ModalType.STANDARD, size);
    };

    private openConfirmationModalBase = (title:string, message:string, showComment:boolean, type:ModalType, size?:string):ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/modals/confirmation-modal/confirmation-modal-view.html',
            controller: 'Sdc.ViewModels.ConfirmationModalViewModel',
            size: size ? size : 'sdc-sm',
            backdrop: 'static',
            resolve: {
                confirmationModalModel: ():IConfirmationModalModel => {
                    let model:IConfirmationModalModel = {
                        title: title,
                        message: message,
                        showComment: showComment,
                        type: type
                    };
                    return model;
                }
            }
        };

        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };

    openEmailModal = (emailModel:IEmailModalModel):ng.IPromise<any> => {

        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/modals/email-modal/email-modal-view.html',
            controller: 'Sdc.ViewModels.EmailModalViewModel',
            size: 'sdc-sm',
            backdrop: 'static',
            resolve: {
                emailModalModel: ():IEmailModalModel => {
                    return emailModel;
                }
            }
        };
        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;

    };

    openServerMessageModal = (data:IServerMessageModalModel):ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/modals/message-modal/message-server-modal/server-message-modal-view.html',
            controller: 'Sdc.ViewModels.ServerMessageModalViewModel',
            size: 'sdc-sm',
            backdrop: 'static',
            resolve: {
                serverMessageModalModel: ():IServerMessageModalModel => {
                    return data;
                }
            }
        };

        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };

    openClientMessageModal = (data:IClientMessageModalModel):ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/modals/message-modal/message-client-modal/client-message-modal-view.html',
            controller: 'Sdc.ViewModels.ClientMessageModalViewModel',
            size: 'sdc-sm',
            backdrop: 'static',
            resolve: {
                clientMessageModalModel: ():IClientMessageModalModel => {
                    return data;
                }
            }
        };
        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance);
        return deferred.promise;
    };

    openOnboadrdingModal = (okButtonText:string, currentCsarUUID?:string, currentCsarVersion?:string):ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/modals/onboarding-modal/onboarding-modal-view.html',
            controller: 'Sdc.ViewModels.OnboardingModalViewModel',
            size: 'sdc-xl',
            backdrop: 'static',
            resolve: {
                okButtonText: ():string=> {
                    return okButtonText;
                },
                currentCsarUUID: ():string=> {
                    return currentCsarUUID || null;
                },
                currentCsarVersion: ():string=> {
                    return currentCsarVersion || null;
                }
            }
        };
        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };

    openUpdateIconModal = (component: Component):ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/modals/icons-modal/icons-modal-view.html',
            controller: 'Sdc.ViewModels.IconsModalViewModel',
            size: 'sdc-auto',
            backdrop: 'static',
            resolve: {
                component: ():Component => {
                    return component;
                }
            }
        };
        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };

    openEditEnvParametersModal = (artifactResource:ArtifactModel, component?:Component):ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/forms/env-parameters-form/env-parameters-form.html',
            controller: 'Sdc.ViewModels.EnvParametersFormViewModel',
            size: 'sdc-xl',
            backdrop: 'static',
            resolve: {
                artifact: ():ArtifactModel => {
                    return artifactResource;
                },
                component: ():Component => {
                    return component;
                }
            }
        };
        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };

    openEditInputValueModal = (input:InputModel):ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/forms/input-form/input-form-view.html',
            controller: 'Sdc.ViewModels.InputFormViewModel',
            size: 'sdc-md',
            backdrop: 'static',
            resolve: {
                input: ():InputModel => {
                    return input;
                }
            }
        };
        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };

    openArtifactModal = (artifact:ArtifactModel, component:Component):ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/forms/artifact-form/artifact-form-view.html',
            controller: 'Sdc.ViewModels.ArtifactResourceFormViewModel',
            size: 'sdc-md',
            backdrop: 'static',
            keyboard: false,
            resolve: {
                artifact: ():ArtifactModel => {
                    return artifact;
                },
                component: ():Component => {
                    return component;
                }
            }
        };

        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };


    /**
     *
     * This function openes up the edit property modal
     *
     * @param property - the property to edit
     * @param component - the component who is the owner of the property
     * @param filteredProperties - the filtered properties list to scroll between in the edit modal
     * @param isPropertyValueOwner - boolean telling if the component is eligible of editing the property
     * @returns {IPromise<T>} - Promise telling if the modal has opened or not
     */
    openEditPropertyModal = (property:PropertyModel, component:Component, filteredProperties:Array<PropertyModel>, isPropertyValueOwner:boolean, propertyOwnerType:string, propertyOwnerId:string):ng.IPromise<any> => {
        let deferred = this.$q.defer();

        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/forms/property-forms/component-property-form/property-form-view.html',
            controller: 'Sdc.ViewModels.PropertyFormViewModel',
            size: 'sdc-l',
            backdrop: 'static',
            keyboard: false,
            resolve: {
                property: ():PropertyModel => {
                    return property;
                },
                component: ():Component => {
                    return <Component> component;
                },
                filteredProperties: ():Array<PropertyModel> => {
                    return filteredProperties;
                },
                isPropertyValueOwner: ():boolean => {
                    return isPropertyValueOwner;
                },
                propertyOwnerType: ():string => {
                    return propertyOwnerType;
                },
                propertyOwnerId: ():string => {
                    return propertyOwnerId;
                }
            }
        };

        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };


    openEditModulePropertyModal = (property:PropertyModel, component:Component, selectedModule:DisplayModule, filteredProperties:Array<PropertyModel>):ng.IPromise<any> => {
        let deferred = this.$q.defer();

        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/forms/property-forms/base-property-form/property-form-base-view.html',
            controller: 'Sdc.ViewModels.ModulePropertyView',
            size: 'sdc-l',
            backdrop: 'static',
            keyboard: false,
            resolve: {
                originalProperty: ():PropertyModel => {
                    return property;
                },
                component: ():Component => {
                    return <Component> component;
                },
                selectedModule: ():DisplayModule => {
                    return selectedModule;
                },
                filteredProperties: ():Array<PropertyModel> => {
                    return filteredProperties;
                }
            }
        };

        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };

    openSelectDataTypeModal = (property:PropertyModel, component:Component, filteredProperties:Array<PropertyModel>, propertiesMap:Array<InputPropertyBase>):ng.IPromise<any> => {
        let deferred = this.$q.defer();

        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/forms/property-forms/base-property-form/property-form-base-view.html',
            controller: 'Sdc.ViewModels.SelectDataTypeViewModel',
            size: 'sdc-l',
            backdrop: 'static',
            keyboard: false,
            resolve: {
                originalProperty: ():PropertyModel => {
                    return property;
                },
                component: ():Component => {
                    return <Component> component;
                },
                filteredProperties: ():Array<PropertyModel> => {
                    return filteredProperties;
                },
                propertiesMap: ():Array<InputPropertyBase>=> {
                    return propertiesMap;
                }
            }
        };

        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };

    public openEditAttributeModal = (attribute:AttributeModel, component: Component):void => {

        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/forms/attribute-form/attribute-form-view.html',
            controller: 'Sdc.ViewModels.AttributeFormViewModel',
            size: 'sdc-md',
            backdrop: 'static',
            keyboard: false,
            resolve: {
                attribute: ():AttributeModel => {
                    return attribute;
                },
                component: ():Component => {
                    return component;
                }
            }
        };
        this.$uibModal.open(modalOptions);
    };

    public openUpdateComponentInstanceNameModal = (currentComponent: Component):ng.IPromise<any> => {
        let deferred = this.$q.defer();

        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/forms/resource-instance-name-form/resource-instance-name-view.html',
            controller: 'Sdc.ViewModels.ResourceInstanceNameViewModel',
            size: 'sdc-sm',
            backdrop: 'static',
            resolve: {
                component: ():Component => {
                    return currentComponent;
                }
            }
        };

        let modalInstance:ng.ui.bootstrap.IModalServiceInstance =  this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };

    public openConformanceLevelModal = ():ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let modalOptions:ng.ui.bootstrap.IModalSettings = {
            templateUrl: '../view-models/modals/conformance-level-modal/conformance-level-modal-view.html',
            controller: 'Sdc.ViewModels.ConformanceLevelModalViewModel',
            size: 'sdc-sm',
            backdrop: 'static',
            resolve: {

            }
        };

        let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$uibModal.open(modalOptions);
        deferred.resolve(modalInstance.result);
        return deferred.promise;
    };

}
