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
/// <reference path="../../../references"/>
module Sdc.ViewModels {
    'use strict';

    interface IErrorViewModelScope{
        mailtoJson: any;
    }

    export class ErrorViewModel {

        static '$inject' = ['$scope', 'Sdc.Services.CookieService', '$window', '$filter'];

        constructor($scope:IErrorViewModelScope, cookieService:Services.CookieService, $window, $filter:ng.IFilterService){
            let adminEmail:string = $filter('translate')('ADMIN_EMAIL');
            let subjectPrefix:string = $filter('translate')('EMAIL_SUBJECT_PREFIX');
            let userDetails = cookieService.getFirstName() + ' '+cookieService.getLastName() + ' ('+cookieService.getUserId() + ')';
            let line = adminEmail+'?subject='+$window.encodeURIComponent(subjectPrefix+' '+userDetails);
            $scope.mailtoJson = {
                "mailto": line
            };
        }

    }
}
