/**
 * Copyright (c) 2019 Vodafone Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import { actionTypes } from './SoftwareProductValidationResultsViewConstants.js';

function encodeResultQueryData(requestId, endPoints) {
    const query = [];
    query.push('requestId=' + requestId);
    endPoints.forEach(endPoint => {
        query.push('endPoint=' + encodeURIComponent(endPoint));
    });

    return query.join('&');
}
function fetchVspValidationResults(requestId, endPoints) {
    const restPrefix = Configuration.get('restPrefix');
    const requestQuery = encodeResultQueryData(requestId, endPoints);
    return RestAPIUtil.fetch(
        `${restPrefix}/v1.0/externaltesting/executions?${requestQuery}`
    );
}
function fetchVspChecks() {
    const restPrefix = Configuration.get('restPrefix');
    return RestAPIUtil.fetch(`${restPrefix}/v1.0/externaltesting/testcasetree`);
}
const SoftwareProductValidationResultsViewActionHelper = {
    refreshValidationResults(dispatch, { requestId, endPoints }) {
        return new Promise((resolve, reject) => {
            fetchVspValidationResults(requestId, endPoints)
                .then(response => {
                    dispatch({
                        type: actionTypes.FETCH_VSP_RESULT,
                        vspTestResults: response
                    });
                    resolve(response);
                })
                .catch(error => {
                    reject(error);
                });
        });
    },
    fetchVspChecks(dispatch) {
        return new Promise((resolve, reject) => {
            fetchVspChecks()
                .then(response => {
                    dispatch({
                        type: actionTypes.FETCH_VSP_CHECKS,
                        vspChecks: response
                    });
                    resolve(response);
                })
                .catch(error => {
                    reject(error);
                });
        });
    },
    updateDisplayTestResultData(dispatch, { testResultToDisplay }) {
        dispatch({
            type: actionTypes.UPDATE_DISPLAY_TEST_RESULT_DATA,
            testResultToDisplay: testResultToDisplay
        });
    }
};

export default SoftwareProductValidationResultsViewActionHelper;
