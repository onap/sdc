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
import { ServerErrors } from '../utils/constants';
import '../utils/prototypes';

export class ServerErrorResponse {

    title: string;
    message: string;
    messageId: string;
    status: number;
    ecompRequestId: string;

    constructor(response?: any, isNg1Response?: boolean) {
        if (response) {
            if (isNg1Response) {
                // Shall handle the case where this error response is generated from the NG1 http interceptor
                this.setValuesByRejectionObject(response, response.data);
            } else {
                // Shall handle NG5 http error responses
                this.setValuesByRejectionObject(response, response.error);
            }
        }
    }

    private setValuesByRejectionObject(response: any, errorResponseBody: any) {
        let rejectionObj: any = {};

        // If it is an internal server error, we dont want to expose anything to the user, just display a default error an return
        if (response.status === 500) {
            this.title = ServerErrors.ERROR_TITLE;
            this.message = ServerErrors.DEFAULT_ERROR;
            this.status = response.status;
            return;
        }

        if (errorResponseBody) {
            if (errorResponseBody.requestError || errorResponseBody.serviceException) {
                rejectionObj = errorResponseBody.serviceException || errorResponseBody.requestError.serviceException || errorResponseBody.requestError.policyException;
                rejectionObj.text = this.getFormattedMessage(rejectionObj.text || ServerErrors.MESSAGE_ERROR, rejectionObj.variables);
            } else if (errorResponseBody.type === 'application/octet-stream') {
                rejectionObj.text = 'Error downloading file';
                rejectionObj.title = ServerErrors.DOWNLOAD_ERROR;
            } else if (errorResponseBody.message) {
                rejectionObj.text = response.error.message;
            } else {
                rejectionObj.text = response.error;
            }
        }
        this.title = rejectionObj.title || ServerErrors.ERROR_TITLE;
        this.message = rejectionObj.text || response.statusText || ServerErrors.DEFAULT_ERROR;
        this.messageId = rejectionObj.messageId;
        this.status = response.status;
        this.ecompRequestId = rejectionObj.ecompRequestId;
    }

    private getFormattedMessage = (text: string, variables: string[]): string => {
        // Remove the "Error: " text at the beginning
        if (text.trim().indexOf('Error:') === 0) {
            text = text.replace('Error:', '').trim();
        }

        // mshitrit DE199895 bug fix
        let count: number = 0;
        variables.forEach( (item) => {
            variables[count] = item ? item.replace('<', '&lt').replace('>', '&gt') : '';
            count++;
        });

        // Format the message in case has array to <ul><li>
        text = text.replace(/\[%(\d+)\]/g, (_, m) => {
            const tmp = [];
            const list = variables[--m].split(';');
            list.forEach((item) => {
                tmp.push('<li>' + item + '</li>');
            });
            return '<ul>' + tmp.join('') + '</ul>';
        });

        // Format the message %1 %2
        text = text.format(variables);

        return text;

    }
}
