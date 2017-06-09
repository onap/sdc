'use strict';

interface ITutorialEndViewModelScope extends ng.IScope {
}

export class TutorialEndViewModel {

    static '$inject' = [
        '$scope'
    ];

    constructor(private $scope:ITutorialEndViewModelScope) {
        this.init();
    }

    private init = ():void => {

    }

}
