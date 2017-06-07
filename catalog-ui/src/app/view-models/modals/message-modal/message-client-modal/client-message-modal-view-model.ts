'use strict';
import {IMessageModalModel, MessageModalViewModel, IMessageModalViewModelScope} from "../message-base-modal-model";

export interface IClientMessageModalModel extends IMessageModalModel {
}

export interface IClientMessageModalViewModelScope extends IMessageModalViewModelScope {
    clientMessageModalModel:IClientMessageModalModel;
}

export class ClientMessageModalViewModel extends MessageModalViewModel {

    static '$inject' = ['$scope', '$uibModalInstance', 'clientMessageModalModel'];

    constructor(private $scope:IClientMessageModalViewModelScope,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private clientMessageModalModel:IClientMessageModalModel) {

        super($scope, $uibModalInstance, clientMessageModalModel);
    }

}
