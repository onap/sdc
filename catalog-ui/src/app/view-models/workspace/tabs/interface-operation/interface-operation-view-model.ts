'use strict';

import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";

export interface IInterfaceOperationViewModelScope extends IWorkspaceViewModelScope {};

export class InterfaceOperationViewModel {

    static '$inject' = [
        '$scope'
    ];

    constructor(private $scope: IInterfaceOperationViewModelScope) {}

}
