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
import {IAppConfigurtaion, Component, AsdcComment} from "app/models";
import {ValidationUtils} from "app/utils";

export interface IEmailModalModel_Email {
    to:string;
    subject:string;
    message:string;
}

export interface IEmailModalModel_Data {
    component:Component;
    stateUrl:string;
}

export interface IEmailModalModel {
    title:string;
    email:IEmailModalModel_Email;
    data:IEmailModalModel_Data;
}

interface IEmailModalViewModelScope {
    modalInstanceEmail:ng.ui.bootstrap.IModalServiceInstance;
    emailModalModel:IEmailModalModel;
    submitInProgress:boolean;
    commentValidationPattern:RegExp;
    isLoading:boolean;
    submit():any;
    cancel():void;
    validateField(field:any):boolean;
}

export class EmailModalViewModel {

    static '$inject' = ['$scope', '$filter', 'sdcConfig', '$uibModalInstance', 'emailModalModel', 'ValidationUtils', 'CommentValidationPattern'];

    constructor(private $scope:IEmailModalViewModelScope,
                private $filter:ng.IFilterService,
                private sdcConfig:IAppConfigurtaion,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private emailModalModel:IEmailModalModel,
                private ValidationUtils:ValidationUtils,
                private CommentValidationPattern:RegExp) {

        this.initScope(emailModalModel);
    }

    private initScope = (emailModalModel:IEmailModalModel):void => {
        this.$scope.emailModalModel = emailModalModel;
        this.$scope.submitInProgress = false;
        this.$scope.commentValidationPattern = this.CommentValidationPattern;
        this.$scope.modalInstanceEmail = this.$uibModalInstance;

        this.$scope.submit = ():any => {

            let onSuccess = (component:Component) => {
                this.$scope.isLoading = false;
                this.$scope.submitInProgress = false;
                // showing the outlook modal according to the config json
                if (this.sdcConfig.showOutlook) {
                    let link:string = encodeURI(this.sdcConfig.api.baseUrl + "?folder=Ready_For_Testing");
                    let outlook:string = this.$filter('translate')("EMAIL_OUTLOOK_MESSAGE", "{'to': '" + emailModalModel.email.to + "','subject': '" + emailModalModel.email.subject + "','message': '" + emailModalModel.email.message + "', 'entityNameAndVersion': '" + emailModalModel.email.subject + "','link': '" + link + "'}");
                    window.location.href = outlook; // Open outlook with the email to send
                }
                this.$uibModalInstance.close(component); // Close the dialog
            };

            let onError = () => {
                this.$scope.isLoading = false;
                this.$scope.submitInProgress = false;
                this.$uibModalInstance.close(); // Close the dialog
            };

            // Submit to server
            // Prevent from user pressing multiple times on submit.
            if (this.$scope.submitInProgress === false) {
                this.$scope.isLoading = true;
                this.$scope.submitInProgress = true;
                let comment:AsdcComment = new AsdcComment();
                comment.userRemarks = emailModalModel.email.message;
                emailModalModel.data.component.changeLifecycleState(emailModalModel.data.stateUrl, comment).then(onSuccess, onError);
            }
        };

        this.$scope.cancel = ():void => {
            this.$uibModalInstance.dismiss();
        };

        this.$scope.validateField = (field:any):boolean => {
            if (field && field.$dirty && field.$invalid) {
                return true;
            }
            return false;
        };
    }
}
