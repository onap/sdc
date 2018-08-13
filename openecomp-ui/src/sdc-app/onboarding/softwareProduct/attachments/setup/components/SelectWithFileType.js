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
import SelectInput from 'nfvo-components/input/SelectInput.jsx';

const SelectWithFileType = ({ type, selected, files, onChange }) => {
    let filteredFiledAccordingToType = files.filter(
        file => file.label.search(type.regex) > -1
    );
    if (selected) {
        filteredFiledAccordingToType = filteredFiledAccordingToType.concat({
            label: selected,
            value: selected
        });
    }

    return (
        <SelectInput
            data-test-id={`${type.label}-list`}
            label={type.label}
            value={selected}
            onChange={value =>
                value !== selected && onChange(value, type.label)
            }
            disabled={filteredFiledAccordingToType.length === 0}
            placeholder={
                filteredFiledAccordingToType.length === 0 ? '' : undefined
            }
            clearable={true}
            options={filteredFiledAccordingToType}
        />
    );
};

export default SelectWithFileType;
