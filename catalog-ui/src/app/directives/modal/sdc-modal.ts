'use strict';

export interface ISdcModalScope extends ng.IScope {
    modal:ng.ui.bootstrap.IModalServiceInstance;
    hideBackground:string;
    ok():void;
    close(result:any):void;
    cancel(reason:any):void;
}

export interface ISdcModalButton {
    name:string;
    css:string;
    disabled?:boolean;
    callback:Function;
}

export class SdcModalDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        modal: '=',
        type: '@',
        header: '@',
        headerTranslate: '@',
        headerTranslateValues: '@',
        showCloseButton: '@',
        hideBackground: '@',
        buttons: '=',
        getCloseModalResponse: '='
    };

    public replace = true;
    public restrict = 'E';
    public transclude = true;

    template = ():string => {
        return require('./sdc-modal.html');
    };

    link = (scope:ISdcModalScope, $elem:any) => {

        if (scope.hideBackground === "true") {
            $(".modal-backdrop").css('opacity', '0');
        }

        scope.close = function (result:any) {
            scope.modal.close(result);
        };

        scope.ok = function () {
            scope.modal.close();
        };

        scope.cancel = function (reason:any) {
            if (this.getCloseModalResponse)
                scope.modal.dismiss(this.getCloseModalResponse());
            else {
                scope.modal.dismiss();
            }
        };

        if (scope.modal) {
            scope.modal.result.then(function (selectedItem) {
                //$scope.selected = selectedItem;
            }, function () {
                //console.info('Modal dismissed at: ' + new Date());
            });
        }
    }

    public static factory = ()=> {
        return new SdcModalDirective();
    };

}

SdcModalDirective.factory.$inject = [];
