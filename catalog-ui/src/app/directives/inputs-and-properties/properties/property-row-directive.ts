/**
 * Created by obarda on 1/8/2017.
 */
'use strict';

export interface IPropertyRowDirective extends ng.IScope {
    onNameClicked:Function;
    isClickable:boolean;
}

export class PropertyRowDirective implements ng.IDirective {

    constructor() {

    }

    scope = {
        property: '=',
        instanceName: '=',
        instanceId: '=',
        instancePropertiesMap: '=',
        onNameClicked: '&',
        onCheckboxClicked: '&'
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./property-row-view.html');
    };

    link = (scope:IPropertyRowDirective, element:any, $attr:any) => {
        scope.isClickable = $attr.onNameClicked ? true : false;
    };

    public static factory = ()=> {
        return new PropertyRowDirective();
    };

}

PropertyRowDirective.factory.$inject = [];
