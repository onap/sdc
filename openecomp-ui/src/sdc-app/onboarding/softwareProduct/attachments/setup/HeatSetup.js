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
import HeatSetupView from './HeatSetupView.jsx';
import HeatSetupActionHelper from './HeatSetupActionHelper.js';

const BASE = true;

function baseExists(modules) {
    for (let i in modules) {
        if (modules[i].isBase) {
            return true;
        }
    }
    return false;
}

export const mapStateToProps = ({
    softwareProduct: {
        softwareProductAttachments: { heatSetup, heatSetupCache }
    }
}) => {
    let {
        modules = [],
        unassigned = [],
        artifacts = [],
        nested = []
    } = heatSetup;
    let isBaseExist = baseExists(modules);

    return {
        heatSetupCache,
        modules,
        unassigned,
        artifacts,
        nested,
        isBaseExist
    };
};

export const mapActionsToProps = (dispatch, {}) => {
    return {
        onModuleRename: (oldName, newName) =>
            HeatSetupActionHelper.renameModule(dispatch, { oldName, newName }),
        onModuleAdd: () => HeatSetupActionHelper.addModule(dispatch, !BASE),
        onBaseAdd: () => HeatSetupActionHelper.addModule(dispatch, BASE),
        onModuleDelete: moduleName =>
            HeatSetupActionHelper.deleteModule(dispatch, moduleName),
        onModuleFileTypeChange: ({ module, value, type }) =>
            HeatSetupActionHelper.changeModuleFileType(dispatch, {
                module,
                value,
                type
            }),
        onToggleVolFilesDisplay: ({ module, value }) => {
            HeatSetupActionHelper.toggleVolFilesDisplay(dispatch, {
                module,
                value
            });
        },
        onArtifactListChange: artifacts =>
            HeatSetupActionHelper.changeArtifactList(dispatch, artifacts),
        onAddAllUnassigned: () =>
            HeatSetupActionHelper.addAllUnassignedFilesToArtifacts(dispatch)
    };
};

export default connect(mapStateToProps, mapActionsToProps, null, {
    withRef: true
})(HeatSetupView);
