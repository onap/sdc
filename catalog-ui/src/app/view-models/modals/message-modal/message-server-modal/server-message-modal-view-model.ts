'use strict';
import {IMessageModalModel, IMessageModalViewModelScope, MessageModalViewModel} from "../message-base-modal-model";

export interface IServerMessageModalModel extends IMessageModalModel {
    status:string;
    messageId:string;
}

export interface IServerMessageModalViewModelScope extends IMessageModalViewModelScope {
    serverMessageModalModel:IServerMessageModalModel;
}

export class ServerMessageModalViewModel extends MessageModalViewModel {

    static '$inject' = ['$scope', '$uibModalInstance', 'serverMessageModalModel'];

    constructor(private $scope:IServerMessageModalViewModelScope,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private serverMessageModalModel:IServerMessageModalModel) {

        super($scope, $uibModalInstance, serverMessageModalModel);
    }

}
