'use strict';
import {IAppConfigurtaion} from "app/models";

export interface IEcompFooterDirectiveScope extends ng.IScope {

}

export class EcompFooterDirective implements ng.IDirective {

    constructor(private sdcConfig:IAppConfigurtaion) {

    }

    public replace = true;
    public restrict = 'E';

    public scope = {};

    template = ():string => {
        return require('./ecomp-footer.html');
    };

    link = (scope:IEcompFooterDirectiveScope, $elem:JQuery, attr:any) => {

    };

    public static factory = (sdcConfig:IAppConfigurtaion)=> {
        return new EcompFooterDirective(sdcConfig);
    };

}

EcompFooterDirective.factory.$inject = ['sdcConfig'];
