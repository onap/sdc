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
import React, { Component } from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import { tabsMapping } from '../SoftwareProductAttachmentsConstants.js';
import SortableModuleFileList from './components/SortableModuleFileList';
import UnassignedFile from './components/UnassignedFile';
import UnassignedFileList from './components/UnassignedFileList';
import EmptyListContent from './components/EmptyListContent';
import ArtifactOrNestedFileList from './components/ArtifactOrNestedFileList';

const buildLabelValueObject = str =>
    typeof str === 'string' ? { value: str, label: str } : str;

class SoftwareProductHeatSetupView extends Component {
    processAndValidateHeat(heatData, heatDataCache) {
        let {
            onProcessAndValidate,
            changeAttachmentsTab,
            version
        } = this.props;
        onProcessAndValidate({ heatData, heatDataCache, version }).then(() =>
            changeAttachmentsTab(tabsMapping.VALIDATION)
        );
    }

    render() {
        let {
            modules,
            isReadOnlyMode,
            heatDataExist,
            unassigned,
            artifacts,
            nested,
            onArtifactListChange,
            onAddAllUnassigned
        } = this.props;

        const formattedUnassigned = unassigned.map(buildLabelValueObject);
        const formattedArtifacts = artifacts.map(buildLabelValueObject);
        return (
            <div
                className={`heat-setup-view ${
                    isReadOnlyMode ? 'disabled' : ''
                }`}>
                <div className="heat-setup-view-modules-and-artifacts">
                    <SortableModuleFileList
                        {...this.props}
                        isReadOnlyMode={this.props.isReadOnlyMode}
                        artifacts={formattedArtifacts}
                        unassigned={formattedUnassigned}
                    />
                    <ArtifactOrNestedFileList
                        type={'artifact'}
                        title={i18n('ARTIFACTS')}
                        options={formattedUnassigned}
                        selected={formattedArtifacts}
                        onSelectChanged={onArtifactListChange}
                        isReadOnlyMode={this.props.isReadOnlyMode}
                        headerClassName={
                            modules && modules.length > 0
                                ? 'with-list-items'
                                : ''
                        }
                        onAddAllUnassigned={onAddAllUnassigned}
                    />
                    <ArtifactOrNestedFileList
                        type={'nested'}
                        title={i18n('NESTED HEAT FILES')}
                        options={[]}
                        isReadOnlyMode={this.props.isReadOnlyMode}
                        selected={nested}
                    />
                </div>
                <UnassignedFileList>
                    {formattedUnassigned.length > 0 ? (
                        <ul>
                            {formattedUnassigned.map(file => (
                                <UnassignedFile
                                    key={file.label}
                                    name={file.label}
                                />
                            ))}
                        </ul>
                    ) : (
                        <EmptyListContent heatDataExist={heatDataExist} />
                    )}
                </UnassignedFileList>
            </div>
        );
    }
}

export default SoftwareProductHeatSetupView;
