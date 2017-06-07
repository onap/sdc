/**
 * Created by ob0695 on 5/9/2017.
 */
/**
 * Created by obarda on 1/8/2017.
 */
'use strict';
import {CapabilitiesGroup} from "app/models";

export interface ICapabilitiesListScope extends ng.IScope {
    capabilities:CapabilitiesGroup;
}


export class CapabilitiesListDirective implements ng.IDirective {

    constructor() {

    }

    scope = {
        capabilities: '=',
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./capabilities-list-view.html');
    };

    link = (scope:ICapabilitiesListScope, element:any, $attr:any) => {

    };

    public static factory = ()=> {
        return new CapabilitiesListDirective();
    };
}

CapabilitiesListDirective.factory.$inject = [];
