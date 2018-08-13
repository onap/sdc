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
import isEqual from 'lodash/isEqual';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SortableListItem from './SortableListItem.js';
import { fileTypes } from '../HeatSetupConstants.js';

import Button from 'sdc-ui/lib/react/Button.js';
import ModuleFile from './ModuleFile.js';

class SortableModuleFileList extends React.Component {
    state = {
        draggingIndex: null,
        data: this.props.modules
    };

    componentDidUpdate() {
        if (!isEqual(this.state.data, this.props.modules)) {
            /* eslint-disable-next-line */
            this.setState({
                data: this.props.modules
            });
        }
    }

    render() {
        let {
            unassigned,
            onModuleRename,
            onModuleDelete,
            onModuleAdd,
            onBaseAdd,
            onModuleFileTypeChange,
            onToggleVolFilesDisplay,
            isBaseExist,
            isReadOnlyMode
        } = this.props;
        const childProps = module => ({
            module,
            onModuleRename,
            onModuleDelete,
            onModuleFileTypeChange: (value, type) => {
                if (
                    type === fileTypes.VOL.label ||
                    type === fileTypes.VOL_ENV.label
                ) {
                    onToggleVolFilesDisplay({ module, value: false });
                }
                onModuleFileTypeChange({ module, value, type });
            },

            files: unassigned,
            displayVolumes: Boolean(
                module.vol || module.volEnv || module.showVolFiles
            ),
            onToggleVolFilesDisplay: value =>
                onToggleVolFilesDisplay({ module, value })
        });

        let listItems = this.state.data.map(function(item, i) {
            return (
                <SortableListItem
                    key={i}
                    updateState={data => this.setState(data)}
                    items={this.state.data}
                    draggingIndex={this.state.draggingIndex}
                    sortId={i}
                    outline="list">
                    <ModuleFile
                        {...childProps(item)}
                        isReadOnlyMode={this.props.isReadOnlyMode}
                    />
                </SortableListItem>
            );
        }, this);

        return (
            <div
                className={`modules-list-wrapper ${
                    listItems.length > 0 ? 'modules-list-wrapper-divider' : ''
                }`}>
                <div className="modules-list-header">
                    {!isBaseExist && (
                        <div>
                            <Button
                                btnType="link"
                                onClick={onBaseAdd}
                                disabled={
                                    isReadOnlyMode || unassigned.length === 0
                                }>
                                {i18n('Add Base')}
                            </Button>
                        </div>
                    )}
                    <div>
                        <Button
                            btnType="link"
                            onClick={onModuleAdd}
                            disabled={
                                isReadOnlyMode || unassigned.length === 0
                            }>
                            {i18n('Add Module')}
                        </Button>
                    </div>
                </div>
                {listItems.length > 0 && <ul>{listItems}</ul>}
            </div>
        );
    }
}

export default SortableModuleFileList;
