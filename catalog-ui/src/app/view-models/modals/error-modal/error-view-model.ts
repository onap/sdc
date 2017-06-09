'use strict';
import {CookieService} from "app/services";

interface IErrorViewModelScope {
    mailto:string;
}

export class ErrorViewModel {

    static ADMIN_EMAIL = 'dl-asdcaccessrequest@att.com';
    static SUBJECT_PRFIEX = 'SDC Access Request for';

    static '$inject' = ['$scope', 'Sdc.Services.CookieService', '$window'];

    constructor($scope:IErrorViewModelScope, cookieService:CookieService, $window) {
        let userDetails = cookieService.getFirstName() + ' ' + cookieService.getLastName() + ' (' + cookieService.getUserId() + ')';
        $scope.mailto = ErrorViewModel.ADMIN_EMAIL + '?subject=' + $window.encodeURIComponent(ErrorViewModel.SUBJECT_PRFIEX + ' ' + userDetails);
    }
}
