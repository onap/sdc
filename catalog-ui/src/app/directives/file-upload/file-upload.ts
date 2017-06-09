/**
 * Created by obarda on 1/27/2016.
 */
'use strict';
import {IAppConfigurtaion} from "app/models";

export class FileUploadModel {
    filetype:string;
    filename:string;
    filesize:number;
    base64:string;
}

export interface IFileUploadScope extends ng.IScope {
    fileModel:FileUploadModel;
    formElement:ng.IFormController;
    extensions:string;
    elementDisabled:string;
    elementName:string;
    elementRequired:string;
    myFileModel:any; // From the ng bind to <input type=file
    defaultText:string;
    onFileChangedInDirective:Function;

    getExtensionsWithDot():string;
    onFileChange():void
    onFileClick(element:any):void;
    onAfterValidate():void;
    setEmptyError(element):void;
    validateField(field:any):boolean;
    cancel():void;
}


export class FileUploadDirective implements ng.IDirective {

    constructor(private sdcConfig:IAppConfigurtaion) {
    }

    scope = {
        fileModel: '=',
        formElement: '=',
        extensions: '@',
        elementDisabled: '@',
        elementName: '@',
        elementRequired: '@',
        onFileChangedInDirective: '=?',
        defaultText: '=',
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./file-upload.html');
    };

    link = (scope:IFileUploadScope, element:any, $attr:any) => {

        // In case the browse has filename, set it valid.
        // When editing artifact the file is not sent again, so if we have filename I do not want to show error.
        if (scope.fileModel && scope.fileModel.filename && scope.fileModel.filename !== '') {
            scope.formElement[scope.elementName].$setValidity('required', true);
        }

        scope.getExtensionsWithDot = ():string => {
            let ret = [];
            if (scope.extensions) {
                _.each(scope.extensions.split(','), function (item) {
                    ret.push("." + item.toString());
                });
            }
            return ret.join(",");
        };

        scope.onFileChange = ():void => {
            if (scope.onFileChangedInDirective) {
                scope.onFileChangedInDirective();
            }
            if (scope.myFileModel) {
                scope.fileModel = scope.myFileModel;
                scope.formElement[scope.elementName].$setValidity('required', true);
            }
        };

        scope.setEmptyError = (element):void => {
            if (element.files[0].size) {
                scope.formElement[scope.elementName].$setValidity('emptyFile', true);
            } else {
                scope.formElement[scope.elementName].$setValidity('emptyFile', false);
                scope.fileModel = undefined;
            }

        };

        // Prevent case-sensitivity in the upload-file accept parameter
        // Workaround for github issue: https://github.com/adonespitogo/angular-base64-upload/issues/81
        scope.onAfterValidate = () => {
            if (!scope.formElement[scope.elementName].$valid && scope.extensions) {
                let uploadfileExtension:string = scope.fileModel.filename.split('.').pop().toLowerCase();
                if (scope.extensions.split(',').indexOf(uploadfileExtension) > -1) {
                    scope.formElement[scope.elementName].$setValidity('accept', true);
                }
            }
                // Adding fix for cases when we're changing file type for upload from file that requires certain
                // extensions to a file that don't requires any extensions
                if (!scope.formElement[scope.elementName].$valid && scope.formElement[scope.elementName].$error.accept && scope.extensions === "") {
                    scope.formElement[scope.elementName].$setValidity('accept', true);
                }
        };

        // Workaround, in case user select a file then cancel (X) then select the file again, the event onChange is not fired.
        // This is a workaround to fix this issue.
        scope.onFileClick = (element:any):void => {
            element.value = null;
        };

        scope.cancel = ():void => {
            scope.fileModel.filename = '';
            scope.formElement[scope.elementName].$pristine;
            scope.formElement[scope.elementName].$setValidity('required', false);
        }
    };

    public static factory = (sdcConfig:IAppConfigurtaion)=> {
        return new FileUploadDirective(sdcConfig);
    };

}

FileUploadDirective.factory.$inject = [ 'sdcConfig'];
