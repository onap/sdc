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

/**
 * Created by ngordon on 7/27/2017.
 */

import { Response } from '@angular/http';
import { SEVERITY, ServerErrors } from "../utils/constants";

export class ServerErrorResponse {

    title: string;
    message: string;
    messageId: string;
    status: number;
    severity: SEVERITY;

    constructor(response?: Response) {

        if (response) {
            let rejectionObj: any = {};
            if (response.text().length) {
                let rejection = response.json();
                rejectionObj = rejection.serviceException || rejection.requestError && (rejection.requestError.serviceException || rejection.requestError.policyException);
                rejectionObj.text = this.getFormattedMessage(rejectionObj.text || ServerErrors.MESSAGE_ERROR, rejectionObj.variables);
            }

            this.title = ServerErrors.ERROR_TITLE;
            this.message = rejectionObj.text || response.statusText || ServerErrors.DEFAULT_ERROR;
            this.messageId = rejectionObj.messageId;
            this.status = response.status;
            this.severity = SEVERITY.ERROR;
        }
    }


    private getFormattedMessage = (text: string, variables: Array<string>): string => { //OLD CODE
        // Remove the "Error: " text at the begining
        if (text.trim().indexOf("Error:") === 0) {
            text = text.replace("Error:", "").trim();
        }

        //mshitrit DE199895 bug fix
        let count: number = 0;
        variables.forEach(function (item) {
            variables[count] = item ? item.replace('<', '&lt').replace('>', '&gt') : '';
            count++;
        });

        // Format the message in case has array to <ul><li>
        text = text.replace(/\[%(\d+)\]/g, function (_, m) {
            let tmp = [];
            let list = variables[--m].split(";");
            list.forEach(function (item) {
                tmp.push("<li>" + item + "</li>");
            });
            return "<ul>" + tmp.join("") + "</ul>";
        });

        // Format the message %1 %2
        text = text.format(variables);

        return text;

    };
}