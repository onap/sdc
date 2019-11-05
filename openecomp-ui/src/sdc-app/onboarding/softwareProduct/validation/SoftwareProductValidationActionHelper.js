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
import { actionTypes } from './SoftwareProductValidationConstants.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import { enums, screenTypes } from 'sdc-app/onboarding/OnboardingConstants.js';
import { actionTypes as modalActionTypes } from 'nfvo-components/modal/GlobalModalConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

function createCertificationFormData(tests) {
    var formData = new FormData();
    var testData = [];
    for (var test of tests) {
        if (test.files) {
            for (var file of test.files) {
                formData.append('files', file.file, file.name);
            }
        }
        delete test.files;
        testData.push(test);
    }
    formData.append('testdata', JSON.stringify(testData));

    return formData;
}
function postVSPCertificationChecks(tests, version, softwareProductId) {
    const restPrefix = Configuration.get('restPrefix');
    var id = version.id;
    var formData = createCertificationFormData(tests);
    return RestAPIUtil.post(
        `${restPrefix}/v1.0/externaltesting/executions?vspId=${softwareProductId}&vspVersionId=${id}`,
        formData
    );
}

function fetchVspChecks() {
    const restPrefix = Configuration.get('restPrefix');
    return RestAPIUtil.fetch(`${restPrefix}/v1.0/externaltesting/testcasetree`);
}
const SoftwareProductValidationActionHelper = {
    navigateToSoftwareProductValidationResults(
        dispatch,
        { softwareProductId, version, status, tests }
    ) {
        return new Promise((resolve, reject) => {
            postVSPCertificationChecks(tests, version, softwareProductId)
                .then(response => {
                    dispatch({
                        type: actionTypes.POST_VSP_TESTS,
                        vspTestResults: response
                    });
                    ScreensHelper.loadScreen(dispatch, {
                        screen:
                            enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION_RESULTS,
                        screenType: screenTypes.SOFTWARE_PRODUCT,
                        props: {
                            softwareProductId,
                            version,
                            status
                        }
                    });
                    resolve(response);
                })
                .catch(error => {
                    let errMessage =
                        error.message || error.responseJSON.message;
                    let title = error.responseJSON
                        ? error.responseJSON.status
                        : i18n('Error');
                    dispatch({
                        type: modalActionTypes.GLOBAL_MODAL_ERROR,
                        data: {
                            title: title,
                            msg: errMessage,
                            cancelButtonText: i18n('OK')
                        }
                    });
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

    setActiveTab(dispatch, { activeTab }) {
        dispatch({
            type: actionTypes.SET_ACTIVE_TAB,
            activeTab
        });
    },

    onErrorThrown(dispatch, msg) {
        dispatch({
            type: modalActionTypes.GLOBAL_MODAL_ERROR,
            data: {
                title: i18n('Error'),
                modalComponentName: i18n('Error'),
                modalComponentProps: {
                    onClose: () =>
                        dispatch({
                            type: modalActionTypes.GLOBAL_MODAL_CLOSE
                        })
                },
                msg: msg,
                cancelButtonText: i18n('OK')
            }
        });
    },

    setVspTestsMap(dispatch, map) {
        dispatch({
            type: actionTypes.SET_VSP_TESTS_MAP,
            vspTestsMap: map
        });
    },

    setComplianceChecked(dispatch, checked) {
        dispatch({
            type: actionTypes.SET_COMPLIANCE_CHECKED,
            complianceChecked: checked
        });
    },

    setCertificationChecked(dispatch, checked) {
        dispatch({
            type: actionTypes.SET_CERTIFICATION_CHECKED,
            certificationChecked: checked
        });
    },

    setTestsRequest(dispatch, request, info) {
        dispatch({
            type: actionTypes.SET_TESTS_REQUEST,
            testsRequest: request,
            generalInfo: info
        });
    },

    setGeneralInfo(dispatch, info) {
        dispatch({
            type: actionTypes.SET_GENERAL_INFO,
            generalInfo: info
        });
    },

    setIsVspValidationDisabled(dispatch, { isValidationDisabled }) {
        dispatch({
            type: actionTypes.SET_VSP_VALIDATION_DISABLED,
            isValidationDisabled: isValidationDisabled
        });
    }
};

export default SoftwareProductValidationActionHelper;
