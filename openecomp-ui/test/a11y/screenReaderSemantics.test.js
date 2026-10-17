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
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import SelectActionTableCell from 'nfvo-components/table/SelectActionTableCell.jsx';
import LicenseAgreementListEditorView from 'sdc-app/onboarding/licenseModel/licenseAgreement/LicenseAgreementListEditorView.jsx';
import SoftwareProductComponentsListView from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsListView.jsx';
import ArtifactOrNestedFileList from 'sdc-app/onboarding/softwareProduct/attachments/setup/components/ArtifactOrNestedFileList.js';
import UnassignedFileList from 'sdc-app/onboarding/softwareProduct/attachments/setup/components/UnassignedFileList.js';
import SoftwareProductValidationResultsView from 'sdc-app/onboarding/softwareProduct/validationResults/SoftwareProductValidationResultsView.jsx';
import {
    documentTitleFor,
    DocumentTitleKeeper
} from 'sdc-app/onboarding/onboardingDocumentTitle.js';
import { enums } from 'sdc-app/onboarding/OnboardingConstants.js';

const noop = () => {};

describe('List rows: the Open button is named by the item, not the row', () => {
    it('uses itemName when given', () => {
        const { getByRole } = render(
            <ListEditorItemView itemName="LA 1" onSelect={noop}>
                <div>Name LA 1 Type Fixed Term Description long text</div>
            </ListEditorItemView>
        );
        const open = getByRole('button', { name: 'Open LA 1' });
        expect(open.hasAttribute('aria-labelledby')).toBe(false);
    });

    it('falls back to the whole row without itemName', () => {
        const { container } = render(
            <ListEditorItemView onSelect={noop}>
                <div>row</div>
            </ListEditorItemView>
        );
        const open = container.querySelector('.list-editor-item-view-open');
        expect(open.getAttribute('aria-labelledby')).toContain(open.id);
    });

    it('license agreements pass the agreement name', () => {
        const { getByRole } = render(
            <LicenseAgreementListEditorView
                licenseModelId="vlm"
                isReadOnlyMode={false}
                version={{ id: 'v' }}
                licenseAgreementList={[
                    {
                        id: 'la1',
                        name: 'LA 1',
                        description: '... vendor license agreement',
                        licenseTerm: { choice: 'Fixed_Term' },
                        featureGroupsIds: ['fg']
                    }
                ]}
                onAddLicenseAgreementClick={noop}
                onEditLicenseAgreementClick={noop}
                onDeleteLicenseAgreement={noop}
            />
        );
        expect(getByRole('button', { name: 'Open LA 1' })).toBeTruthy();
    });

    it('VSP components pass the display name', () => {
        const { getByRole } = render(
            <SoftwareProductComponentsListView
                componentsList={[
                    {
                        id: 'c1',
                        name: 'vpg',
                        displayName: 'vpg',
                        description: ''
                    }
                ]}
                currentSoftwareProduct={{ id: 'vsp' }}
                version={{ id: 'v' }}
                onComponentSelect={noop}
                isReadOnlyMode={true}
            />
        );
        expect(getByRole('button', { name: 'Open vpg' })).toBeTruthy();
    });

    it('the truncation glyph has an empty alt text', () => {
        const utils = fs.readFileSync(
            path.join(__dirname, '../../resources/scss/common/_utils.scss'),
            'utf8'
        );
        expect(utils).toMatch(/content:\s*'\.\.\.'\s*\/\s*'';/);
    });
});

describe('Section titles are headings', () => {
    it('ListEditorView renders its title as h2', () => {
        const { getByRole } = render(
            <ListEditorView title="License Agreements">{[]}</ListEditorView>
        );
        const heading = getByRole('heading', {
            level: 2,
            name: 'License Agreements'
        });
        expect(heading.className).toBe('list-editor-view-title');
    });

    it('HEAT setup lists have h2 titles and the artifact select is named', () => {
        const { getByRole } = render(
            <div>
                <ArtifactOrNestedFileList
                    type="artifact"
                    title="ARTIFACTS"
                    options={[]}
                    selected={[]}
                    onAddAllUnassigned={noop}
                />
                <ArtifactOrNestedFileList
                    type="nested"
                    title="NESTED HEAT FILES"
                    options={[]}
                    selected={[]}
                />
                <UnassignedFileList>
                    <ul />
                </UnassignedFileList>
            </div>
        );
        ['ARTIFACTS', 'NESTED HEAT FILES', 'UNASSIGNED FILES'].forEach(name =>
            expect(getByRole('heading', { level: 2, name })).toBeTruthy()
        );
        expect(getByRole('combobox', { name: 'ARTIFACTS' })).toBeTruthy();
    });

    it('validation results without tests do not skip to h4', () => {
        const view = new SoftwareProductValidationResultsView({
            softwareProductId: 'vsp',
            version: { name: '1.0' },
            softwareProductValidationResult: {}
        });
        const { container, getByRole } = render(view.render());
        expect(
            getByRole('heading', { level: 3, name: 'No Test Performed' })
        ).toBeTruthy();
        expect(container.querySelector('h4')).toBeNull();
    });
});

describe('Table selects are named', () => {
    it('SelectActionTableCell forwards ariaLabel', () => {
        const { getByRole } = render(
            <SelectActionTableCell
                ariaLabel="Source"
                options={[{ value: 'a', label: 'A' }]}
                onChange={noop}
            />
        );
        expect(getByRole('combobox', { name: 'Source' })).toBeTruthy();
    });
});

describe('Punch-out document title', () => {
    const level = (selectedKey, displayText) => ({
        selectedKey,
        menuItems: [{ key: selectedKey, displayText }]
    });

    it('names page and VLM', () => {
        expect(
            documentTitleFor([
                level('vlm1', 'Vendor A'),
                level(enums.BREADCRUMS.LICENSE_MODEL, 'License Model'),
                level(enums.BREADCRUMS.LICENSE_AGREEMENTS, 'License Agreements')
            ])
        ).toBe('License Agreements - Vendor A - SDC');
    });

    it('names page and VSP, without the vendor', () => {
        expect(
            documentTitleFor([
                level('vlm1', 'Vendor A'),
                level(enums.BREADCRUMS.SOFTWARE_PRODUCT, 'Software Products'),
                level('vsp1', 'vFW'),
                level(enums.BREADCRUMS.SOFTWARE_PRODUCT_DETAILS, 'General')
            ])
        ).toBe('General - vFW - SDC');
    });

    it('names the versions page of a VLM', () => {
        expect(
            documentTitleFor([
                level('vlm1', 'Vendor A'),
                level(enums.BREADCRUMS.VERSIONS_PAGE, 'Versions Page')
            ])
        ).toBe('Versions Page - Vendor A - SDC');
    });

    it('has no title of its own on the onboarding home', () => {
        expect(documentTitleFor([])).toBeNull();
    });

    it('sets the title while an item is open and restores the host title', () => {
        document.title = 'Onboard - SDC';
        const keeper = new DocumentTitleKeeper();
        keeper.update([level('vlm1', 'Vendor A')]);
        expect(document.title).toBe('Vendor A - SDC');
        keeper.update([
            level('vlm1', 'Vendor A'),
            level(enums.BREADCRUMS.LICENSE_MODEL, 'License Model'),
            level(enums.BREADCRUMS.ACTIVITY_LOG, 'Activity Log')
        ]);
        expect(document.title).toBe('Activity Log - Vendor A - SDC');
        keeper.update([]);
        expect(document.title).toBe('Onboard - SDC');
    });

    it('does not overwrite a title the host set after ours', () => {
        document.title = 'Onboard - SDC';
        const keeper = new DocumentTitleKeeper();
        keeper.update([level('vlm1', 'Vendor A')]);
        document.title = 'Catalog - SDC';
        keeper.restore();
        expect(document.title).toBe('Catalog - SDC');
    });
});
