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
import React from 'react';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import NameEditInput from './NameEditInput.js';
import SelectWithFileType from './SelectWithFileType';
import { fileTypes } from '../HeatSetupConstants.js';
import AddOrDeleteVolumeFiles from './AddOrDeleteVolumeFiles';

class ModuleFile extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            isInNameEdit: false
        };
    }

    handleSubmit(event, name) {
        if (event.keyCode === 13) {
            this.handleModuleRename(event, name);
        }
    }

    handleModuleRename(event, name) {
        this.setState({ isInNameEdit: false });
        this.props.onModuleRename(name, event.target.value);
    }

    deleteVolumeFiles() {
        const { onModuleFileTypeChange, onToggleVolFilesDisplay } = this.props;
        onModuleFileTypeChange(null, fileTypes.VOL.label);
        onModuleFileTypeChange(null, fileTypes.VOL_ENV.label);
        onToggleVolFilesDisplay(false);
    }

    renderNameAccordingToEditState() {
        const { module: { name } } = this.props;
        if (this.state.isInNameEdit) {
            return (
                <NameEditInput
                    defaultValue={name}
                    onBlur={evt => this.handleModuleRename(evt, name)}
                    onKeyDown={evt => this.handleSubmit(evt, name)}
                />
            );
        }
        return <span className="filename-text">{name}</span>;
    }

    render() {
        const {
            module: { name, isBase, yaml, env, vol, volEnv },
            onModuleDelete,
            files,
            onModuleFileTypeChange,
            onToggleVolFilesDisplay,
            isReadOnlyMode,
            displayVolumes
        } = this.props;

        //const { displayVolumes } = this.state;

        const moduleType = isBase ? 'BASE' : 'MODULE';
        return (
            <div className="modules-list-item" data-test-id="module-item">
                <div className="modules-list-item-controllers">
                    <div className="modules-list-item-filename">
                        <SVGIcon
                            name={isBase ? 'base' : 'module'}
                            color="primary"
                            iconClassName="heat-setup-module-icon"
                        />
                        <span className="module-title-by-type">{`${moduleType}: `}</span>
                        <div
                            className={`text-and-icon ${
                                this.state.isInNameEdit ? 'in-edit' : ''
                            }`}>
                            {this.renderNameAccordingToEditState()}
                            {!this.state.isInNameEdit && (
                                <SVGIcon
                                    name="pencil"
                                    onClick={() =>
                                        this.setState({ isInNameEdit: true })
                                    }
                                    data-test-id={
                                        isBase ? 'base-name' : 'module-name'
                                    }
                                />
                            )}
                        </div>
                    </div>
                    <SVGIcon
                        name="trashO"
                        onClick={() => onModuleDelete(name)}
                        data-test-id="module-delete"
                    />
                </div>
                <div className="modules-list-item-selectors">
                    <SelectWithFileType
                        type={fileTypes.YAML}
                        files={files}
                        selected={yaml}
                        onChange={onModuleFileTypeChange}
                    />
                    <SelectWithFileType
                        type={fileTypes.ENV}
                        files={files}
                        selected={env}
                        onChange={onModuleFileTypeChange}
                    />
                    {displayVolumes && (
                        <SelectWithFileType
                            type={fileTypes.VOL}
                            files={files}
                            selected={vol}
                            onChange={onModuleFileTypeChange}
                        />
                    )}
                    {displayVolumes && (
                        <SelectWithFileType
                            type={fileTypes.VOL_ENV}
                            files={files}
                            selected={volEnv}
                            onChange={onModuleFileTypeChange}
                        />
                    )}
                    <AddOrDeleteVolumeFiles
                        isReadOnlyMode={isReadOnlyMode}
                        onAdd={() => onToggleVolFilesDisplay(true)}
                        onDelete={() => this.deleteVolumeFiles()}
                        add={!displayVolumes}
                    />
                </div>
            </div>
        );
    }
}

export default ModuleFile;
