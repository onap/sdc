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
import ShallowRenderer from 'react-test-renderer/shallow';
import { render, fireEvent } from '@testing-library/react';
import SoftwareProductAttachmentsView from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachmentsView.jsx';
import Datepicker from 'nfvo-components/datepicker/Datepicker.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import SelectActionTableRow from 'nfvo-components/table/SelectActionTableRow.jsx';
import SoftwareProductDependenciesView from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependenciesView.jsx';
import { NEW_RULE_TEMP_ID } from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependenciesConstants.js';
import ModuleFile from 'sdc-app/onboarding/softwareProduct/attachments/setup/components/ModuleFile.js';
// Before VNFImportView: it reaches the modal content map through an import cycle that
// only resolves from this side.
import 'sdc-app/common/modal/ModalContentMapper.js';
import { VNFItemList } from 'sdc-app/onboarding/softwareProduct/vnfMarketPlace/VNFImportView.jsx';
import CatalogList from 'sdc-app/onboarding/onboard/CatalogList.jsx';
import SummaryCountItem from 'sdc-app/onboarding/licenseModel/overview/summary/SummaryCountItem.jsx';
import SoftwareProductValidationResultsView from 'sdc-app/onboarding/softwareProduct/validationResults/SoftwareProductValidationResultsView.jsx';

const noop = () => {};

const nameOf = button =>
    button.getAttribute('aria-label') || button.textContent.trim();

const buttonNames = container =>
    Array.from(container.querySelectorAll('button')).map(nameOf);

const findShallow = (node, predicate, found = []) => {
    if (!node || typeof node !== 'object') {
        return found;
    }
    if (Array.isArray(node)) {
        node.forEach(child => findShallow(child, predicate, found));
        return found;
    }
    if (predicate(node)) {
        found.push(node);
    }
    if (node.props) {
        findShallow(node.props.children, predicate, found);
    }
    return found;
};

describe('Clickable icons and accordion headers have an accessible name', () => {
    it('Datepicker: clear button', () => {
        const { getByRole } = render(
            <Datepicker date="01/01/2026" format="MM/DD/YYYY" onChange={noop} />
        );
        expect(getByRole('button', { name: 'Clear date' })).toBeTruthy();
    });

    it('ListEditorItemView: edit and delete buttons', () => {
        const { getByRole } = render(
            <ListEditorItemView onEdit={noop} onDelete={noop}>
                <span>item</span>
            </ListEditorItemView>
        );
        expect(getByRole('button', { name: 'Edit' })).toBeTruthy();
        const del = getByRole('button', { name: 'Delete' });
        expect(del.getAttribute('data-test-id')).toBe('delete-list-item');
    });

    it('SelectActionTableRow: action button, disabled while hidden', () => {
        const onAction = jest.fn();
        const { getByRole, rerender } = render(
            <SelectActionTableRow
                actionIcon="plusCircle"
                actionAriaLabel="Add Dependency"
                onAction={onAction}
                showAction={false}
            />
        );
        const add = getByRole('button', { name: 'Add Dependency' });
        expect(add.disabled).toBe(true);
        fireEvent.click(add);
        expect(onAction).not.toHaveBeenCalled();
        rerender(
            <SelectActionTableRow
                actionIcon="plusCircle"
                actionAriaLabel="Add Dependency"
                onAction={onAction}
                showAction={true}
            />
        );
        fireEvent.click(getByRole('button', { name: 'Add Dependency' }));
        expect(onAction).toHaveBeenCalledTimes(1);
    });

    it('SoftwareProductDependenciesView: add and delete dependency buttons', () => {
        const dependencies = [
            {
                id: NEW_RULE_TEMP_ID,
                sourceId: null,
                targetId: null,
                relationType: null
            },
            {
                id: 'd1',
                sourceId: 'a',
                targetId: 'b',
                relationType: 'dependsOn'
            }
        ];
        const renderer = new ShallowRenderer();
        renderer.render(
            <SoftwareProductDependenciesView
                componentsOptions={[]}
                softwareProductDependencies={dependencies}
                onDataChanged={noop}
                onAddDependency={noop}
                onDeleteDependency={noop}
                isReadOnlyMode={false}
            />
        );
        const rows = findShallow(
            renderer.getRenderOutput(),
            n => n.props && n.props.actionIcon
        );
        expect(
            rows.map(r => [r.props.actionIcon, r.props.actionAriaLabel])
        ).toEqual([
            ['plusCircle', 'Add Dependency'],
            ['trashO', 'Delete Dependency']
        ]);
    });

    it('SoftwareProductAttachmentsView: download and upload buttons', () => {
        const renderer = new ShallowRenderer();
        renderer.render(
            <SoftwareProductAttachmentsView
                heatDataExist
                isReadOnlyMode={false}
                candidateInProcess={false}
                heatSetup={{}}
                version={{ id: 'v1' }}
                activeTab={1}
                onDownload={noop}
                setActiveTab={noop}
                HeatSetupComponent={() => null}
            />
        );
        const icons = findShallow(
            renderer.getRenderOutput(),
            n =>
                n.props &&
                ['download-heat', 'upload-heat'].includes(
                    n.props['data-test-id']
                )
        );
        expect(
            icons.map(i => [
                i.props['data-test-id'],
                i.props.ariaLabel,
                typeof i.props.onClick
            ])
        ).toEqual([
            ['download-heat', 'Download HEAT', 'function'],
            ['upload-heat', 'Upload New HEAT', 'function']
        ]);
    });

    it('ModuleFile: rename and delete buttons', () => {
        const { getByRole } = render(
            <ModuleFile
                module={{ name: 'base_module', isBase: true }}
                files={[]}
                onModuleDelete={noop}
                onModuleRename={noop}
                onModuleFileTypeChange={noop}
                onToggleVolFilesDisplay={noop}
                isReadOnlyMode={false}
                displayVolumes={false}
            />
        );
        expect(getByRole('button', { name: 'Rename module' })).toBeTruthy();
        expect(getByRole('button', { name: 'Delete module' })).toBeTruthy();
    });

    it('VNFItemList: download CSAR button', () => {
        const { getByRole } = render(
            <ul>
                <VNFItemList
                    vnf={{ csarId: 'c1', name: 'vnf', version: '1' }}
                    downloadCSAR={noop}
                    selectTableRow={noop}
                />
            </ul>
        );
        expect(getByRole('button', { name: 'Download CSAR' })).toBeTruthy();
    });

    it('CatalogList: back button of the vendor page', () => {
        const { getByRole } = render(
            <CatalogList
                vendorPageOptions={{
                    onBack: noop,
                    selectedVendor: { name: 'Vendor A' }
                }}
            />
        );
        expect(getByRole('button', { name: 'Back' })).toBeTruthy();
    });

    it('SummaryCountItem: add button names the item type', () => {
        const { getByRole } = render(
            <SummaryCountItem
                name="License Agreements"
                counter={2}
                onAdd={noop}
                onNavigate={noop}
                isReadOnlyMode={false}
            />
        );
        const add = getByRole('button', { name: 'Add License Agreements' });
        expect(add.getAttribute('data-test-id')).toBe(
            'vlm-summary-vendor-add-btn-license-agreements'
        );
    });

    it('SoftwareProductValidationResultsView: error details accordion', () => {
        const view = new SoftwareProductValidationResultsView({
            softwareProductId: 'vsp',
            version: { name: '1.0' }
        });
        const { getByRole } = render(
            <ul>{view.renderError({ unexpected: 'payload' }, 0)}</ul>
        );
        const header = getByRole('button', { name: 'Error details' });
        expect(header.getAttribute('aria-expanded')).toBe('true');
    });

    it('no rendered site nests a button inside another button or link', () => {
        const { container } = render(
            <div>
                <Datepicker
                    date="01/01/2026"
                    format="MM/DD/YYYY"
                    onChange={noop}
                />
                <ListEditorItemView onEdit={noop} onDelete={noop} />
                <SummaryCountItem
                    name="Feature Groups"
                    counter={0}
                    onAdd={noop}
                    onNavigate={noop}
                />
            </div>
        );
        expect(
            container.querySelectorAll('button button, a button').length
        ).toBe(0);
        expect(buttonNames(container).every(Boolean)).toBe(true);
    });
});
