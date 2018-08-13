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
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import { actionTypes } from './computeComponents/computeFlavor/ComputeFlavorConstants.js';
import { modalContentMapper } from 'sdc-app/common/modal/ModalContentMapper.js';
import {
    actionTypes as globalModalActionTypes,
    modalSizes
} from 'nfvo-components/modal/GlobalModalConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import { COMPONENTS_COMPUTE_QUESTIONNAIRE } from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';

function baseUrl(softwareProductId, componentId, version) {
    const versionId = version.id;
    const restPrefix = Configuration.get('restPrefix');
    return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/components/${componentId}/compute-flavors`;
}

function baseUrlVSPLevel(softwareProductId, version) {
    const versionId = version.id;
    const restPrefix = Configuration.get('restPrefix');
    return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/compute-flavors`;
}

function fetchComputesList(softwareProductId, componentId, version) {
    return RestAPIUtil.fetch(
        `${baseUrl(softwareProductId, componentId, version)}`
    );
}

function fetchComputesListForVSP(softwareProductId, version) {
    return RestAPIUtil.fetch(`${baseUrlVSPLevel(softwareProductId, version)}`);
}

function fetchCompute(softwareProductId, componentId, computeId, version) {
    return RestAPIUtil.fetch(
        `${baseUrl(softwareProductId, componentId, version)}/${computeId}`
    );
}

function fetchComputeQuestionnaire({
    softwareProductId,
    componentId,
    computeId,
    version
}) {
    return RestAPIUtil.fetch(
        `${baseUrl(
            softwareProductId,
            componentId,
            version
        )}/${computeId}/questionnaire`
    );
}

function postCompute({ softwareProductId, componentId, compute, version }) {
    return RestAPIUtil.post(
        baseUrl(softwareProductId, componentId, version),
        compute
    );
}

function putCompute({ softwareProductId, componentId, compute, version }) {
    const computeData = {
        name: compute.name,
        description: compute.description
    };
    return RestAPIUtil.put(
        `${baseUrl(softwareProductId, componentId, version)}/${compute.id}`,
        computeData
    );
}

function putComputeQuestionnaire({
    softwareProductId,
    componentId,
    computeId,
    qdata,
    version
}) {
    return RestAPIUtil.put(
        `${baseUrl(
            softwareProductId,
            componentId,
            version
        )}/${computeId}/questionnaire`,
        qdata
    );
}

function deleteCompute({ softwareProductId, componentId, computeId, version }) {
    return RestAPIUtil.destroy(
        `${baseUrl(softwareProductId, componentId, version)}/${computeId}`
    );
}

const ComputeFlavorActionHelper = {
    openComputeEditor(dispatch, { props }) {
        dispatch({
            type: actionTypes.computeEditor.LOAD_EDITOR_DATA,
            compute: props.compute || {}
        });
        dispatch({
            type: globalModalActionTypes.GLOBAL_MODAL_SHOW,
            data: {
                modalComponentName:
                    modalContentMapper.COMPONENT_COMPUTE_FLAVOR_EDITOR,
                modalComponentProps: {
                    ...props,
                    size: props.compute ? modalSizes.XLARGE : undefined,
                    dialogClassName: 'compute-flavor-editor-modal'
                },
                title: `${
                    props.compute
                        ? i18n('Edit Compute Flavor')
                        : i18n('Create New Compute Flavor')
                }`
            }
        });
    },

    closeComputeEditor(dispatch) {
        dispatch({
            type: globalModalActionTypes.GLOBAL_MODAL_CLOSE
        });
        dispatch({
            type: actionTypes.computeEditor.CLEAR_DATA
        });
    },

    fetchComputesList(dispatch, { softwareProductId, componentId, version }) {
        return fetchComputesList(softwareProductId, componentId, version).then(
            response =>
                dispatch({
                    type: actionTypes.COMPUTE_FLAVORS_LIST_LOADED,
                    response
                })
        );
    },

    fetchComputesListForVSP(dispatch, { softwareProductId, version }) {
        return fetchComputesListForVSP(softwareProductId, version).then(
            response =>
                dispatch({
                    type: actionTypes.COMPUTE_FLAVORS_LIST_LOADED,
                    response
                })
        );
    },

    loadComputeData({ softwareProductId, componentId, computeId, version }) {
        return fetchCompute(softwareProductId, componentId, computeId, version);
    },

    loadComputeQuestionnaire(
        dispatch,
        { softwareProductId, componentId, computeId, version }
    ) {
        return fetchComputeQuestionnaire({
            softwareProductId,
            componentId,
            computeId,
            version
        }).then(response =>
            ValidationHelper.qDataLoaded(dispatch, {
                qName: COMPONENTS_COMPUTE_QUESTIONNAIRE,
                response: {
                    qdata: response.data ? JSON.parse(response.data) : {},
                    qschema: JSON.parse(response.schema)
                }
            })
        );
    },

    loadCompute(
        dispatch,
        { softwareProductId, componentId, version, computeId, isReadOnlyMode }
    ) {
        return ComputeFlavorActionHelper.loadComputeData({
            softwareProductId,
            componentId,
            computeId,
            version
        }).then(({ data }) =>
            ComputeFlavorActionHelper.loadComputeQuestionnaire(dispatch, {
                softwareProductId,
                componentId,
                computeId,
                version
            }).then(() =>
                ComputeFlavorActionHelper.openComputeEditor(dispatch, {
                    props: {
                        softwareProductId,
                        componentId,
                        version,
                        isReadOnlyMode,
                        compute: { id: computeId, ...data }
                    }
                })
            )
        );
    },

    saveComputeDataAndQuestionnaire(
        dispatch,
        { softwareProductId, componentId, data: compute, qdata, version }
    ) {
        ComputeFlavorActionHelper.closeComputeEditor(dispatch);
        if (compute.id) {
            return Promise.all([
                putComputeQuestionnaire({
                    softwareProductId,
                    componentId,
                    computeId: compute.id,
                    qdata,
                    version
                }),
                putCompute({
                    softwareProductId,
                    componentId,
                    compute,
                    version
                }).then(() => {
                    dispatch({
                        type: actionTypes.COMPUTE_LIST_EDIT,
                        compute
                    });
                })
            ]);
        } else {
            return postCompute({
                softwareProductId,
                componentId,
                compute,
                version
            }).then(response =>
                dispatch({
                    type: actionTypes.ADD_COMPUTE,
                    compute: {
                        ...compute,
                        id: response.id,
                        componentId
                    }
                })
            );
        }
    },

    deleteCompute(
        dispatch,
        { softwareProductId, componentId, computeId, version }
    ) {
        return deleteCompute({
            softwareProductId,
            componentId,
            computeId,
            version
        }).then(() =>
            dispatch({
                type: actionTypes.DELETE_COMPUTE,
                computeId
            })
        );
    }
};

export default ComputeFlavorActionHelper;
