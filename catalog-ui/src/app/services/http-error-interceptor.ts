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
import {IServerMessageModalModel} from "../view-models/modals/message-modal/message-server-modal/server-message-modal-view-model";
import {SEVERITY} from "../utils/constants";
import 'app/utils/prototypes.ts';

export class HttpErrorInterceptor {
    public static $inject = ['$injector', '$q'];

    public static Factory($injector:ng.auto.IInjectorService, $q:angular.IQService) {
        return new HttpErrorInterceptor($injector, $q);
    }

    constructor(private $injector:ng.auto.IInjectorService, private $q:angular.IQService) {
    }

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

        if (rejection.data && rejection.data.serviceException) {
            text = rejection.data.serviceException.text;
            variables = rejection.data.serviceException.variables;
            messageId = rejection.data.serviceException.messageId;
            isKnownException = true;
        } else if (rejection.data && rejection.data.requestError && rejection.data.requestError.serviceException) {
            text = rejection.data.requestError.serviceException.text;
            variables = rejection.data.requestError.serviceException.variables;
            messageId = rejection.data.requestError.serviceException.messageId;
            isKnownException = true;
        } else if (rejection.data && rejection.data.requestError && rejection.data.requestError.policyException) {
            text = rejection.data.requestError.policyException.text;
            variables = rejection.data.requestError.policyException.variables;
            messageId = rejection.data.requestError.policyException.messageId;
            isKnownException = true;
        } else if (rejection.data) {
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

        let modalsHandler = this.$injector.get('ModalsHandler');
        modalsHandler.openServerMessageModal(data);

        return this.$q.reject(rejection);
    }
}
