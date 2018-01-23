import {Designer} from "app/models";
import {DesignersService} from "../../../../ng2/services/designers.service";


interface IDesignerTabViewModelScope extends ng.IScope {
    designer: Designer;
}

export class DesignersTabViewModel {
    static '$inject' = [
        '$scope',
        '$stateParams',
        'DesignersService'
    ];

    constructor(private $scope:IDesignerTabViewModelScope,
                private $stateParams:any,
                private designersService:DesignersService) {

        this.initScope();
    }

    private initScope = ():void => {
        this.$scope.designer = this.designersService.getDesignerByStateUrl(this.$stateParams.path);
    }
}
