'use strict';

import {Role} from "app/utils";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";

export class InterfaceOperationViewModel {

    static '$inject' = [
        '$scope',
        '$state'
    ];

    constructor(private $scope:IWorkspaceViewModelScope, private $state: ng.ui.IStateService) {}

    isDesigner() {
    	return this.$scope.user.role === Role.DESIGNER;
    }

}
