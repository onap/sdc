/**
 * Created by rcohen on 9/25/2016.
 */
'use strict';

export interface IInfoTooltipScope extends ng.IScope {
    infoMessageTranslate:string;
    direction:string;
}


export class InfoTooltipDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        infoMessageTranslate: '@',
        direction: '@'//get 'right' or 'left', the default is 'right'
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./info-tooltip.html');
    };

    link = (scope:IInfoTooltipScope, element:any, $attr:any) => {
        scope.direction = scope.direction || 'right';
    };

    public static factory = ()=> {
        return new InfoTooltipDirective();
    };
}

InfoTooltipDirective.factory.$inject = [];
