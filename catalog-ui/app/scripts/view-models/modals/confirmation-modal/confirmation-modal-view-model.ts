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
module Sdc.ViewModels {
    'use strict';

    export interface IConfirmationModalModel {
        title: string;
        message: string;
        showComment: boolean;
        type: Utils.Constants.ModalType;
    }

    interface IConfirmationModalViewModelScope {
        modalInstanceConfirmation:ng.ui.bootstrap.IModalServiceInstance;
        confirmationModalModel: IConfirmationModalModel;
        comment: any;
        commentValidationPattern:RegExp;
        editForm:ng.IFormController;
        okButtonColor: string;
        hideCancelButton: boolean;
        ok(): any;
        cancel(): void;
    }

    export class ConfirmationModalViewModel {

        static '$inject' = ['$scope', '$modalInstance', 'confirmationModalModel', 'CommentValidationPattern', 'ValidationUtils', '$templateCache', '$modal'];

        constructor(private $scope:IConfirmationModalViewModelScope,
                    private $modalInstance:ng.ui.bootstrap.IModalServiceInstance,
                    confirmationModalModel:IConfirmationModalModel,
                    private CommentValidationPattern: RegExp,
                    private ValidationUtils: Sdc.Utils.ValidationUtils,
                    private $templateCache:ng.ITemplateCacheService,
                    private $modal:ng.ui.bootstrap.IModalService) {

            this.initScope(confirmationModalModel);
        }

        private initScope = (confirmationModalModel:IConfirmationModalModel):void => {
            let self = this;
            this.$scope.hideCancelButton = false;
            this.$scope.modalInstanceConfirmation = this.$modalInstance;
            this.$scope.confirmationModalModel = confirmationModalModel;
            this.$scope.comment = {"text": ''};
            this.$scope.commentValidationPattern = this.CommentValidationPattern;

            this.$scope.ok = ():any => {
                self.$modalInstance.close(this.ValidationUtils.stripAndSanitize(self.$scope.comment.text));
            };

            this.$scope.cancel = ():void => {
                console.info('Cancel pressed on: ' + this.$scope.confirmationModalModel.title);
                self.$modalInstance.dismiss();
            };

            // Set the OK button color according to modal type (standard, error, alert)
            let _okButtonColor = 'blue'; // Default
            switch (confirmationModalModel.type) {
                case Sdc.Utils.Constants.ModalType.STANDARD:
                    _okButtonColor='blue';
                    break;
                case Sdc.Utils.Constants.ModalType.ERROR:
                    _okButtonColor='red';
                    break;
                case Sdc.Utils.Constants.ModalType.ALERT:
                    this.$scope.hideCancelButton = true;
                    _okButtonColor='grey';
                    break;
                default:
                    _okButtonColor='blue';
                    break;
            }
            this.$scope.okButtonColor = _okButtonColor;

        }
    }
}
