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

import fs from 'fs';
import path from 'path';
import React from 'react';
import { render } from '@testing-library/react';
import Input from 'nfvo-components/input/validation/Input.jsx';
import InputOptions from 'nfvo-components/input/validation/InputOptions.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import LicenseModelCreationView from 'sdc-app/onboarding/licenseModel/creation/LicenseModelCreationView.jsx';

const noop = () => {};

describe('Input - required and error state', () => {
    it.each(['text', 'number', 'textarea', 'file'])(
        'marks a required %s as aria-required without native validation',
        type => {
            const { getByLabelText } = render(
                <Input
                    type={type}
                    label="Vendor Name"
                    isRequired
                    onChange={noop}
                />
            );
            const input = getByLabelText('Vendor Name');
            expect(input.getAttribute('aria-required')).toBe('true');
            expect(input.hasAttribute('required')).toBe(false);
            expect(input.hasAttribute('aria-invalid')).toBe(false);
            expect(input.hasAttribute('aria-describedby')).toBe(false);
        }
    );

    it('leaves an optional field without aria-required', () => {
        const { getByLabelText } = render(
            <Input type="text" label="Description" onChange={noop} />
        );
        expect(
            getByLabelText('Description').hasAttribute('aria-required')
        ).toBe(false);
    });

    it('points an invalid field at its error text', () => {
        const { getByLabelText } = render(
            <Input
                type="text"
                label="Vendor Name"
                isRequired
                isValid={false}
                errorText="Field is required"
                value=""
                onChange={noop}
            />
        );
        const input = getByLabelText('Vendor Name');
        expect(input.getAttribute('aria-invalid')).toBe('true');
        const errorId = input.getAttribute('aria-describedby');
        expect(errorId).toBe(`${input.id}-error`);
        const error = document.getElementById(errorId);
        expect(error).not.toBeNull();
        expect(error.textContent).toBe('Field is required');
    });

    it('describes a select by its error text', () => {
        const { getByLabelText } = render(
            <Input
                type="select"
                label="Vendor"
                isRequired
                isValid={false}
                errorText="Field is required"
                onChange={noop}>
                <option value="">-</option>
            </Input>
        );
        const select = getByLabelText('Vendor');
        expect(select.getAttribute('aria-required')).toBe('true');
        expect(select.getAttribute('aria-invalid')).toBe('true');
        expect(
            document.getElementById(select.getAttribute('aria-describedby'))
                .textContent
        ).toBe('Field is required');
    });
});

describe('Create VLM dialog', () => {
    it('exposes the required Vendor Name and its error text', () => {
        const { getByLabelText } = render(
            <LicenseModelCreationView
                data={{ vendorName: '', description: 'd' }}
                genericFieldInfo={{
                    vendorName: {
                        isValid: false,
                        errorText: 'Field is required'
                    },
                    description: { isValid: true, errorText: '' }
                }}
                onDataChanged={noop}
                onSubmit={noop}
                onValidateForm={noop}
                onCancel={noop}
                isNameUnique={noop}
            />
        );
        const name = getByLabelText('Vendor Name');
        expect(name.getAttribute('aria-required')).toBe('true');
        expect(name.getAttribute('aria-invalid')).toBe('true');
        expect(
            document.getElementById(name.getAttribute('aria-describedby'))
                .textContent
        ).toBe('Field is required');
        const description = getByLabelText('Description');
        expect(description.getAttribute('aria-required')).toBe('true');
        expect(description.hasAttribute('aria-invalid')).toBe(false);
    });
});

describe('InputOptions - required and error state', () => {
    const values = [{ enum: 'a', title: 'A' }];

    it('exposes required, invalid and the error text on the select', () => {
        const { getByLabelText } = render(
            <InputOptions
                label="Region"
                values={values}
                isRequired
                isValid={false}
                errorText="Field is required"
                onEnumChange={noop}
            />
        );
        const select = getByLabelText('Region');
        expect(select.getAttribute('aria-required')).toBe('true');
        expect(select.getAttribute('aria-invalid')).toBe('true');
        expect(
            document.getElementById(select.getAttribute('aria-describedby'))
                .textContent
        ).toBe('Field is required');
    });

    it('describes the multi select input by the error text', () => {
        const { getByLabelText } = render(
            <InputOptions
                label="Regions"
                values={values}
                isMultiSelect
                multiSelectedEnum={[]}
                isValid={false}
                errorText="Field is required"
                onEnumChange={noop}
            />
        );
        const input = getByLabelText('Regions');
        expect(
            document.getElementById(input.getAttribute('aria-describedby'))
                .textContent
        ).toBe('Field is required');
    });
});

describe('Required asterisk is not part of the accessible name', () => {
    const scss = file =>
        fs.readFileSync(
            path.join(__dirname, '../../../resources/scss', file),
            'utf8'
        );

    it.each(['bootstrap-cust/_forms.scss', 'components/_datepicker.scss'])(
        '%s gives the generated "*" an empty alt text',
        file => {
            const contents = scss(file);
            expect(contents).toMatch(/content:\s*(['"])\*\1\s*\/\s*(['"])\2;/);
        }
    );

    it('GridSection hides its required marker', () => {
        const { getByRole } = render(
            <GridSection title="General" required>
                <span />
            </GridSection>
        );
        const heading = getByRole('heading', { level: 2 });
        expect(
            heading.querySelector('.required').getAttribute('aria-hidden')
        ).toBe('true');
    });
});
