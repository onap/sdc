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
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import { actionTypes as licenseKeyGroupsConstants } from './LicenseKeyGroupsConstants.js';
import { actionTypes as limitEditorActions } from 'sdc-app/onboarding/licenseModel/limits/LimitEditorConstants.js';
import { default as getValue, getStrValue } from 'nfvo-utils/getValue.js';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {
    actionTypes as modalActionTypes,
    modalSizes
} from 'nfvo-components/modal/GlobalModalConstants.js';
import { modalContentMapper } from 'sdc-app/common/modal/ModalContentMapper.js';

function baseUrl(licenseModelId, version) {
    const restPrefix = Configuration.get('restPrefix');
    const { id: versionId } = version;
    return `${restPrefix}/v1.0/vendor-license-models/${licenseModelId}/versions/${versionId}/license-key-groups`;
}

function fetchLicenseKeyGroupsList(licenseModelId, version) {
    return RestAPIUtil.fetch(`${baseUrl(licenseModelId, version)}`);
}

function deleteLicenseKeyGroup(licenseModelId, licenseKeyGroupId, version) {
    return RestAPIUtil.destroy(
        `${baseUrl(licenseModelId, version)}/${licenseKeyGroupId}`
    );
}

function postLicenseKeyGroup(licenseModelId, licenseKeyGroup, version) {
    return RestAPIUtil.post(baseUrl(licenseModelId, version), {
        name: licenseKeyGroup.name,
        description: licenseKeyGroup.description,
        type: licenseKeyGroup.type,
        increments: licenseKeyGroup.increments,
        thresholdValue: licenseKeyGroup.thresholdValue,
        thresholdUnits: getValue(licenseKeyGroup.thresholdUnits),
        startDate: licenseKeyGroup.startDate,
        expiryDate: licenseKeyGroup.expiryDate,
        manufacturerReferenceNumber: licenseKeyGroup.manufacturerReferenceNumber
    });
}

function putLicenseKeyGroup(licenseModelId, licenseKeyGroup, version) {
    return RestAPIUtil.put(
        `${baseUrl(licenseModelId, version)}/${licenseKeyGroup.id}`,
        {
            name: licenseKeyGroup.name,
            description: licenseKeyGroup.description,
            type: licenseKeyGroup.type,
            increments: licenseKeyGroup.increments,
            thresholdValue: licenseKeyGroup.thresholdValue,
            thresholdUnits: getValue(licenseKeyGroup.thresholdUnits),
            startDate: licenseKeyGroup.startDate,
            expiryDate: licenseKeyGroup.expiryDate,
            manufacturerReferenceNumber:
                licenseKeyGroup.manufacturerReferenceNumber
        }
    );
}

function fetchLimitsList(licenseModelId, licenseKeyGroupId, version) {
    return RestAPIUtil.fetch(
        `${baseUrl(licenseModelId, version)}/${licenseKeyGroupId}/limits`
    );
}

function deleteLimit(licenseModelId, licenseKeyGroupId, version, limitId) {
    return RestAPIUtil.destroy(
        `${baseUrl(
            licenseModelId,
            version
        )}/${licenseKeyGroupId}/limits/${limitId}`
    );
}

function postLimit(licenseModelId, licenseKeyGroupId, version, limit) {
    return RestAPIUtil.post(
        `${baseUrl(licenseModelId, version)}/${licenseKeyGroupId}/limits`,
        {
            name: limit.name,
            type: limit.type,
            description: limit.description,
            metric: getStrValue(limit.metric),
            value: limit.value,
            unit: getStrValue(limit.unit),
            aggregationFunction: getValue(limit.aggregationFunction),
            time: getValue(limit.time)
        }
    );
}

function putLimit(licenseModelId, licenseKeyGroupId, version, limit) {
    return RestAPIUtil.put(
        `${baseUrl(licenseModelId, version)}/${licenseKeyGroupId}/limits/${
            limit.id
        }`,
        {
            name: limit.name,
            type: limit.type,
            description: limit.description,
            metric: getStrValue(limit.metric),
            value: limit.value,
            unit: getStrValue(limit.unit),
            aggregationFunction: getValue(limit.aggregationFunction),
            time: getValue(limit.time)
        }
    );
}

export default {
    fetchLicenseKeyGroupsList(dispatch, { licenseModelId, version }) {
        return fetchLicenseKeyGroupsList(licenseModelId, version).then(
            response =>
                dispatch({
                    type:
                        licenseKeyGroupsConstants.LICENSE_KEY_GROUPS_LIST_LOADED,
                    response
                })
        );
    },

    openLicenseKeyGroupsEditor(
        dispatch,
        { licenseKeyGroup, licenseModelId, version, isReadOnlyMode } = {}
    ) {
        if (licenseModelId && version && licenseKeyGroup) {
            this.fetchLimits(dispatch, {
                licenseModelId,
                version,
                licenseKeyGroup
            });
        }
        dispatch({
            type: licenseKeyGroupsConstants.licenseKeyGroupsEditor.OPEN,
            licenseKeyGroup
        });
        dispatch({
            type: modalActionTypes.GLOBAL_MODAL_SHOW,
            data: {
                modalComponentName: modalContentMapper.LKG_EDITOR,
                modalComponentProps: {
                    version,
                    licenseModelId,
                    isReadOnlyMode,
                    size: modalSizes.LARGE
                },
                title:
                    licenseModelId && version && licenseKeyGroup
                        ? i18n('Edit License Key Group')
                        : i18n('Create New License Key Group')
            }
        });
    },

    closeLicenseKeyGroupEditor(dispatch) {
        dispatch({
            type: licenseKeyGroupsConstants.licenseKeyGroupsEditor.CLOSE
        });
        dispatch({
            type: modalActionTypes.GLOBAL_MODAL_CLOSE
        });
    },

    async saveLicenseKeyGroup(
        dispatch,
        { licenseModelId, previousLicenseKeyGroup, licenseKeyGroup, version }
    ) {
        if (previousLicenseKeyGroup) {
            await putLicenseKeyGroup(licenseModelId, licenseKeyGroup, version);
        } else {
            await postLicenseKeyGroup(licenseModelId, licenseKeyGroup, version);
        }
        await ItemsHelper.checkItemStatus(dispatch, {
            itemId: licenseModelId,
            versionId: version.id
        });
        await this.fetchLicenseKeyGroupsList(dispatch, {
            licenseModelId,
            version
        });
    },

    async deleteLicenseKeyGroup(
        dispatch,
        { licenseModelId, licenseKeyGroupId, version }
    ) {
        await deleteLicenseKeyGroup(licenseModelId, licenseKeyGroupId, version);
        await ItemsHelper.checkItemStatus(dispatch, {
            itemId: licenseModelId,
            versionId: version.id
        });
        await this.fetchLicenseKeyGroupsList(dispatch, {
            licenseModelId,
            version
        });
    },

    hideDeleteConfirm(dispatch) {
        dispatch({
            type: licenseKeyGroupsConstants.LICENSE_KEY_GROUPS_DELETE_CONFIRM,
            licenseKeyGroupToDelete: false
        });
    },

    openDeleteLicenseAgreementConfirm(dispatch, { licenseKeyGroup }) {
        dispatch({
            type: licenseKeyGroupsConstants.LICENSE_KEY_GROUPS_DELETE_CONFIRM,
            licenseKeyGroupToDelete: licenseKeyGroup
        });
    },

    fetchLimits(dispatch, { licenseModelId, version, licenseKeyGroup }) {
        return fetchLimitsList(
            licenseModelId,
            licenseKeyGroup.id,
            version
        ).then(response => {
            dispatch({
                type:
                    licenseKeyGroupsConstants.licenseKeyGroupsEditor
                        .LIMITS_LIST_LOADED,
                response
            });
        });
    },

    submitLimit(dispatch, { licenseModelId, version, licenseKeyGroup, limit }) {
        const promise = limit.id
            ? putLimit(licenseModelId, licenseKeyGroup.id, version, limit)
            : postLimit(licenseModelId, licenseKeyGroup.id, version, limit);
        return promise.then(() => {
            dispatch({
                type: limitEditorActions.CLOSE
            });
            this.fetchLimits(dispatch, {
                licenseModelId,
                version,
                licenseKeyGroup
            });
            return ItemsHelper.checkItemStatus(dispatch, {
                itemId: licenseModelId,
                versionId: version.id
            });
        });
    },

    deleteLimit(dispatch, { licenseModelId, version, licenseKeyGroup, limit }) {
        return deleteLimit(
            licenseModelId,
            licenseKeyGroup.id,
            version,
            limit.id
        ).then(() => {
            this.fetchLimits(dispatch, {
                licenseModelId,
                version,
                licenseKeyGroup
            });
            return ItemsHelper.checkItemStatus(dispatch, {
                itemId: licenseModelId,
                versionId: version.id
            });
        });
    }
};
