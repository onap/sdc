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
import { actionTypes as entitlementPoolsActionTypes } from './EntitlementPoolsConstants.js';
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
    return `${restPrefix}/v1.0/vendor-license-models/${licenseModelId}/versions/${versionId}/entitlement-pools`;
}

function fetchEntitlementPoolsList(licenseModelId, version) {
    return RestAPIUtil.fetch(`${baseUrl(licenseModelId, version)}`);
}

function postEntitlementPool(licenseModelId, entitlementPool, version) {
    return RestAPIUtil.post(baseUrl(licenseModelId, version), {
        name: entitlementPool.name,
        description: entitlementPool.description,
        thresholdValue: entitlementPool.thresholdValue,
        thresholdUnits: getValue(entitlementPool.thresholdUnits),
        increments: entitlementPool.increments,
        time: entitlementPool.time,
        startDate: entitlementPool.startDate,
        expiryDate: entitlementPool.expiryDate,
        manufacturerReferenceNumber: entitlementPool.manufacturerReferenceNumber
    });
}

function putEntitlementPool(
    licenseModelId,
    previousEntitlementPool,
    entitlementPool,
    version
) {
    return RestAPIUtil.put(
        `${baseUrl(licenseModelId, version)}/${entitlementPool.id}`,
        {
            name: entitlementPool.name,
            description: entitlementPool.description,
            thresholdValue: entitlementPool.thresholdValue,
            thresholdUnits: getValue(entitlementPool.thresholdUnits),
            increments: entitlementPool.increments,
            time: entitlementPool.time,
            startDate: entitlementPool.startDate,
            expiryDate: entitlementPool.expiryDate,
            manufacturerReferenceNumber:
                entitlementPool.manufacturerReferenceNumber
        }
    );
}

function deleteEntitlementPool(licenseModelId, entitlementPoolId, version) {
    return RestAPIUtil.destroy(
        `${baseUrl(licenseModelId, version)}/${entitlementPoolId}`
    );
}

function fetchLimitsList(licenseModelId, entitlementPoolId, version) {
    return RestAPIUtil.fetch(
        `${baseUrl(licenseModelId, version)}/${entitlementPoolId}/limits`
    );
}

function deleteLimit(licenseModelId, entitlementPoolId, version, limitId) {
    return RestAPIUtil.destroy(
        `${baseUrl(
            licenseModelId,
            version
        )}/${entitlementPoolId}/limits/${limitId}`
    );
}

function postLimit(licenseModelId, entitlementPoolId, version, limit) {
    return RestAPIUtil.post(
        `${baseUrl(licenseModelId, version)}/${entitlementPoolId}/limits`,
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

function putLimit(licenseModelId, entitlementPoolId, version, limit) {
    return RestAPIUtil.put(
        `${baseUrl(licenseModelId, version)}/${entitlementPoolId}/limits/${
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

const EntitlementPoolsActionHelper = {
    fetchEntitlementPoolsList(dispatch, { licenseModelId, version }) {
        return fetchEntitlementPoolsList(licenseModelId, version).then(
            response =>
                dispatch({
                    type:
                        entitlementPoolsActionTypes.ENTITLEMENT_POOLS_LIST_LOADED,
                    response
                })
        );
    },

    openEntitlementPoolsEditor(
        dispatch,
        { entitlementPool, licenseModelId, version, isReadOnlyMode } = {}
    ) {
        if (licenseModelId && version && entitlementPool) {
            this.fetchLimits(dispatch, {
                licenseModelId,
                version,
                entitlementPool
            });
        }
        dispatch({
            type: entitlementPoolsActionTypes.entitlementPoolsEditor.OPEN,
            entitlementPool
        });
        dispatch({
            type: modalActionTypes.GLOBAL_MODAL_SHOW,
            data: {
                modalComponentName: modalContentMapper.EP_EDITOR,
                modalComponentProps: {
                    version,
                    licenseModelId,
                    isReadOnlyMode,
                    size: modalSizes.LARGE
                },
                title:
                    licenseModelId && version && entitlementPool
                        ? i18n('Edit Entitlement Pool')
                        : i18n('Create New Entitlement Pool')
            }
        });
    },

    async deleteEntitlementPool(
        dispatch,
        { licenseModelId, entitlementPoolId, version }
    ) {
        await deleteEntitlementPool(licenseModelId, entitlementPoolId, version);

        await ItemsHelper.checkItemStatus(dispatch, {
            itemId: licenseModelId,
            versionId: version.id
        });

        await EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {
            licenseModelId,
            version
        });
    },

    entitlementPoolsEditorDataChanged(dispatch, { deltaData }) {
        dispatch({
            type:
                entitlementPoolsActionTypes.entitlementPoolsEditor.DATA_CHANGED,
            deltaData
        });
    },

    closeEntitlementPoolsEditor(dispatch) {
        dispatch({
            type: entitlementPoolsActionTypes.entitlementPoolsEditor.CLOSE
        });
        dispatch({
            type: modalActionTypes.GLOBAL_MODAL_CLOSE
        });
    },

    async saveEntitlementPool(
        dispatch,
        { licenseModelId, previousEntitlementPool, entitlementPool, version }
    ) {
        if (previousEntitlementPool) {
            await putEntitlementPool(
                licenseModelId,
                previousEntitlementPool,
                entitlementPool,
                version
            );
        } else {
            await postEntitlementPool(licenseModelId, entitlementPool, version);
        }
        await ItemsHelper.checkItemStatus(dispatch, {
            itemId: licenseModelId,
            versionId: version.id
        });
        await EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {
            licenseModelId,
            version
        });
    },

    hideDeleteConfirm(dispatch) {
        dispatch({
            type: entitlementPoolsActionTypes.ENTITLEMENT_POOLS_DELETE_CONFIRM,
            entitlementPoolToDelete: false
        });
    },
    openDeleteEntitlementPoolConfirm(dispatch, { entitlementPool }) {
        dispatch({
            type: entitlementPoolsActionTypes.ENTITLEMENT_POOLS_DELETE_CONFIRM,
            entitlementPoolToDelete: entitlementPool
        });
    },

    fetchLimits(dispatch, { licenseModelId, version, entitlementPool }) {
        return fetchLimitsList(
            licenseModelId,
            entitlementPool.id,
            version
        ).then(response => {
            dispatch({
                type:
                    entitlementPoolsActionTypes.entitlementPoolsEditor
                        .LIMITS_LIST_LOADED,
                response
            });
        });
    },

    submitLimit(dispatch, { licenseModelId, version, entitlementPool, limit }) {
        const propmise = limit.id
            ? putLimit(licenseModelId, entitlementPool.id, version, limit)
            : postLimit(licenseModelId, entitlementPool.id, version, limit);
        return propmise.then(() => {
            dispatch({
                type: limitEditorActions.CLOSE
            });
            this.fetchLimits(dispatch, {
                licenseModelId,
                version,
                entitlementPool
            });
            return ItemsHelper.checkItemStatus(dispatch, {
                itemId: licenseModelId,
                versionId: version.id
            });
        });
    },

    deleteLimit(dispatch, { licenseModelId, version, entitlementPool, limit }) {
        return deleteLimit(
            licenseModelId,
            entitlementPool.id,
            version,
            limit.id
        ).then(() => {
            this.fetchLimits(dispatch, {
                licenseModelId,
                version,
                entitlementPool
            });
            return ItemsHelper.checkItemStatus(dispatch, {
                itemId: licenseModelId,
                versionId: version.id
            });
        });
    }
};

export default EntitlementPoolsActionHelper;
