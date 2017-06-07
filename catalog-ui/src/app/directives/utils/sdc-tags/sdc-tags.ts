'use strict';

export interface ISdcTagsScope extends ng.IScope {
    tags:Array<string>;
    specialTag:string;
    newTag:string;
    formElement:ng.IFormController;
    elementName:string;
    pattern:any;
    sdcDisabled:boolean;
    maxTags:number;
    deleteTag(tag:string):void;
    addTag(tag:string):void;
    validateName():void;
}

export class SdcTagsDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        tags: '=',
        specialTag: '=',
        pattern: '=',
        sdcDisabled: '=',
        formElement: '=',
        elementName: '@',
        maxTags: '@'
    };

    public replace = false;
    public restrict = 'E';
    public transclude = false;

    template = ():string => {
        return require('./sdc-tags.html');
    };

    link = (scope:ISdcTagsScope, element:ng.INgModelController) => {

        scope.deleteTag = (tag:string):void => {
            scope.tags.splice(scope.tags.indexOf(tag), 1);
        };

        scope.addTag = ():void => {
            let valid = scope.formElement[scope.elementName].$valid;
            if (valid &&
                scope.tags.length < scope.maxTags &&
                scope.newTag &&
                scope.newTag !== '' &&
                scope.tags.indexOf(scope.newTag) === -1 &&
                scope.newTag !== scope.specialTag) {
                scope.tags.push(scope.newTag);
                scope.newTag = '';
            }
        };

        scope.validateName = ():void => {
            if (scope.tags.indexOf(scope.newTag) > -1) {
                scope.formElement[scope.elementName].$setValidity('nameExist', false);
            } else {
                scope.formElement[scope.elementName].$setValidity('nameExist', true);
            }
        }

    };

    public static factory = ()=> {
        return new SdcTagsDirective();
    };

}

SdcTagsDirective.factory.$inject = [];
