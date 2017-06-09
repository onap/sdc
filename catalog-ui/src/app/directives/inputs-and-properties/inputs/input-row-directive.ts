/**
 * Created by obarda on 1/8/2017.
 */
'use strict';

export interface IInputRowDirective extends ng.IScope {
    showDeleteIcon:boolean;
}


export class InputRowDirective implements ng.IDirective {

    constructor() {

    }

    scope = {
        instanceInputsMap: '=',
        input: '=',
        instanceName: '=',
        instanceId: '=',
        isViewOnly: '=',
        deleteInput: '&',
        onNameClicked: '&',
        onCheckboxClicked: '&'
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./input-row-view.html');
    };

    link = (scope:IInputRowDirective, element:any, $attr:any) => {
        scope.showDeleteIcon = $attr.deleteInput ? true : false;
    };

    public static factory = ()=> {
        return new InputRowDirective();
    };
}

InputRowDirective.factory.$inject = [];
