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
import i18n from 'nfvo-utils/i18n/i18n.js';
import { actionTypes as modalActionTypes } from 'nfvo-components/modal/GlobalModalConstants.js';

import SoftwareProductProcessesActionHelper from './SoftwareProductProcessesActionHelper.js';
import SoftwareProductProcessesView from './SoftwareProductProcessesView.jsx';

export const mapStateToProps = ({ softwareProduct }) => {
    let {
        softwareProductEditor: { data: currentSoftwareProduct = {} },
        softwareProductProcesses: { processesList, processesEditor }
    } = softwareProduct;
    let { data } = processesEditor;

    return {
        currentSoftwareProduct,
        processesList,
        isDisplayEditor: Boolean(data),
        isModalInEditMode: Boolean(data && data.id)
    };
};

const mapActionsToProps = (dispatch, { softwareProductId, version }) => {
    return {
        onAddProcess: () =>
            SoftwareProductProcessesActionHelper.openEditor(dispatch, {
                softwareProductId,
                version,
                isReadOnlyMode: false
            }),
        onEditProcess: (process, isReadOnlyMode) =>
            SoftwareProductProcessesActionHelper.openEditor(dispatch, {
                process,
                softwareProductId,
                version,
                isReadOnlyMode
            }),
        onDeleteProcess: process =>
            dispatch({
                type: modalActionTypes.GLOBAL_MODAL_WARNING,
                data: {
                    msg: i18n('Are you sure you want to delete "{name}"?', {
                        name: process.name
                    }),
                    confirmationButtonText: i18n('Delete'),
                    title: i18n('Delete'),
                    onConfirmed: () =>
                        SoftwareProductProcessesActionHelper.deleteProcess(
                            dispatch,
                            {
                                process,
                                softwareProductId,
                                version
                            }
                        )
                }
            })
    };
};

export default connect(mapStateToProps, mapActionsToProps, null, {
    withRef: true
})(SoftwareProductProcessesView);
