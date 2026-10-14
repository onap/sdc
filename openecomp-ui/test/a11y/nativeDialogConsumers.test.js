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
import { render, fireEvent, act } from '@testing-library/react';
import { installDialogShim } from 'nfvo-utils/dialogShim.js';
import { GlobalModalView } from 'nfvo-components/modal/GlobalModal.js';
import { Notifications } from 'nfvo-components/notification/Notifications.js';
import VendorItem from 'sdc-app/onboarding/onboard/onboardingCatalog/VendorItem.jsx';
import CatalogItemDetails from 'sdc-app/onboarding/onboard/CatalogItemDetails.jsx';
import VSPOverlay from 'sdc-app/onboarding/onboard/onboardingCatalog/VSPOverlay.jsx';
import { catalogItemTypes } from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogConstants.js';

const noop = () => {};
const flushObservers = () => act(() => Promise.resolve());

describe('dialog shim', () => {
    it('gives dialogs open, show, showModal and close where the API is missing', () => {
        const doc = document.implementation.createHTMLDocument('');
        const nativeCreate = doc.createElement;
        doc.createElement = function(tag) {
            const el = nativeCreate.call(this, tag);
            delete el.showModal;
            return el;
        };
        // jsdom implements no showModal, like Firefox before 98.
        expect(typeof doc.createElement('dialog').showModal).toBe('undefined');
        expect(installDialogShim(doc)).toBe(true);
        const dialog = doc.createElement('dialog');
        const onClose = jest.fn();
        dialog.addEventListener('close', onClose);
        dialog.showModal();
        expect(dialog.open).toBe(true);
        dialog.close();
        expect(dialog.open).toBe(false);
        expect(onClose).toHaveBeenCalledTimes(1);
        expect(typeof doc.createElement('div').showModal).toBe('undefined');
    });

    it('leaves a native dialog implementation alone', () => {
        const doc = document.implementation.createHTMLDocument('');
        const nativeCreate = doc.createElement;
        doc.createElement = function(tag) {
            const el = nativeCreate.call(this, tag);
            if (tag === 'dialog') {
                el.showModal = noop;
            }
            return el;
        };
        expect(installDialogShim(doc)).toBe(false);
    });
});

describe('GlobalModal', () => {
    it('opens a dialog with a named close button and closes on Escape once', () => {
        const onClose = jest.fn();
        render(
            <GlobalModalView
                show
                type="custom"
                title="New License Model"
                msg="body"
                onClose={onClose}
            />
        );
        const dialog = document.querySelector('dialog.sdc-modal');
        expect(dialog.open).toBe(true);
        expect(dialog.getAttribute('aria-labelledby')).toBeTruthy();
        const close = dialog.querySelector('button.sdc-modal__close-button');
        expect(close.getAttribute('aria-label')).toBe('Close');
        fireEvent.click(close);
        expect(onClose).toHaveBeenCalledTimes(1);
        fireEvent.keyDown(document, { key: 'Escape' });
        expect(onClose).toHaveBeenCalledTimes(2);
    });
});

describe('Notifications', () => {
    it('render inside an open modal dialog and back in the body once it closes', async () => {
        const notifications = [
            { id: 'n1', type: 'success', title: 'Done', message: 'Archived' }
        ];
        render(<Notifications notifications={notifications} onClick={noop} />);
        const container = () =>
            document.querySelector('.onboarding-notifications-container');
        expect(container().closest('dialog')).toBeNull();

        const dialog = document.createElement('dialog');
        dialog.className = 'sdc-modal';
        document.body.appendChild(dialog);
        dialog.showModal();
        await flushObservers();
        expect(container().parentElement).toBe(dialog);
        expect(container().classList.contains('in-dialog')).toBe(true);

        dialog.close();
        await flushObservers();
        expect(container().closest('dialog')).toBeNull();
        expect(container().classList.contains('in-dialog')).toBe(false);
        dialog.remove();
    });
});

describe('Onboarding catalog tiles', () => {
    const noNestedControls = container =>
        expect(
            container.querySelectorAll(
                'button button, button a, a button, [role=button] button'
            ).length
        ).toBe(0);

    it('VendorItem: header button named by the vendor, name click opens it too', () => {
        const onVendorSelect = jest.fn();
        const { container, getByRole } = render(
            <VendorItem
                vendor={{ name: 'Vendor A', softwareProductList: [] }}
                onVendorSelect={onVendorSelect}
                onAddVSP={noop}
                onVSPButtonClick={noop}
            />
        );
        noNestedControls(container);
        fireEvent.click(getByRole('button', { name: 'Vendor A' }));
        fireEvent.click(
            container.querySelector('[data-test-id="catalog-item-name"]')
        );
        expect(onVendorSelect).toHaveBeenCalledTimes(2);
        expect(getByRole('button', { name: 'Create new VSP' })).toBeTruthy();
    });

    it('CatalogItemDetails: header button carries the type and the item name', () => {
        const onSelect = jest.fn();
        const { container, getByRole } = render(
            <CatalogItemDetails
                catalogItemData={{
                    name: 'MyVsp',
                    vendorName: 'Vendor A',
                    owner: 'cs0008'
                }}
                catalogItemTypeClass={catalogItemTypes.SOFTWARE_PRODUCT}
                onSelect={onSelect}
                onMigrate={noop}
            />
        );
        noNestedControls(container);
        fireEvent.click(getByRole('button', { name: 'VSP MyVsp' }));
        fireEvent.click(
            container.querySelector('[data-test-id="catalog-item-name"]')
        );
        expect(onSelect).toHaveBeenCalledTimes(2);
    });

    it('VSPOverlay: rows and See More are buttons', () => {
        const onSelectVSP = jest.fn();
        const onSeeMore = jest.fn();
        const list = [1, 2, 3, 4, 5, 6].map(i => ({
            id: 'v' + i,
            name: 'vsp' + i
        }));
        const { getByRole } = render(
            <VSPOverlay
                VSPList={list}
                onSelectVSP={onSelectVSP}
                onSeeMore={onSeeMore}
                onMigrate={noop}
            />
        );
        fireEvent.click(getByRole('button', { name: 'vsp1' }));
        expect(onSelectVSP).toHaveBeenCalledWith(list[0]);
        fireEvent.click(getByRole('button', { name: 'See More' }));
        expect(onSeeMore).toHaveBeenCalledTimes(1);
    });
});
