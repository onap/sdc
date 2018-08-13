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
import differenceWith from 'lodash/differenceWith.js';
import cloneDeep from 'lodash/cloneDeep';

const emptyModule = (isBase, currentLength) => ({
    name: `${isBase ? 'base_' : 'module_'}${currentLength + 1}`,
    isBase: isBase
});

function syncUnassignedFilesWithArtifactsChanges(
    unassigned,
    artifacts,
    oldArtifacts
) {
    if (artifacts.length > oldArtifacts.length) {
        return differenceWith(
            unassigned,
            artifacts,
            (unassignedFile, artifact) => unassignedFile === artifact
        );
    } else {
        const removedArtifact = differenceWith(
            oldArtifacts,
            artifacts,
            (oldArtifact, artifact) => artifact === oldArtifact
        );
        return [...unassigned, removedArtifact[0]];
    }
}

function findModuleIndexByName(modules, name) {
    return modules.findIndex(module => module.name === name);
}

function addDeletedModuleFilesToUnassigned(unassigned, deletedModule) {
    let files = [];
    for (let i in deletedModule) {
        if (deletedModule.hasOwnProperty(i)) {
            if (
                typeof deletedModule[i] === 'string' &&
                deletedModule[i] &&
                i !== 'name'
            ) {
                files.push(deletedModule[i]);
            }
        }
    }

    return unassigned.concat(files);
}

export default (state = {}, action) => {
    switch (action.type) {
        case actionTypes.TOGGLE_VOL_DISPLAY:
            let clonedState = cloneDeep(state);
            const indexToModify = findModuleIndexByName(
                clonedState.modules,
                action.data.module.name
            );
            let modToModify = clonedState.modules[indexToModify];
            modToModify.showVolFiles = action.data.value;
            return clonedState;
        case actionTypes.MANIFEST_LOADED:
            return {
                ...state,
                ...action.response,
                modules: action.response.modules.map(module => ({
                    ...module,
                    name:
                        module.name ||
                        module.yaml.substring(0, module.yaml.lastIndexOf('.'))
                }))
            };
        case actionTypes.ARTIFACT_LIST_CHANGE:
            return {
                ...state,
                artifacts: action.data.artifacts,
                unassigned: syncUnassignedFilesWithArtifactsChanges(
                    state.unassigned,
                    action.data.artifacts,
                    state.artifacts
                )
            };
        case actionTypes.ADD_ALL_UNASSIGNED_TO_ARTIFACTS:
            return {
                ...state,
                artifacts: [...state.artifacts, ...state.unassigned],
                unassigned: []
            };
        case actionTypes.ADD_ALL_ARTIFACTS_TO_UNASSIGNED:
            return {
                ...state,
                artifacts: [],
                unassigned: [...state.unassigned, ...state.artifacts]
            };
        case actionTypes.ADD_MODULE:
            return {
                ...state,
                modules: state.modules.concat({
                    ...emptyModule(action.data.isBase, state.modules.length)
                })
            };
        case actionTypes.REMOVE_MODULE:
            const moduleIndexToDelete = findModuleIndexByName(
                state.modules,
                action.data.moduleName
            );
            let unassigned = addDeletedModuleFilesToUnassigned(
                state.unassigned,
                state.modules[moduleIndexToDelete]
            );
            return {
                ...state,
                unassigned,
                modules: [
                    ...state.modules.slice(0, moduleIndexToDelete),
                    ...state.modules.slice(moduleIndexToDelete + 1)
                ]
            };
        case actionTypes.RENAME_MODULE:
            const indexToRename = findModuleIndexByName(
                state.modules,
                action.data.oldName
            );
            let moduleToRename = state.modules[indexToRename];
            moduleToRename.name = action.data.newName;
            return {
                ...state,
                modules: [
                    ...state.modules.slice(0, indexToRename),
                    moduleToRename,
                    ...state.modules.slice(indexToRename + 1)
                ]
            };
        case actionTypes.FILE_ASSIGN_CHANGED:
            let { module, value: { value }, type } = action.data;
            const moduleIndexToModify = findModuleIndexByName(
                state.modules,
                module.name
            );
            let moduleToModify = state.modules[moduleIndexToModify];
            let dumpedFile = moduleToModify[type];
            if (dumpedFile !== value) {
                if (value) {
                    moduleToModify[type] = value;
                } else {
                    delete moduleToModify[type];
                }
                const newUnassignedList = dumpedFile
                    ? [
                          ...state.unassigned.filter(file => file !== value),
                          dumpedFile
                      ]
                    : state.unassigned.filter(file => file !== value);
                return {
                    ...state,
                    modules: [
                        ...state.modules.slice(0, moduleIndexToModify),
                        moduleToModify,
                        ...state.modules.slice(moduleIndexToModify + 1)
                    ],
                    unassigned: newUnassignedList
                };
            } else {
                return state;
            }
        default:
            return state;
    }
};
