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
import { actionTypes } from './HeatSetupConstants.js';
import isEqual from 'lodash/isEqual.js';
import cloneDeep from 'lodash/cloneDeep.js';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';

export default {
    toggleVolFilesDisplay(dispatch, data) {
        dispatch({ type: actionTypes.TOGGLE_VOL_DISPLAY, data });
    },

    addModule(dispatch, isBase) {
        dispatch({ type: actionTypes.ADD_MODULE, data: { isBase } });
    },

    deleteModule(dispatch, moduleName) {
        dispatch({ type: actionTypes.REMOVE_MODULE, data: { moduleName } });
    },

    renameModule(dispatch, { oldName, newName }) {
        dispatch({
            type: actionTypes.RENAME_MODULE,
            data: { oldName, newName }
        });
    },

    changeModuleFileType(dispatch, { module, value, type }) {
        if (!value) {
            value = { value: '' };
        }
        dispatch({
            type: actionTypes.FILE_ASSIGN_CHANGED,
            data: { module, value, type }
        });
    },

    changeArtifactList(dispatch, artifacts) {
        dispatch({
            type: actionTypes.ARTIFACT_LIST_CHANGE,
            data: { artifacts: artifacts.map(artifact => artifact.value) }
        });
    },

    processAndValidateHeat(
        dispatch,
        { softwareProductId, heatData, heatDataCache, isReadOnlyMode, version }
    ) {
        return isEqual({ ...heatData, softwareProductId }, heatDataCache) ||
            isReadOnlyMode
            ? Promise.resolve()
            : SoftwareProductActionHelper.updateSoftwareProductHeatCandidate(
                  dispatch,
                  { softwareProductId, heatCandidate: heatData, version }
              )
                  .then(() =>
                      SoftwareProductActionHelper.processAndValidateHeatCandidate(
                          dispatch,
                          { softwareProductId, version }
                      )
                  )
                  .then(() =>
                      dispatch({
                          type: actionTypes.FILL_HEAT_SETUP_CACHE,
                          payload: { ...cloneDeep(heatData), softwareProductId }
                      })
                  );
    },

    addAllUnassignedFilesToArtifacts(dispatch) {
        dispatch({ type: actionTypes.ADD_ALL_UNASSIGNED_TO_ARTIFACTS });
    },

    heatSetupLeaveConfirmation() {
        return Promise.resolve();
    }

    /*heatSetupLeaveConfirmation(dispatch, {softwareProductId, heatSetup, heatSetupCache}) {
		return new Promise((resolve, reject) => {
			if (isEqual({...heatSetup, softwareProductId}, heatSetupCache)) {
				resolve();
			} else {
				dispatch({
					type: modalActionTypes.GLOBAL_MODAL_WARNING,
					data:{
						msg: i18n(`You have uploaded a new HEAT. If you navigate away or Check-in without proceeding to validation,
							Old HEAT zip file will be in use. new HEAT will be ignored. Do you want to continue?`),
						confirmationButtonText: i18n('Continue'),
						onConfirmed: () => resolve(),
						onDeclined: () => reject()
					}
				});
			}
		});
	}*/
};
