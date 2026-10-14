/*
 * Copyright (C) 2026 Deutsche Telekom AG
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
import { render, fireEvent } from '@testing-library/react';
import Input from 'nfvo-components/input/validation/Input.jsx';
import InputOptions from 'nfvo-components/input/validation/InputOptions.jsx';
import InputWrapper from 'nfvo-components/input/validation/InputWrapper.jsx';
import SelectInput from 'nfvo-components/input/SelectInput.jsx';
import ExpandableInput from 'nfvo-components/input/ExpandableInput.jsx';

const noop = () => {};

describe('Input - label association', () => {
    it('labels text, textarea, select and number controls', () => {
        const { getByLabelText } = render(
            <div>
                <Input
                    type="text"
                    label="Name"
                    data-test-id="vsp-name"
                    value="a"
                    onChange={noop}
                />
                <Input
                    type="textarea"
                    label="Description"
                    value="a"
                    onChange={noop}
                />
                <Input type="select" label="Vendor" value="1" onChange={noop}>
                    <option value="1">x</option>
                </Input>
                <Input type="number" label="Size" value={1} onChange={noop} />
            </div>
        );
        expect(getByLabelText('Name').getAttribute('data-test-id')).toBe(
            'vsp-name'
        );
        expect(getByLabelText('Description').tagName).toBe('TEXTAREA');
        expect(getByLabelText('Vendor').tagName).toBe('SELECT');
        expect(getByLabelText('Size').getAttribute('type')).toBe('number');
    });

    it('keeps an explicit id and gives every other input its own', () => {
        const { getByLabelText } = render(
            <div>
                <Input type="text" id="given" label="Given" onChange={noop} />
                <Input type="text" label="First" onChange={noop} />
                <Input type="text" label="Second" onChange={noop} />
            </div>
        );
        expect(getByLabelText('Given').id).toBe('given');
        const first = getByLabelText('First').id;
        const second = getByLabelText('Second').id;
        expect(first).toBeTruthy();
        expect(second).toBeTruthy();
        expect(first).not.toBe(second);
    });
});

describe('InputOptions - label association', () => {
    const values = [{ enum: 'a', title: 'A' }, { enum: 'b', title: 'B' }];

    it('labels the single select', () => {
        const { getByLabelText } = render(
            <InputOptions
                label="Region"
                values={values}
                selectedEnum="a"
                onEnumChange={noop}
            />
        );
        expect(getByLabelText('Region').tagName).toBe('SELECT');
    });

    it('labels the multi select input', () => {
        const { getByLabelText } = render(
            <InputOptions
                label="Regions"
                values={values}
                isMultiSelect={true}
                multiSelectedEnum={['a']}
                onEnumChange={noop}
            />
        );
        expect(getByLabelText('Regions').getAttribute('role')).toBe(
            'combobox'
        );
    });

    it('forwards aria-label when there is no visible label', () => {
        const { getByLabelText } = render(
            <div>
                <InputOptions
                    aria-label="Region"
                    values={values}
                    selectedEnum="a"
                    onEnumChange={noop}
                />
                <InputOptions
                    aria-label="Regions"
                    values={values}
                    isMultiSelect={true}
                    multiSelectedEnum={['a']}
                    onEnumChange={noop}
                />
            </div>
        );
        expect(getByLabelText('Region').tagName).toBe('SELECT');
        expect(getByLabelText('Regions').getAttribute('role')).toBe(
            'combobox'
        );
    });
});

describe('SelectInput', () => {
    const options = [{ value: 'a', label: 'A' }, { value: 'b', label: 'B' }];

    it('labels the react-select input', () => {
        const { getByLabelText } = render(
            <SelectInput
                label="Feature Groups"
                multi
                options={options}
                value={['a']}
                onMultiSelectChanged={noop}
            />
        );
        expect(getByLabelText('Feature Groups').getAttribute('role')).toBe(
            'combobox'
        );
    });

    it('renders selected values without an orphan option role', () => {
        const { container, getByText } = render(
            <SelectInput
                label="Feature Groups"
                multi
                options={options}
                value={['a']}
                onMultiSelectChanged={noop}
            />
        );
        expect(getByText('A')).toBeTruthy();
        expect(container.querySelector('[role="option"]')).toBeNull();
        expect(container.querySelector('[aria-selected]')).toBeNull();
    });
});

describe('InputWrapper - label association', () => {
    it('labels its text control and forwards aria-label', () => {
        const { getByLabelText } = render(
            <div>
                <InputWrapper type="text" label="Labelled" onChange={noop} />
                <InputWrapper
                    type="text"
                    aria-label="Unlabelled"
                    onChange={noop}
                />
            </div>
        );
        expect(getByLabelText('Labelled').tagName).toBe('INPUT');
        expect(getByLabelText('Unlabelled').tagName).toBe('INPUT');
    });
});

describe('ExpandableInput', () => {
    it('names the opened search input', () => {
        const { container, getByLabelText } = render(
            <ExpandableInput iconType="search" onChange={noop} value="" />
        );
        fireEvent.click(
            container.querySelector('[data-test-id="expandable-input-closed"]')
        );
        expect(getByLabelText('Search').getAttribute('data-test-id')).toBe(
            'expandable-input-opened'
        );
    });
});
