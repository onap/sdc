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
import LicenseAgreementActionHelper from './LicenseAgreementActionHelper.js';
import LicenseAgreementListEditorView from './LicenseAgreementListEditorView.jsx';
import { actionTypes as globalMoadlActions } from 'nfvo-components/modal/GlobalModalConstants.js';
import { SORTING_PROPERTY_NAME } from 'sdc-app/onboarding/licenseModel/LicenseModelConstants.js';

const mapStateToProps = ({
    licenseModel: { licenseAgreement, licenseModelEditor }
}) => {
    let { licenseAgreementList } = licenseAgreement;
    let { vendorName, version } = licenseModelEditor.data;

    return {
        vendorName,
        version,
        licenseAgreementList: sortByStringProperty(
            licenseAgreementList,
            SORTING_PROPERTY_NAME
        )
    };
};

const mapActionsToProps = (dispatch, { licenseModelId }) => {
    return {
        onAddLicenseAgreementClick: version =>
            LicenseAgreementActionHelper.openLicenseAgreementEditor(dispatch, {
                licenseModelId,
                version,
                isReadOnlyMode: false
            }),
        onEditLicenseAgreementClick: (
            licenseAgreement,
            version,
            isReadOnlyMode
        ) =>
            LicenseAgreementActionHelper.openLicenseAgreementEditor(dispatch, {
                licenseModelId,
                licenseAgreement,
                version,
                isReadOnlyMode
            }),
        onDeleteLicenseAgreement: (licenseAgreement, version) =>
            dispatch({
                type: globalMoadlActions.GLOBAL_MODAL_WARNING,
                data: {
                    msg: i18n('Are you sure you want to delete "{name}"?', {
                        name: licenseAgreement.name
                    }),
                    confirmationButtonText: i18n('Delete'),
                    title: i18n('Delete'),
                    onConfirmed: () =>
                        LicenseAgreementActionHelper.deleteLicenseAgreement(
                            dispatch,
                            {
                                licenseModelId,
                                licenseAgreementId: licenseAgreement.id,
                                version
                            }
                        )
                }
            })
    };
};

export default connect(mapStateToProps, mapActionsToProps)(
    LicenseAgreementListEditorView
);
