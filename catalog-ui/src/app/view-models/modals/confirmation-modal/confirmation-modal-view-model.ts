'use strict';
import {ValidationUtils, ModalType} from "app/utils";

export interface IConfirmationModalModel {
    title:string;
    message:string;
    showComment:boolean;
    type:ModalType;
}

interface IConfirmationModalViewModelScope {
    modalInstanceConfirmation:ng.ui.bootstrap.IModalServiceInstance;
    confirmationModalModel:IConfirmationModalModel;
    comment:any;
    commentValidationPattern:RegExp;
    editForm:ng.IFormController;
    okButtonColor:string;
    hideCancelButton:boolean;
    ok():any;
    cancel():void;
}

export class ConfirmationModalViewModel {

    static '$inject' = ['$scope', '$uibModalInstance', 'confirmationModalModel', 'CommentValidationPattern', 'ValidationUtils'];

    constructor(private $scope:IConfirmationModalViewModelScope,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                confirmationModalModel:IConfirmationModalModel,
                private CommentValidationPattern:RegExp,
                private ValidationUtils:ValidationUtils) {

        this.initScope(confirmationModalModel);
    }

    private initScope = (confirmationModalModel:IConfirmationModalModel):void => {
        let self = this;
        this.$scope.hideCancelButton = false;
        this.$scope.modalInstanceConfirmation = this.$uibModalInstance;
        this.$scope.confirmationModalModel = confirmationModalModel;
        this.$scope.comment = {"text": ''};
        this.$scope.commentValidationPattern = this.CommentValidationPattern;

        this.$scope.ok = ():any => {
            self.$uibModalInstance.close(this.ValidationUtils.stripAndSanitize(self.$scope.comment.text));
        };

        this.$scope.cancel = ():void => {
            console.info('Cancel pressed on: ' + this.$scope.confirmationModalModel.title);
            self.$uibModalInstance.dismiss();
        };

        // Set the OK button color according to modal type (standard, error, alert)
        let _okButtonColor = 'blue'; // Default
        switch (confirmationModalModel.type) {
            case ModalType.STANDARD:
                _okButtonColor = 'blue';
                break;
            case ModalType.ERROR:
                _okButtonColor = 'red';
                break;
            case ModalType.ALERT:
                this.$scope.hideCancelButton = true;
                _okButtonColor = 'grey';
                break;
            default:
                _okButtonColor = 'blue';
                break;
        }
        this.$scope.okButtonColor = _okButtonColor;

    }
}
