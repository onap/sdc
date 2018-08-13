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
import i18n from 'nfvo-utils/i18n/i18n.js';
import Button from 'sdc-ui/lib/react/Button.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import SelectInput from 'nfvo-components/input/SelectInput.jsx';

const NestedList = ({ selected }) => (
    <ul className="nested-list">
        {selected.map(nested => (
            <li key={nested} className="nested-list-item">
                {nested}
            </li>
        ))}
    </ul>
);

const ArtifactOrNestedFileList = ({
    type,
    title,
    selected,
    options,
    onSelectChanged,
    onAddAllUnassigned,
    isReadOnlyMode,
    headerClassName
}) => (
    <div
        className={`artifact-files ${
            type === 'nested' ? 'nested' : ''
        } ${headerClassName} `}>
        <div className="artifact-files-header">
            <span>
                {type === 'artifact' && (
                    <SVGIcon
                        color="primary"
                        name="artifacts"
                        iconClassName="heat-setup-module-icon"
                    />
                )}
                {`${title}`}
            </span>
            {type === 'artifact' && (
                <Button
                    disabled={isReadOnlyMode}
                    btnType="link"
                    className="add-all-unassigned"
                    onClick={onAddAllUnassigned}>
                    {i18n('Add All Unassigned Files')}
                </Button>
            )}
        </div>
        {type === 'nested' ? (
            <NestedList selected={selected} />
        ) : (
            <SelectInput
                options={options}
                onMultiSelectChanged={onSelectChanged || (() => {})}
                value={selected}
                clearable={false}
                placeholder={i18n('Add Artifact')}
                multi
            />
        )}
    </div>
);

export default ArtifactOrNestedFileList;
