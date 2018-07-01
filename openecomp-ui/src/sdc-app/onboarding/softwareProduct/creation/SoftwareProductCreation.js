/*!
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import { connect } from 'react-redux';
import SoftwareProductCreationActionHelper from './SoftwareProductCreationActionHelper.js';
import SoftwareProductCreationView from './SoftwareProductCreationView.jsx';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import SoftwareProductActionHelper from '../SoftwareProductActionHelper.js';
import VersionsPageActionHelper from 'sdc-app/onboarding/versionsPage/VersionsPageActionHelper.js';
import { itemTypes as versionItemTypes } from 'sdc-app/onboarding/versionsPage/VersionsPageConstants.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import { enums, screenTypes } from 'sdc-app/onboarding/OnboardingConstants.js';
import PermissionsActionHelper from 'sdc-app/onboarding/permissions/PermissionsActionHelper.js';
import UniqueTypesHelper from 'sdc-app/common/helpers/UniqueTypesHelper.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import { itemType } from 'sdc-app/common/helpers/ItemsHelperConstants.js';

export const mapStateToProps = ({
    finalizedLicenseModelList,
    users: { usersList },
    archivedSoftwareProductList,
    softwareProductList,
    finalizedSoftwareProductList,
    softwareProduct: { softwareProductCreation, softwareProductCategories }
}) => {
    let { genericFieldInfo, vendorList = [] } = softwareProductCreation;
    let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

    let VSPNames = {};
    const allVspList = [
        ...softwareProductList,
        ...finalizedSoftwareProductList,
        ...archivedSoftwareProductList
    ];
    allVspList.map(item => {
        VSPNames[item.name.toLowerCase()] = item.id;
    });

    return {
        data: softwareProductCreation.data,
        selectedVendorId: softwareProductCreation.selectedVendorId,
        disableVendor: softwareProductCreation.disableVendor,
        softwareProductCategories,
        finalizedLicenseModelList,
        vendorList,
        isFormValid,
        formReady: softwareProductCreation.formReady,
        genericFieldInfo,
        VSPNames,
        usersList
    };
};

export const mapActionsToProps = dispatch => {
    return {
        onDataChanged: (deltaData, formName, customValidations) =>
            ValidationHelper.dataChanged(dispatch, {
                deltaData,
                formName,
                customValidations
            }),
        onCancel: () => SoftwareProductCreationActionHelper.resetData(dispatch),
        onSubmit: (softwareProduct, usersList) => {
            SoftwareProductCreationActionHelper.resetData(dispatch);
            SoftwareProductCreationActionHelper.createSoftwareProduct(
                dispatch,
                { softwareProduct }
            ).then(response => {
                let { itemId, version } = response;
                SoftwareProductActionHelper.fetchSoftwareProductList(
                    dispatch
                ).then(() =>
                    PermissionsActionHelper.fetchItemUsers(dispatch, {
                        itemId,
                        allUsers: usersList
                    }).then(() =>
                        VersionsPageActionHelper.fetchVersions(dispatch, {
                            itemType: versionItemTypes.SOFTWARE_PRODUCT,
                            itemId
                        }).then(() =>
                            ScreensHelper.loadScreen(dispatch, {
                                screen:
                                    enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE,
                                screenType: screenTypes.SOFTWARE_PRODUCT,
                                props: { softwareProductId: itemId, version }
                            })
                        )
                    )
                );
            });
        },
        onValidateForm: formName =>
            ValidationHelper.validateForm(dispatch, formName),
        isNameUnique: (value, name, formName) =>
            UniqueTypesHelper.isNameUnique(dispatch, {
                value,
                name,
                formName,
                errorText: `${i18n(
                    'Software product by the name'
                )} ${value} ${i18n('already exists')}. ${i18n(
                    'Software product name must be unique'
                )}`,
                itemType: itemType.VSP
            })
    };
};

export default connect(mapStateToProps, mapActionsToProps, null, {
    withRef: true
})(SoftwareProductCreationView);
