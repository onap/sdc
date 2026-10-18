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
import { render } from '@testing-library/react';
import CatalogList from 'sdc-app/onboarding/onboard/CatalogList.jsx';
import CatalogItemDetails from 'sdc-app/onboarding/onboard/CatalogItemDetails.jsx';
import VendorItem from 'sdc-app/onboarding/onboard/onboardingCatalog/VendorItem.jsx';
import { EntityType } from 'sdc-app/onboarding/onboard/filter/FilterComponents.jsx';
import { catalogItemTypes } from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogConstants.js';

describe('onboarding catalog spells out VSP and VLM', () => {
    it('in the entity type filter, keeping the test ids', () => {
        const { container } = render(
            <EntityType data={{ itemType: {} }} onDataChanged={jest.fn()} />
        );
        expect(
            container
                .querySelector('[data-test-id="catalog-filter-type-vsp"]')
                .closest('div').textContent
        ).toContain('Vendor Software Product (VSP)');
        expect(container.textContent).toContain('Vendor License Model (VLM)');
    });

    it('on the create tiles', () => {
        const { container } = render(
            <CatalogList onAddVLM={jest.fn()} onAddVSP={jest.fn()} canAddVSP />
        );
        expect(
            container.querySelector('[data-test-id="catalog-add-new-vsp"]')
                .textContent
        ).toContain('CREATE NEW VENDOR SOFTWARE PRODUCT');
        expect(
            container.querySelector('[data-test-id="catalog-add-new-vlm"]')
                .textContent
        ).toContain('CREATE NEW VENDOR LICENSE MODEL');
    });

    it('on the tile badges', () => {
        const { container } = render(
            <CatalogItemDetails
                catalogItemData={{ name: 'n' }}
                catalogItemTypeClass={catalogItemTypes.SOFTWARE_PRODUCT}
                onSelect={jest.fn()}
                onMigrate={jest.fn()}
            />
        );
        expect(container.querySelector('.sdc-tile-header').textContent).toBe(
            'Vendor Software Product'
        );
    });

    it('describes the Create new VSP button on a vendor tile without renaming it', () => {
        const { getByRole } = render(
            <VendorItem
                vendor={{ name: 'Vendor A', softwareProductList: [] }}
                onVendorSelect={jest.fn()}
                onAddVSP={jest.fn()}
                onVSPButtonClick={jest.fn()}
            />
        );
        expect(
            getByRole('button', { name: 'Create new VSP' }).getAttribute(
                'aria-describedby'
            )
        ).toBe('sdc-onboarding-glossary-VSP');
    });
});
