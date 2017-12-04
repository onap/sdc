'use strict'

import {IUserProperties} from "app/models";
import {CacheService} from "app/services";

export interface IWorkflowDesignerViewModelScope extends ng.IScope {
    user:IUserProperties;
    version:string;
}

export class WorkflowDesignerViewModel {
    static '$inject' = [
        '$scope',
        'Sdc.Services.CacheService'
    ];

    constructor(private $scope:IWorkflowDesignerViewModelScope,
                private cacheService:CacheService) {

        this.initScope();
    }

    private initScope = ():void => {
        this.$scope.version = this.cacheService.get('version');

        this.$scope.user = this.cacheService.get('user');
    }
}
