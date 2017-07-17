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

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import 'rxjs/Rx';
import {sdc2Config} from './../../../main';
import {Interceptor, InterceptedRequest, InterceptedResponse} from 'ng2-interceptors';
import {SharingService} from "../../services/sharing-service";
import {ReflectiveInjector} from '@angular/core';
import {Cookie2Service} from "./cookie.service";
import {UUID} from "angular2-uuid";
import {Dictionary} from "../../utils/dictionary/dictionary";
import {SEVERITY} from "../../utils/constants";
import {IServerMessageModalModel} from "../../view-models/modals/message-modal/message-server-modal/server-message-modal-view-model";


export class HttpInterceptor implements Interceptor {

    private cookieService:Cookie2Service;
    private sharingService:SharingService;

    constructor() {
        let injector = ReflectiveInjector.resolveAndCreate([Cookie2Service, SharingService]);
        this.cookieService = injector.get(Cookie2Service);
        this.sharingService = injector.get(SharingService);
    }

    public interceptBefore(request:InterceptedRequest):InterceptedRequest {
        /**
         * For every request to the server, that the service id, or resource id is sent in the URL, need to pass UUID in the header.
         * Check if the unique id exists in uuidMap, and if so get the UUID and add it to the header.
         */
        request.options.headers.append(this.cookieService.getUserIdSuffix(), this.cookieService.getUserId());
        request.options.withCredentials = true;
        var uuidValue = this.getUuidValue(request.url);
        if (uuidValue != '') {
            request.options.headers.set('X-ECOMP-ServiceID', uuidValue);
        }
        request.options.headers.set('X-ECOMP-RequestID', UUID.UUID());
        return request;
    }

    public interceptAfter(response:InterceptedResponse):InterceptedResponse {

        if (response.response.status !== 200 && response.response.status !== 201) {
            this.responseError(response.response.json());
            //console.log("Error from BE:",response);
        }
        return response;
    }

    private getUuidValue = (url:string):string => {
        let map:Dictionary<string, string> = this.sharingService.getUuidMap();
        if (map && url.indexOf(sdc2Config.api.root) > 0) {
            map.forEach((key:string) => {
                if (url.indexOf(key) !== -1) {
                    return this.sharingService.getUuidValue(key);
                }
            });
        }
        return '';
    };

    public formatMessageArrays = (message:string, variables:Array<string>)=> {
        return message.replace(/\[%(\d+)\]/g, function (_, m) {
            let tmp = [];
            let list = variables[--m].split(";");
            list.forEach(function (item) {
                tmp.push("<li>" + item + "</li>");
            });
            return "<ul>" + tmp.join("") + "</ul>";
        });
    };

    public responseError = (rejection:any)=> {

        let text:string;
        let variables;
        let messageId:string = "";
        let isKnownException = false;

        if (rejection && rejection.serviceException) {
            text = rejection.serviceException.text;
            variables = rejection.serviceException.variables;
            messageId = rejection.serviceException.messageId;
            isKnownException = true;
        } else if (rejection && rejection.requestError && rejection.requestError.serviceException) {
            text = rejection.requestError.serviceException.text;
            variables = rejection.requestError.serviceException.variables;
            messageId = rejection.requestError.serviceException.messageId;
            isKnownException = true;
        } else if (rejection && rejection.requestError && rejection.requestError.policyException) {
            text = rejection.requestError.policyException.text;
            variables = rejection.requestError.policyException.variables;
            messageId = rejection.requestError.policyException.messageId;
            isKnownException = true;
        } else if (rejection) {
            text = 'Wrong error format from server';
            console.error(text);
            isKnownException = false;
        }

        let data:IServerMessageModalModel;
        if (isKnownException) {
            // Remove the "Error: " text at the begining
            if (text.trim().indexOf("Error:") === 0) {
                text = text.replace("Error:", "").trim();
            }

            //mshitrit DE199895 bug fix
            let count:number = 0;
            variables.forEach(function (item) {
                variables[count] = item ? item.replace('<', '&lt').replace('>', '&gt') : '';
                count++;
            });

            // Format the message in case has array to <ul><li>
            text = this.formatMessageArrays(text, variables);

            // Format the message %1 %2
            text = text.format(variables);

            // Need to inject the MessageService manually to prevent circular dependencies (because MessageService use $templateCache that use $http).
            data = {
                title: 'Error',
                message: text,
                messageId: messageId,
                status: rejection.status,
                severity: SEVERITY.ERROR
            };
        } else {
            // Need to inject the MessageService manually to prevent circular dependencies (because MessageService use $templateCache that use $http).
            data = {
                title: 'Error',
                message: rejection.status !== -1 ? rejection.statusText : "Error getting response from server",
                messageId: messageId,
                status: rejection.status,
                severity: SEVERITY.ERROR
            };
        }

        console.error('ERROR data',data);
    }
}
