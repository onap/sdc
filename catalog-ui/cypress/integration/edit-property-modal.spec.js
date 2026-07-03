/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2026 Deutsche Telekom AG.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

import { initCommonFixtures } from "../common/init";

/**
 * Characterization tests for the edit-property modal (Phase 9 oracle).
 *
 * These tests assert the USER-VISIBLE contract of the current AngularJS
 * property-form modal: open → fill → save → reopen → assert value persisted.
 * They are FRAMEWORK-AGNOSTIC: all assertions use data-tests-id attributes from
 * the live templates; no AngularJS internals are touched.  The tests must stay
 * green after the Phase 9 Angular rewrite of the modal.
 *
 * Navigation: workspace properties tab  (#!/dashboard/workspace/{id}/service/properties)
 * All HTTP calls are fully mocked — no live backend required.
 *
 * Selector notes recorded during authoring (to verify / correct on first run):
 *   - Modal container:        [data-tests-id="sdc-edit-property-container"]  (on <ng1-modal>)
 *   - Save button:            [data-tests-id="Save"]
 *   - "Add Property" button:  [data-tests-id="addGrey"]
 *   - Type select:            [data-tests-id="propertyType"]
 *   - Entry schema select:    [data-tests-id="schema-type"]
 *   - Property name input:    [data-tests-id="propertyName"]
 *   - Map add-item button:    [data-tests-id="add-map-item"]
 *   - Map key input:          [data-tests-id^="mapKey"]   (prefix = currentPropertyIndex = -1 for new)
 *   - Map value input:        [data-tests-id^="mapValue"]
 *   - List add-item button:   [data-tests-id="add-list-item"]
 *   - List item input:        [data-tests-id^="listNewItem"]
 *   - Nested leaf input:      [data-tests-id="-1vlan_range_plan"]  (fieldsPrefixName+propertyName)
 *   - Datatype expand toggle: .open-close-button  (no data-tests-id; expand-by-default=true means auto-open)
 *
 * currentPropertyIndex is -1 for a brand-new property (not yet in filteredProperties).
 * The starts-with selectors above therefore match regardless of the exact prefix value.
 */

describe('Edit-property modal — value editing (characterization)', () => {
    beforeEach(function () {
        cy.server();
        initCommonFixtures(cy);

        cy.fixture('properties-assignment/onap-user-data').as('onapUserData');
        cy.fixture('service-proxy-tabs/metadata-service-with-service').as('metadata');
        cy.fixture('service-proxy-tabs/full-data-service-with-service').as('fullData');
        cy.fixture('properties-assignment/service-include-policies').as('serviceIncludePolicies');

        // edit-property-modal fixtures
        cy.fixture('edit-property-modal/empty-properties').as('emptyProperties');
        cy.fixture('edit-property-modal/save-map-response').as('saveMapResponse');
        cy.fixture('edit-property-modal/properties-after-map-save').as('propertiesAfterMapSave');
        cy.fixture('edit-property-modal/save-list-response').as('saveListResponse');
        cy.fixture('edit-property-modal/properties-after-list-save').as('propertiesAfterListSave');
        cy.fixture('edit-property-modal/save-nested-response').as('saveNestedResponse');
        cy.fixture('edit-property-modal/properties-after-nested-save').as('propertiesAfterNestedSave');
    });

    /**
     * MAP round-trip: Add a new map(string) property, fill one key/value entry,
     * save, then reopen the property row and verify the entry is still there.
     */
    it('edits a map property value, saves, and the value persists on reopen', function () {
        // --- route stubs for page load ---
        cy.route('GET', '**/authorize', '@onapUserData');
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=nonExcludedPolicies&include=nonExcludedGroups&include=forwardingPaths', '@fullData');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstances&include=policies&include=nonExcludedGroups', '@serviceIncludePolicies');
        // Initial properties load (empty — no rows yet)
        cy.route('GET', '**/services/*/filteredDataByParams?include=properties', '@emptyProperties');

        const propertiesPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/properties';
        cy.visit(propertiesPageUrl);

        // --- open Add Property modal ---
        cy.get('[data-tests-id="addGrey"]').trigger('click', { force: true });
        cy.get('[data-tests-id="sdc-edit-property-container"]').should('be.visible');

        // Fill name and select type = map with string entry schema
        cy.get('[data-tests-id="propertyName"]').clear().type('map_prop');
        cy.get('[data-tests-id="propertyType"]').select('map');
        cy.get('[data-tests-id="schema-type"]').select('string');

        // Add a map entry
        cy.get('[data-tests-id="add-map-item"]').click();
        cy.get('[data-tests-id^="mapKey"]').first().clear().type('region');
        cy.get('[data-tests-id^="mapValue"]').first().clear().type('eu');

        // Stub POST (save new property) and the subsequent reload
        cy.route('POST', '**/services/*/properties', '@saveMapResponse');
        cy.route('GET', '**/services/*/filteredDataByParams?include=properties', '@propertiesAfterMapSave');

        cy.get('[data-tests-id="Save"]').trigger('click', { force: true });

        // Modal should close; property row should appear
        cy.get('[data-tests-id="sdc-edit-property-container"]').should('not.exist');
        cy.get('[data-tests-id="propertyRow"]').should('have.length', 1);

        // --- reopen the property and verify value persisted ---
        cy.get('[data-tests-id^="propertyName_"]').first().trigger('click', { force: true });
        cy.get('[data-tests-id="sdc-edit-property-container"]').should('be.visible');
        cy.get('[data-tests-id^="mapKey"]').first().should('have.value', 'region');
        cy.get('[data-tests-id^="mapValue"]').first().should('have.value', 'eu');
    });

    /**
     * LIST round-trip: Add a new list(string) property, fill one item,
     * save, then reopen and verify the item persists.
     */
    it('edits a list property value, saves, and the value persists on reopen', function () {
        cy.route('GET', '**/authorize', '@onapUserData');
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=nonExcludedPolicies&include=nonExcludedGroups&include=forwardingPaths', '@fullData');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstances&include=policies&include=nonExcludedGroups', '@serviceIncludePolicies');
        cy.route('GET', '**/services/*/filteredDataByParams?include=properties', '@emptyProperties');

        const propertiesPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/properties';
        cy.visit(propertiesPageUrl);

        // --- open Add Property modal ---
        cy.get('[data-tests-id="addGrey"]').trigger('click', { force: true });
        cy.get('[data-tests-id="sdc-edit-property-container"]').should('be.visible');

        cy.get('[data-tests-id="propertyName"]').clear().type('list_prop');
        cy.get('[data-tests-id="propertyType"]').select('list');
        cy.get('[data-tests-id="schema-type"]').select('string');

        // Add a list item
        cy.get('[data-tests-id="add-list-item"]').click();
        cy.get('[data-tests-id^="listNewItem"]').first().clear().type('alpha');

        // Stub POST and reload
        cy.route('POST', '**/services/*/properties', '@saveListResponse');
        cy.route('GET', '**/services/*/filteredDataByParams?include=properties', '@propertiesAfterListSave');

        cy.get('[data-tests-id="Save"]').trigger('click', { force: true });

        cy.get('[data-tests-id="sdc-edit-property-container"]').should('not.exist');
        cy.get('[data-tests-id="propertyRow"]').should('have.length', 1);

        // --- reopen and verify ---
        cy.get('[data-tests-id^="propertyName_"]').first().trigger('click', { force: true });
        cy.get('[data-tests-id="sdc-edit-property-container"]').should('be.visible');
        cy.get('[data-tests-id^="listNewItem"]').first().should('have.value', 'alpha');
    });

    /**
     * NESTED-DATATYPE round-trip: Add a property of type VlanRequirements
     * (a custom datatype with sub-fields), fill the vlan_range_plan leaf field,
     * save, then reopen and verify the leaf value persists.
     *
     * The fields-structure directive renders with expand-by-default=true, so the
     * sub-fields are visible without a manual expand click.
     *
     * The leaf input data-tests-id is fieldsPrefixName+property.name.
     * For a new property currentPropertyIndex=-1, so the id is "-1vlan_range_plan".
     * On reopen the property is at index 0 in the refreshed list, so the id becomes "0vlan_range_plan".
     */
    it('edits a nested-datatype property value, saves, and the value persists on reopen', function () {
        cy.route('GET', '**/authorize', '@onapUserData');
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=nonExcludedPolicies&include=nonExcludedGroups&include=forwardingPaths', '@fullData');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstances&include=policies&include=nonExcludedGroups', '@serviceIncludePolicies');
        cy.route('GET', '**/services/*/filteredDataByParams?include=properties', '@emptyProperties');

        const propertiesPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/properties';
        cy.visit(propertiesPageUrl);

        // --- open Add Property modal ---
        cy.get('[data-tests-id="addGrey"]').trigger('click', { force: true });
        cy.get('[data-tests-id="sdc-edit-property-container"]').should('be.visible');

        cy.get('[data-tests-id="propertyName"]').clear().type('nested_prop');
        // Select a custom datatype from the non-primitive section of the type <select>
        // VlanRequirements is in nonPrimitiveTypes; its display name strips the heat prefix.
        cy.get('[data-tests-id="propertyType"]').select('org.openecomp.datatypes.network.VlanRequirements');

        // The fields-structure renders with expand-by-default=true — sub-fields appear immediately.
        // Fill the vlan_range_plan string leaf.
        // data-tests-id = fieldsPrefixName + property.name = "-1" + "vlan_range_plan" = "-1vlan_range_plan"
        cy.get('[data-tests-id="-1vlan_range_plan"]').should('be.visible').clear().type('plan-A');

        // Stub POST and reload
        cy.route('POST', '**/services/*/properties', '@saveNestedResponse');
        cy.route('GET', '**/services/*/filteredDataByParams?include=properties', '@propertiesAfterNestedSave');

        cy.get('[data-tests-id="Save"]').trigger('click', { force: true });

        cy.get('[data-tests-id="sdc-edit-property-container"]').should('not.exist');
        cy.get('[data-tests-id="propertyRow"]').should('have.length', 1);

        // --- reopen and verify ---
        // After reload the property is index 0, so fieldsPrefixName = 0 → "0vlan_range_plan"
        cy.get('[data-tests-id^="propertyName_"]').first().trigger('click', { force: true });
        cy.get('[data-tests-id="sdc-edit-property-container"]').should('be.visible');
        // The fields-structure expands by default; verify the leaf value
        cy.get('[data-tests-id="0vlan_range_plan"]').should('have.value', 'plan-A');
    });
});
