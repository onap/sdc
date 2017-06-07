'use strict';

export interface IFileOpenerScope extends ng.IScope {
    importFile:any;
    testsId:any;
    extensions:string;

    onFileSelect():void;
    onFileUpload(file:any):void;
    getExtensionsWithDot():string;
}

export class FileOpenerDirective implements ng.IDirective {

    constructor(private $compile:ng.ICompileService) {
    }

    scope = {
        onFileUpload: '&',
        testsId: '@',
        extensions: '@'
    };

    restrict = 'AE';
    replace = true;
    template = ():string => {
        return require('./file-opener.html');
    };

    link = (scope:IFileOpenerScope, element:any) => {

        scope.onFileSelect = () => {
            scope.onFileUpload({file: scope.importFile});
            element.html('app/directives/file-opener/file-opener.html');
            this.$compile(element.contents())(scope);
        };

        scope.getExtensionsWithDot = ():string => {
            let ret = [];
            _.each(scope.extensions.split(','), function (item) {
                ret.push("." + item.toString());
            });
            return ret.join(",");
        };

    };

    public static factory = ($compile:ng.ICompileService)=> {
        return new FileOpenerDirective($compile);
    };

}

FileOpenerDirective.factory.$inject = ['$compile'];
