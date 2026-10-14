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
import { Provider } from 'react-redux';
import { render, fireEvent } from '@testing-library/react';
import { storeCreator } from 'sdc-app/AppStore.js';
import { OnboardHeaderTabs } from 'sdc-app/onboarding/onboard/OnboardView.jsx';
import WorkspaceView from 'sdc-app/onboarding/onboard/workspace/WorkspaceView.jsx';
import OnboardingCatalogView from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogView.jsx';
import { mapStateToProps } from 'sdc-app/onboarding/onboard/Onboard.js';
import { tabsMapping } from 'sdc-app/onboarding/onboard/OnboardConstants.js';
import { defaultStoreFactory } from 'test-utils/factories/onboard/OnboardingCatalogFactories.js';

const renderTabs = (activeTab = tabsMapping.WORKSPACE) => {
    const onTabClick = jest.fn();
    const utils = render(
        <OnboardHeaderTabs activeTab={activeTab} onTabClick={onTabClick} />
    );
    const tab = id => utils.container.querySelector(`[data-test-id="${id}"]`);
    return {
        ...utils,
        onTabClick,
        workspace: tab('onboard-workspace-tab'),
        catalog: tab('onboard-onboard-tab')
    };
};

describe('Onboard header tabs', () => {
    it('exposes a named tablist of native buttons', () => {
        const { getByRole, workspace, catalog } = renderTabs();
        const tablist = getByRole('tablist');
        expect(tablist.getAttribute('aria-label')).toBe('Onboarding views');
        [workspace, catalog].forEach(tab => {
            expect(tab.tagName).toBe('BUTTON');
            expect(tab.getAttribute('type')).toBe('button');
            expect(tab.getAttribute('role')).toBe('tab');
            expect(tab.getAttribute('aria-controls')).toBe('onboard-tabpanel');
        });
        expect(workspace.id).toBe('onboard-workspace-tab');
        expect(catalog.id).toBe('onboard-onboard-tab');
    });

    it('marks the active tab selected and keeps only it in the tab order', () => {
        const { workspace, catalog } = renderTabs();
        expect(workspace.getAttribute('aria-selected')).toBe('true');
        expect(workspace.getAttribute('tabindex')).toBe('0');
        expect(catalog.getAttribute('aria-selected')).toBe('false');
        expect(catalog.getAttribute('tabindex')).toBe('-1');
    });

    it('activates a tab on click', () => {
        const { catalog, onTabClick } = renderTabs();
        fireEvent.click(catalog);
        expect(onTabClick).toHaveBeenCalledWith(tabsMapping.CATALOG);
    });

    it('moves to the next and previous tab with the arrow keys, wrapping', () => {
        const { workspace, catalog, onTabClick } = renderTabs();
        fireEvent.keyDown(workspace, { key: 'ArrowRight' });
        expect(onTabClick).toHaveBeenLastCalledWith(tabsMapping.CATALOG);
        expect(document.activeElement).toBe(catalog);
        fireEvent.keyDown(workspace, { key: 'ArrowLeft' });
        expect(onTabClick).toHaveBeenLastCalledWith(tabsMapping.CATALOG);
        fireEvent.keyDown(catalog, { key: 'ArrowRight' });
        expect(onTabClick).toHaveBeenLastCalledWith(tabsMapping.WORKSPACE);
        expect(document.activeElement).toBe(workspace);
    });

    it('jumps to the first and last tab with Home and End', () => {
        const { workspace, catalog, onTabClick } = renderTabs(
            tabsMapping.CATALOG
        );
        fireEvent.keyDown(catalog, { key: 'Home' });
        expect(onTabClick).toHaveBeenLastCalledWith(tabsMapping.WORKSPACE);
        expect(document.activeElement).toBe(workspace);
        fireEvent.keyDown(workspace, { key: 'End' });
        expect(onTabClick).toHaveBeenLastCalledWith(tabsMapping.CATALOG);
        expect(document.activeElement).toBe(catalog);
    });

    it('ignores other keys', () => {
        const { workspace, onTabClick } = renderTabs();
        fireEvent.keyDown(workspace, { key: 'a' });
        expect(onTabClick).not.toHaveBeenCalled();
    });
});

describe('Onboard tab panels', () => {
    const props = () => ({
        ...mapStateToProps(defaultStoreFactory.build()),
        onAddLicenseModelClick: jest.fn(),
        onAddSoftwareProductClick: jest.fn(),
        onSelectLicenseModel: jest.fn(),
        onSelectSoftwareProduct: jest.fn(),
        onVspOverlayChange: jest.fn(),
        onVendorSelect: jest.fn(),
        onMigrate: jest.fn()
    });

    it('labels the workspace panel by its tab and gives it a heading', () => {
        const { getByRole } = render(
            <Provider store={storeCreator()}>
                <WorkspaceView {...props()} />
            </Provider>
        );
        const panel = getByRole('tabpanel');
        expect(panel.id).toBe('onboard-tabpanel');
        expect(panel.getAttribute('aria-labelledby')).toBe(
            'onboard-workspace-tab'
        );
        expect(getByRole('heading', { level: 1 }).textContent).toBe(
            'WORKSPACE'
        );
    });

    it('labels the catalog panel by its tab and gives it a heading', () => {
        const { getByRole } = render(
            <Provider store={storeCreator()}>
                <OnboardingCatalogView {...props()} />
            </Provider>
        );
        const panel = getByRole('tabpanel');
        expect(panel.id).toBe('onboard-tabpanel');
        expect(panel.getAttribute('aria-labelledby')).toBe(
            'onboard-onboard-tab'
        );
        expect(getByRole('heading', { level: 1 }).textContent).toBe(
            'ONBOARD CATALOG'
        );
    });
});
