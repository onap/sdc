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
import { connect } from 'react-redux';
import FlowsEditorModalView from './FlowsEditorModalView.jsx';
import FlowsActions from './FlowsActions.js';
import { FLOWS_EDITOR_FORM } from './FlowsConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';

export const mapStateToProps = ({ flows }) => {
    let {
        data = { artifactName: '', description: '' },
        serviceID,
        diagramType,
        flowParticipants,
        genericFieldInfo,
        formReady
    } = flows;
    if (!data.serviceID) {
        data.serviceID = serviceID;
    }
    if (!data.artifactType) {
        data.artifactType = diagramType;
    }
    if (!data.participants) {
        data.participants = flowParticipants;
    }
    let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

    return {
        currentFlow: data,
        genericFieldInfo,
        isFormValid,
        formReady
    };
};

const mapActionsToProps = (dispatch, { isNewArtifact }) => {
    return {
        onSubmit: flow => {
            FlowsActions.closeEditCreateWFModal(dispatch);
            FlowsActions.createOrUpdateFlow(dispatch, { flow }, isNewArtifact);
        },
        onCancel: () => FlowsActions.closeEditCreateWFModal(dispatch),
        onDataChanged: deltaData =>
            ValidationHelper.dataChanged(dispatch, {
                deltaData,
                formName: FLOWS_EDITOR_FORM
            }),
        onValidateForm: () =>
            ValidationHelper.validateForm(dispatch, FLOWS_EDITOR_FORM)
    };
};

export default connect(mapStateToProps, mapActionsToProps)(
    FlowsEditorModalView
);
