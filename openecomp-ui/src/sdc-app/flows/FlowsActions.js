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
import { actionTypes, enums } from './FlowsConstants.js';
import SequenceDiagramModelHelper from './SequenceDiagramModelHelper.js';
import { actionTypes as modalActionTypes } from 'nfvo-components/modal/GlobalModalConstants.js';
import { modalContentMapper } from 'sdc-app/common/modal/ModalContentMapper.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

function baseUrl(serviceId, artifactId = '') {
    const restCatalogPrefix = Configuration.get('restCatalogPrefix');
    return `${restCatalogPrefix}/v1/catalog/services/${serviceId}/artifacts/${artifactId}`;
}

function encodeDataToBase64(dataAsString) {
    return window.btoa(dataAsString);
}

function decodeDataToBase64(encodedData) {
    return window.atob(encodedData);
}

function encodeContent(flowData) {
    let data = {
        VERSION: {
            major: 1,
            minor: 0
        },
        description: flowData.description,
        sequenceDiagramModel: flowData.sequenceDiagramModel
    };

    return encodeDataToBase64(JSON.stringify(data));
}

function decodeContent(base64Contents) {
    let description, sequenceDiagramModel;
    let payload = JSON.parse(decodeDataToBase64(base64Contents));

    if (payload.VERSION === undefined) {
        description = payload.description || 'Please, provide description...';
        sequenceDiagramModel = payload.data || payload;
        sequenceDiagramModel =
            sequenceDiagramModel.model || sequenceDiagramModel;
    } else if (payload.VERSION.major === 1) {
        description = payload.description;
        sequenceDiagramModel = payload.sequenceDiagramModel;
    }

    return {
        description,
        sequenceDiagramModel
    };
}

function createOrUpdate(flowData) {
    let createOrUpdateRequest = {
        payloadData: encodeContent(flowData),
        artifactLabel: flowData.artifactLabel || flowData.artifactName,
        artifactName: flowData.artifactName,
        artifactType: flowData.artifactType,
        artifactGroupType: enums.INFORMATIONAL,
        description: flowData.description
    };

    return RestAPIUtil.post(
        baseUrl(flowData.serviceID, flowData.uniqueId),
        createOrUpdateRequest,
        { md5: true }
    );
}

const FlowsActions = Object.freeze({
    fetchFlowArtifacts(
        dispatch,
        { artifacts, diagramType, participants, serviceID, readonly }
    ) {
        let results = [];
        if (!Object.keys(artifacts).length) {
            dispatch({
                type: actionTypes.FLOW_LIST_LOADED,
                results,
                participants,
                serviceID,
                diagramType,
                readonly
            });
            if (!readonly) {
                FlowsActions.openEditCreateWFModal(dispatch);
            }
        } else {
            Object.keys(artifacts).forEach(artifact =>
                results.push({
                    artifactType: diagramType,
                    participants,
                    serviceID,
                    ...artifacts[artifact]
                })
            );
            dispatch({
                type: actionTypes.FLOW_LIST_LOADED,
                results,
                participants,
                serviceID,
                diagramType,
                readonly
            });
        }
    },

    fetchArtifact(dispatch, { flow }) {
        let { serviceID, uniqueId, participants } = flow;
        return RestAPIUtil.fetch(baseUrl(serviceID, uniqueId)).then(
            response => {
                let { artifactName, base64Contents } = response;
                let { sequenceDiagramModel, ...other } = decodeContent(
                    base64Contents
                );

                if (!sequenceDiagramModel) {
                    sequenceDiagramModel = SequenceDiagramModelHelper.createModel(
                        {
                            id: uniqueId,
                            name: artifactName,
                            lifelines: participants
                        }
                    );
                } else {
                    sequenceDiagramModel = SequenceDiagramModelHelper.updateModel(
                        sequenceDiagramModel,
                        {
                            name: artifactName,
                            lifelines: participants
                        }
                    );
                }

                flow = {
                    ...flow,
                    ...other,
                    uniqueId,
                    artifactName,
                    sequenceDiagramModel
                };

                dispatch({ type: actionTypes.ARTIFACT_LOADED, flow });
                FlowsActions.openFlowDiagramEditor(dispatch, { flow });
            }
        );
    },

    createOrUpdateFlow(dispatch, { flow }, isNew) {
        if (!isNew && flow.sequenceDiagramModel) {
            flow.sequenceDiagramModel = SequenceDiagramModelHelper.updateModel(
                flow.sequenceDiagramModel,
                {
                    name: flow.artifactName
                }
            );
        }
        return createOrUpdate(flow).then(response => {
            let { uniqueId, artifactLabel } = response;
            flow = { ...flow, uniqueId, artifactLabel };
            if (isNew) {
                flow.sequenceDiagramModel = SequenceDiagramModelHelper.createModel(
                    {
                        id: uniqueId,
                        name: flow.artifactName
                    }
                );
            }
            dispatch({ type: actionTypes.ADD_OR_UPDATE_FLOW, flow });
        });
    },

    deleteFlow(dispatch, { flow }) {
        return RestAPIUtil.destroy(baseUrl(flow.serviceID, flow.uniqueId)).then(
            () =>
                dispatch({
                    type: actionTypes.DELETE_FLOW,
                    flow
                })
        );
    },

    openFlowDiagramEditor(dispatch, { flow }) {
        dispatch({ type: actionTypes.OPEN_FLOW_DIAGRAM_EDITOR, flow });
    },

    closeFlowDiagramEditor(dispatch) {
        dispatch({ type: actionTypes.CLOSE_FLOW_DIAGRAM_EDITOR });
    },

    reset(dispatch) {
        dispatch({ type: actionTypes.RESET });
    },
    openEditCreateWFModal(dispatch, flow) {
        dispatch({ type: actionTypes.OPEN_FLOW_DETAILS_EDITOR, flow });
        dispatch({
            type: modalActionTypes.GLOBAL_MODAL_SHOW,
            data: {
                modalComponentName: modalContentMapper.FLOWS_EDITOR,
                modalComponentProps: {
                    isNewArtifact: Boolean(flow && flow.uniqueId)
                },
                title: flow
                    ? i18n('Edit Workflow')
                    : i18n('Create New Workflow')
            }
        });
    },
    closeEditCreateWFModal(dispatch) {
        dispatch({
            type: modalActionTypes.GLOBAL_MODAL_CLOSE
        });
        dispatch({ type: actionTypes.CLOSE_FLOW_DETAILS_EDITOR });
    }
});

export default FlowsActions;
