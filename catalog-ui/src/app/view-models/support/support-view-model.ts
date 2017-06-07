'use strict';
import {CacheService} from "app/services";

interface ISupportViewModelScope {
    version:string;
}

export class SupportViewModel {

    static '$inject' = ['$scope', 'Sdc.Services.CacheService'];

    constructor(private $scope:ISupportViewModelScope,
                private cacheService:CacheService) {
        this.$scope.version = this.cacheService.get('version');
    }
}
