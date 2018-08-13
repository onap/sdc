/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
import { actionTypes } from './SoftwareProductProcessesConstants.js';
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {
    actionTypes as modalActionTypes,
    modalSizes
} from 'nfvo-components/modal/GlobalModalConstants.js';
import { modalContentMapper } from 'sdc-app/common/modal/ModalContentMapper.js';

function baseUrl(vspId, version) {
    let { id: versionId } = version;
    const restPrefix = Configuration.get('restPrefix');
    return `${restPrefix}/v1.0/vendor-software-products/${vspId}/versions/${versionId}/processes`;
}

function putProcess(softwareProductId, version, process) {
    return RestAPIUtil.put(
        `${baseUrl(softwareProductId, version)}/${process.id}`,
        {
            name: process.name,
            description: process.description,
            type: process.type === '' ? null : process.type
        }
    );
}

function postProcess(softwareProductId, version, process) {
    return RestAPIUtil.post(`${baseUrl(softwareProductId, version)}`, {
        name: process.name,
        description: process.description,
        type: process.type === '' ? null : process.type
    });
}

function deleteProcess(softwareProductId, version, processId) {
    return RestAPIUtil.destroy(
        `${baseUrl(softwareProductId, version)}/${processId}`
    );
}

function uploadFileToProcess(softwareProductId, version, processId, formData) {
    return RestAPIUtil.post(
        `${baseUrl(softwareProductId, version)}/${processId}/upload`,
        formData
    );
}

function fetchProcesses(softwareProductId, version) {
    return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version)}`);
}

const SoftwareProductActionHelper = {
    fetchProcessesList(dispatch, { softwareProductId, version }) {
        dispatch({
            type: actionTypes.FETCH_SOFTWARE_PRODUCT_PROCESSES,
            processesList: []
        });

        return fetchProcesses(softwareProductId, version).then(response => {
            dispatch({
                type: actionTypes.FETCH_SOFTWARE_PRODUCT_PROCESSES,
                processesList: response.results
            });
        });
    },
    openEditor(
        dispatch,
        { process, softwareProductId, version, isReadOnlyMode }
    ) {
        dispatch({
            type: actionTypes.SOFTWARE_PRODUCT_PROCESS_EDITOR_OPEN,
            process: process ? process : {}
        });
        dispatch({
            type: modalActionTypes.GLOBAL_MODAL_SHOW,
            data: {
                modalComponentName: modalContentMapper.PROCESS_EDITOR,
                modalComponentProps: {
                    version,
                    softwareProductId,
                    isReadOnlyMode,
                    size: modalSizes.LARGE
                },
                bodyClassName: 'edit-process-modal',
                title: process
                    ? i18n('Edit Process Details')
                    : i18n('Create New Process Details')
            }
        });
    },

    deleteProcess(dispatch, { process, softwareProductId, version }) {
        return deleteProcess(softwareProductId, version, process.id).then(
            () => {
                dispatch({
                    type: actionTypes.DELETE_SOFTWARE_PRODUCT_PROCESS,
                    processId: process.id
                });
            }
        );
    },

    closeEditor(dispatch) {
        dispatch({
            type: actionTypes.SOFTWARE_PRODUCT_PROCESS_EDITOR_CLOSE
        });
        dispatch({
            type: modalActionTypes.GLOBAL_MODAL_CLOSE
        });
    },

    saveProcess(
        dispatch,
        { softwareProductId, version, previousProcess, process }
    ) {
        if (previousProcess) {
            return putProcess(softwareProductId, version, process).then(() => {
                if (process.formData) {
                    uploadFileToProcess(
                        softwareProductId,
                        version,
                        process.id,
                        process.formData
                    );
                }
                dispatch({
                    type: actionTypes.EDIT_SOFTWARE_PRODUCT_PROCESS,
                    process
                });
            });
        } else {
            return postProcess(softwareProductId, version, process).then(
                response => {
                    if (process.formData) {
                        uploadFileToProcess(
                            softwareProductId,
                            version,
                            response.value,
                            process.formData
                        );
                    }
                    dispatch({
                        type: actionTypes.ADD_SOFTWARE_PRODUCT_PROCESS,
                        process: {
                            ...process,
                            id: response.value
                        }
                    });
                }
            );
        }
    },

    hideDeleteConfirm(dispatch) {
        dispatch({
            type: actionTypes.SOFTWARE_PRODUCT_PROCESS_DELETE_CONFIRM,
            processToDelete: false
        });
    },

    openDeleteProcessesConfirm(dispatch, { process }) {
        dispatch({
            type: actionTypes.SOFTWARE_PRODUCT_PROCESS_DELETE_CONFIRM,
            processToDelete: process
        });
    }
};

export default SoftwareProductActionHelper;
