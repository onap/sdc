'use strict';

export interface IConformanceLevelModalModelScope {
    footerButtons:Array<any>;
    modalInstance:ng.ui.bootstrap.IModalServiceInstance;
}

export class ConformanceLevelModalViewModel {

    static '$inject' = ['$scope', '$uibModalInstance'];

    constructor(private $scope:IConformanceLevelModalModelScope,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance) {

        this.initScope();
    }

    private initScope = ():void => {

        this.$scope.modalInstance = this.$uibModalInstance;

        this.$scope.footerButtons = [
            {'name': 'Continue', 'css': 'grey', 'callback': this.$uibModalInstance.close},
            {'name': 'Reject', 'css': 'blue', 'callback': this.$uibModalInstance.dismiss}
        ];

    };

}
