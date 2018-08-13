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
import { actionTypes } from './SoftwareProductDeploymentConstants.js';
import { actionTypes as GlobalModalActions } from 'nfvo-components/modal/GlobalModalConstants.js';
import { modalContentMapper } from 'sdc-app/common/modal/ModalContentMapper.js';
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import pickBy from 'lodash/pickBy';

function baseUrl(vspId, version) {
    const versionId = version.id;
    const restPrefix = Configuration.get('restPrefix');
    return `${restPrefix}/v1.0/vendor-software-products/${vspId}/versions/${versionId}/deployment-flavors`;
}

function fetchDeploymentFlavorsList({ softwareProductId, version }) {
    return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version)}`);
}

function fetchDeploymentFlavor({
    softwareProductId,
    deploymentFlavorId,
    version
}) {
    return RestAPIUtil.fetch(
        `${baseUrl(softwareProductId, version)}/${deploymentFlavorId}`
    );
}

function deleteDeploymentFlavor({
    softwareProductId,
    deploymentFlavorId,
    version
}) {
    return RestAPIUtil.destroy(
        `${baseUrl(softwareProductId, version)}/${deploymentFlavorId}`
    );
}

function createDeploymentFlavor({ softwareProductId, data, version }) {
    return RestAPIUtil.post(`${baseUrl(softwareProductId, version)}`, data);
}

function editDeploymentFlavor({
    softwareProductId,
    deploymentFlavorId,
    data,
    version
}) {
    return RestAPIUtil.put(
        `${baseUrl(softwareProductId, version)}/${deploymentFlavorId}`,
        data
    );
}

const SoftwareProductDeploymentActionHelper = {
    fetchDeploymentFlavorsList(dispatch, { softwareProductId, version }) {
        return fetchDeploymentFlavorsList({ softwareProductId, version }).then(
            response => {
                dispatch({
                    type: actionTypes.FETCH_SOFTWARE_PRODUCT_DEPLOYMENT_FLAVORS,
                    deploymentFlavors: response.results
                });
            }
        );
    },

    fetchDeploymentFlavor({ softwareProductId, deploymentFlavorId, version }) {
        return fetchDeploymentFlavor({
            softwareProductId,
            deploymentFlavorId,
            version
        });
    },

    deleteDeploymentFlavor(
        dispatch,
        { softwareProductId, deploymentFlavorId, version }
    ) {
        return deleteDeploymentFlavor({
            softwareProductId,
            deploymentFlavorId,
            version
        }).then(() => {
            return SoftwareProductDeploymentActionHelper.fetchDeploymentFlavorsList(
                dispatch,
                { softwareProductId, version }
            );
        });
    },

    createDeploymentFlavor(dispatch, { softwareProductId, data, version }) {
        return createDeploymentFlavor({
            softwareProductId,
            data,
            version
        }).then(() => {
            return SoftwareProductDeploymentActionHelper.fetchDeploymentFlavorsList(
                dispatch,
                { softwareProductId, version }
            );
        });
    },

    editDeploymentFlavor(
        dispatch,
        { softwareProductId, deploymentFlavorId, data, version }
    ) {
        let dataWithoutId = pickBy(data, (val, key) => key !== 'id');
        return editDeploymentFlavor({
            softwareProductId,
            deploymentFlavorId,
            data: dataWithoutId,
            version
        }).then(() => {
            return SoftwareProductDeploymentActionHelper.fetchDeploymentFlavorsList(
                dispatch,
                { softwareProductId, version }
            );
        });
    },

    closeDeploymentFlavorEditor(dispatch) {
        dispatch({
            type:
                actionTypes.deploymentFlavorEditor
                    .SOFTWARE_PRODUCT_DEPLOYMENT_CLEAR_DATA
        });
        dispatch({
            type: GlobalModalActions.GLOBAL_MODAL_CLOSE
        });
    },

    openDeploymentFlavorEditor(
        dispatch,
        {
            softwareProductId,
            modalClassName,
            deploymentFlavor = {},
            componentsList,
            isEdit = false,
            version
        }
    ) {
        let alteredDeploymentFlavor = { ...deploymentFlavor };
        if (componentsList.length) {
            alteredDeploymentFlavor = {
                ...alteredDeploymentFlavor,
                componentComputeAssociations: deploymentFlavor.componentComputeAssociations
                    ? [
                          {
                              ...deploymentFlavor
                                  .componentComputeAssociations[0],
                              componentId: componentsList[0].id
                          }
                      ]
                    : [
                          {
                              componentId: componentsList[0].id,
                              computeFlavorId: null
                          }
                      ]
            };
        }
        dispatch({
            type:
                actionTypes.deploymentFlavorEditor
                    .SOFTWARE_PRODUCT_DEPLOYMENT_FILL_DATA,
            deploymentFlavor: alteredDeploymentFlavor
        });
        dispatch({
            type: GlobalModalActions.GLOBAL_MODAL_SHOW,
            data: {
                modalComponentName: modalContentMapper.DEPLOYMENT_FLAVOR_EDITOR,
                modalComponentProps: { softwareProductId, version },
                bodyClassName: modalClassName,
                title: isEdit
                    ? 'Edit Deployment Flavor'
                    : 'Create a New Deployment Flavor'
            }
        });
    }
};

export default SoftwareProductDeploymentActionHelper;
