import {Designer, IUserProperties} from "app/models";
import {CacheService} from "app/services";
import {DesignersService} from "../../../../ng2/services/designers.service";
import {IWorkspaceViewModelScope} from "../../workspace-view-model";


interface IDesignerTabViewModelScope extends IWorkspaceViewModelScope {
    designer: Designer;
    user:IUserProperties;
    queryParams: Object;
}

export class DesignersTabViewModel {
    static '$inject' = [
        '$scope',
        '$stateParams',
        'Sdc.Services.CacheService',
        'DesignersService'
    ];

    constructor(private $scope:IDesignerTabViewModelScope,
                private $stateParams:any,
                private cacheService:CacheService,
                private designersService:DesignersService) {

        this.initScope();
    }

    private initScope = ():void => {
        this.$scope.designer = this.designersService.getDesignerByStateUrl(this.$stateParams.path);

        this.$scope.user = this.cacheService.get('user');

        this.$scope.queryParams = {
            userId: this.$scope.user.userId,
            contextType: this.$scope.component.componentType,
            uuid: this.$scope.component.uuid,
            lifecycleState: this.$scope.component.lifecycleState,
            isOwner: this.$scope.component.lastUpdaterUserId === this.$scope.user.userId
        };

    }
}
