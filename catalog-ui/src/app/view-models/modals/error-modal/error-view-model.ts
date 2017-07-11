/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
