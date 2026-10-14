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
import CatalogList from 'sdc-app/onboarding/onboard/CatalogList.jsx';
import { mapStateToProps } from 'sdc-app/onboarding/onboard/Onboard.js';
import { defaultStoreFactory } from 'test-utils/factories/onboard/OnboardingCatalogFactories.js';
import { FinalizedLicenseModelFactory } from 'test-utils/factories/licenseModel/LicenseModelFactories.js';

const HINT = 'Create and submit a VLM first';

const renderTiles = (canAddVSP, onAddVSP = jest.fn()) => {
    const { container } = render(
        <CatalogList
            onAddVLM={jest.fn()}
            onAddVSP={onAddVSP}
            canAddVSP={canAddVSP}
        />
    );
    return {
        vspTile: container.querySelector(
            '[data-test-id="catalog-add-new-vsp"]'
        ),
        onAddVSP
    };
};

describe('CatalogList - CREATE NEW VSP tile', () => {
    it('can add a VSP only once a finalized VLM exists', () => {
        expect(mapStateToProps(defaultStoreFactory.build()).canAddVSP).toBe(
            false
        );
        const finalizedLicenseModelList = FinalizedLicenseModelFactory.buildList(
            1
        );
        expect(
            mapStateToProps(
                defaultStoreFactory.build({ finalizedLicenseModelList })
            ).canAddVSP
        ).toBe(true);
    });

    it('opens the dialog on click, Enter and Space when a vendor exists', () => {
        const { vspTile, onAddVSP } = renderTiles(true);
        expect(vspTile.getAttribute('role')).toBe('button');
        expect(vspTile.getAttribute('tabindex')).toBe('0');
        expect(vspTile.hasAttribute('aria-disabled')).toBe(false);
        expect(vspTile.hasAttribute('aria-describedby')).toBe(false);

        fireEvent.click(vspTile);
        fireEvent.keyDown(vspTile, { key: 'Enter' });
        fireEvent.keyDown(vspTile, { key: ' ' });
        expect(onAddVSP).toHaveBeenCalledTimes(3);
    });

    it('stays focusable but does not open the dialog without a vendor', () => {
        const { vspTile, onAddVSP } = renderTiles(false);
        expect(vspTile.getAttribute('aria-disabled')).toBe('true');
        expect(vspTile.getAttribute('tabindex')).toBe('0');
        expect(vspTile.classList.contains('disabled')).toBe(true);

        fireEvent.click(vspTile);
        fireEvent.keyDown(vspTile, { key: 'Enter' });
        fireEvent.keyDown(vspTile, { key: ' ' });
        expect(onAddVSP).not.toHaveBeenCalled();
    });

    it('describes the reason without adding it to the name', () => {
        const { vspTile } = renderTiles(false);
        const hint = document.getElementById(
            vspTile.getAttribute('aria-describedby')
        );
        expect(hint.textContent).toBe(HINT);
        expect(hint.getAttribute('aria-hidden')).toBe('true');
        expect(vspTile.contains(hint)).toBe(true);
    });

    it('shows the hint on hover, focus and tap, and hides it on Escape', () => {
        const { vspTile } = renderTiles(false);
        const hint = () =>
            vspTile
                .querySelector('.create-item-hint')
                .classList.contains('visible');
        expect(hint()).toBe(false);

        fireEvent.mouseEnter(vspTile);
        expect(hint()).toBe(true);
        fireEvent.mouseLeave(vspTile);
        expect(hint()).toBe(false);

        fireEvent.focus(vspTile);
        expect(hint()).toBe(true);
        fireEvent.keyDown(vspTile, { key: 'Escape' });
        expect(hint()).toBe(false);
        fireEvent.blur(vspTile);

        fireEvent.click(vspTile);
        expect(hint()).toBe(true);
    });
});
