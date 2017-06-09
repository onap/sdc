'use strict';

interface IPreLoadingViewScope {
    startZoomIn:boolean;
}

export class PreLoadingViewModel {

    static '$inject' = ['$scope'];

    constructor(private $scope:IPreLoadingViewScope) {
        this.init($scope);
    }

    private init = ($scope:IPreLoadingViewScope):void => {
        this.animate($('.caption1'), 'fadeInUp', 400);
        this.animate($('.caption2'), 'fadeInUp', 800);
    };

    private animate = (element:any, animation:string, when:number):void => {
        window.setTimeout(()=> {
            element.addClass("animated " + animation);
            element[0].style = "visibility: visible;";
        }, when);
    };

}
