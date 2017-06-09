'use strict';

export class TagData {
    tag:string;
    tooltip:string;
    id:string;
}

export interface ITagScope extends ng.IScope {
    tagData:TagData;
    onDelete:Function;
    delete:Function;
    hideTooltip:boolean;
    hideDelete:boolean;
    sdcDisable:boolean;
}

export class TagDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        tagData: '=',
        onDelete: '&',
        hideTooltip: '=',
        hideDelete: '=',
        sdcDisable: '='
    };

    replace = true;
    restrict = 'EA';
    template = ():string => {
        return require('./tag-directive.html');
    };

    link = (scope:ITagScope) => {
        scope.delete = ()=> {
            scope.onDelete({'uniqueId': scope.tagData.id});
        }
    };

    public static factory = ()=> {
        return new TagDirective();
    };

}

TagDirective.factory.$inject = [];
