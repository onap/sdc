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
import sortByStringProperty from 'nfvo-utils/sortByStringProperty.js';

import { actionTypes as globalMoadlActions } from 'nfvo-components/modal/GlobalModalConstants.js';

import LicenseKeyGroupsActionHelper from './LicenseKeyGroupsActionHelper.js';
import LicenseKeyGroupsListEditorView, {
    generateConfirmationMsg
} from './LicenseKeyGroupsListEditorView.jsx';
import { SORTING_PROPERTY_NAME } from 'sdc-app/onboarding/licenseModel/LicenseModelConstants.js';

const mapStateToProps = ({
    licenseModel: { licenseKeyGroup, licenseModelEditor }
}) => {
    let { licenseKeyGroupsList } = licenseKeyGroup;
    let { vendorName } = licenseModelEditor.data;

    return {
        vendorName,
        licenseKeyGroupsList: sortByStringProperty(
            licenseKeyGroupsList,
            SORTING_PROPERTY_NAME
        )
    };
};

const mapActionsToProps = (dispatch, { licenseModelId, version }) => {
    return {
        onAddLicenseKeyGroupClick: () =>
            LicenseKeyGroupsActionHelper.openLicenseKeyGroupsEditor(dispatch, {
                isReadOnlyMode: false,
                version,
                licenseModelId
            }),
        onEditLicenseKeyGroupClick: (licenseKeyGroup, isReadOnlyMode) =>
            LicenseKeyGroupsActionHelper.openLicenseKeyGroupsEditor(dispatch, {
                licenseKeyGroup,
                licenseModelId,
                version,
                isReadOnlyMode
            }),
        onDeleteLicenseKeyGroupClick: licenseKeyGroup =>
            dispatch({
                type: globalMoadlActions.GLOBAL_MODAL_WARNING,
                data: {
                    msg: generateConfirmationMsg(licenseKeyGroup),
                    confirmationButtonText: i18n('Delete'),
                    title: i18n('Delete'),
                    onConfirmed: () =>
                        LicenseKeyGroupsActionHelper.deleteLicenseKeyGroup(
                            dispatch,
                            {
                                licenseModelId,
                                licenseKeyGroupId: licenseKeyGroup.id,
                                version
                            }
                        )
                }
            })
    };
};

export default connect(mapStateToProps, mapActionsToProps)(
    LicenseKeyGroupsListEditorView
);
