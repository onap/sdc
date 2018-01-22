import {Designer, DesignersConfiguration} from "app/models";


interface IDesignerTabViewModelScope extends ng.IScope {
    designer: Designer;
}

export class DesignersTabViewModel {
    static '$inject' = [
        '$scope',
        '$stateParams'
    ];

    constructor(private $scope:IDesignerTabViewModelScope,
                private $stateParams:any) {

        this.initScope();
    }

    private initScope = ():void => {
        // get the designer object by using the path parameter
        let designerKey: any = _.findKey(DesignersConfiguration.designers, (designerConfig: Designer) =>{
            return designerConfig.designerStateUrl ===  this.$stateParams.path;
        });

        this.$scope.designer = DesignersConfiguration.designers[designerKey];
    }
}
