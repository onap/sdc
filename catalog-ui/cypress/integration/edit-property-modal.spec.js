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
 * These tests assert the USER-VISIBLE contract of the edit-property modal:
 * open → fill → save → reopen → assert value persisted.
 * They are FRAMEWORK-AGNOSTIC: all assertions use data-tests-id attributes from
 * the live templates; no framework internals are touched.  The tests stayed the
 * oracle across the Phase 9 Angular rewrite of the modal.
 *
 * Navigation: workspace properties tab  (#!/dashboard/workspace/{id}/service/properties)
 * All HTTP calls are fully mocked — no live backend required.
 *
 * Updated 2026-07-07 (Phase 9 CR 2): the editable modal is now the Angular
 * PropertyFormModalComponent and its recursive value editor is the Angular
 * <dynamic-property> component.  The recursive-editor data-tests-id scheme
 * therefore CHANGED from the old AngularJS type-map / type-list /
 * data-type-fields-structure directives.  The scalar/container ids are preserved.
 *
 * Selector notes (verified against the live Angular templates):
 *   Scalar / container ids — PropertyFormModalComponent
 *   (src/app/ng2/pages/property-form-modal/property-form-modal.component.html):
 *   - Modal container:        [data-tests-id="sdc-edit-property-container"]
 *   - Save button:            [data-tests-id="Save"]
 *   - "Add Property" button:  [data-tests-id="addGrey"]   (properties table, not the modal)
 *   - Type select:            [data-tests-id="propertyType"]
 *   - Entry schema select:    [data-tests-id="schema-type"]
 *   - Property name input:    [data-tests-id="propertyName"]
 *
 *   Recursive value-editor ids — <dynamic-property>
 *   (src/app/ng2/components/logic/properties-table/dynamic-property/dynamic-property.component.html).
 *   These are PATH-BASED: the id suffix is `propertyTestsId`, a dotted path built
 *   by getPropertyTestsId() = [rootProperty.name].concat(parentNames).join('.')
 *   (e.g. "map_prop", "map_prop.0", "nested_prop.vlan_range_plan").  There is NO
 *   old numeric {fieldsPrefixName}{index} prefix any more.
 *   - Add list/map item:      [data-tests-id^="add-to-list-"]   (ONE id for both list AND map add buttons)
 *   - Delete list/map item:   [data-tests-id^="delete-from-list-"]
 *   - Expand child toggle:    [data-tests-id^="expand-"]
 *   The leaf key/value <input>s are rendered by ui-element-* form components,
 *   which PREPEND "value-" to the dynamic-property testId. So the id in the DOM is:
 *   - Map key input:          [data-tests-id^="value-prop-key-"]   (testId 'prop-key-' + path)
 *   - Leaf value input:       [data-tests-id^="value-prop-"]       (testId 'prop-' + path)
 *   Collision: "value-prop-key-*" also starts with "value-prop-". To target the
 *   VALUE specifically, exclude the key:
 *     [data-tests-id^="value-prop-"]:not([data-tests-id^="value-prop-key-"])
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

        // Add a map entry (dynamic-property uses one "add-to-list-" id for both list and map)
        cy.get('[data-tests-id^="add-to-list-"]').click();
        cy.get('[data-tests-id^="value-prop-key-"]').first().clear().type('region');
        // Value input excludes the key input (both start with "value-prop-")
        cy.get('[data-tests-id^="value-prop-"]').not('[data-tests-id^="value-prop-key-"]').first().clear().type('eu');

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
        cy.get('[data-tests-id^="value-prop-key-"]').first().should('have.value', 'region');
        cy.get('[data-tests-id^="value-prop-"]').not('[data-tests-id^="value-prop-key-"]').first().should('have.value', 'eu');
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

        // Add a list item (same "add-to-list-" id as the map add button)
        cy.get('[data-tests-id^="add-to-list-"]').click();
        cy.get('[data-tests-id^="value-prop-"]').first().clear().type('alpha');

        // Stub POST and reload
        cy.route('POST', '**/services/*/properties', '@saveListResponse');
        cy.route('GET', '**/services/*/filteredDataByParams?include=properties', '@propertiesAfterListSave');

        cy.get('[data-tests-id="Save"]').trigger('click', { force: true });

        cy.get('[data-tests-id="sdc-edit-property-container"]').should('not.exist');
        cy.get('[data-tests-id="propertyRow"]').should('have.length', 1);

        // --- reopen and verify ---
        cy.get('[data-tests-id^="propertyName_"]').first().trigger('click', { force: true });
        cy.get('[data-tests-id="sdc-edit-property-container"]').should('be.visible');
        cy.get('[data-tests-id^="value-prop-"]').first().should('have.value', 'alpha');
    });

    /**
     * NESTED-DATATYPE round-trip: Add a property of type VlanRequirements
     * (a custom datatype with sub-fields), fill the vlan_range_plan leaf field,
     * save, then reopen and verify the leaf value persists.
     *
     * <dynamic-property> flattens the datatype's sub-fields; the leaf inputs are
     * visible without a manual expand click.
     *
     * The leaf value input data-tests-id is path-based:
     *   testId = 'prop-' + propertyTestsId, propertyTestsId = "nested_prop.vlan_range_plan"
     * and the ui-element-input prepends "value-", giving the DOM id
     *   "value-prop-nested_prop.vlan_range_plan".
     * We match by the property-name suffix (ends-with "vlan_range_plan"), which is
     * stable across add and reopen — the path no longer carries a numeric prefix.
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

        // dynamic-property flattens the datatype — the vlan_range_plan leaf appears immediately.
        // Fill the vlan_range_plan string leaf; DOM id is "value-prop-nested_prop.vlan_range_plan".
        cy.get('[data-tests-id^="value-prop-"][data-tests-id$="vlan_range_plan"]').should('be.visible').clear().type('plan-A');

        // Stub POST and reload
        cy.route('POST', '**/services/*/properties', '@saveNestedResponse');
        cy.route('GET', '**/services/*/filteredDataByParams?include=properties', '@propertiesAfterNestedSave');

        cy.get('[data-tests-id="Save"]').trigger('click', { force: true });

        cy.get('[data-tests-id="sdc-edit-property-container"]').should('not.exist');
        cy.get('[data-tests-id="propertyRow"]').should('have.length', 1);

        // --- reopen and verify ---
        // The path-based id is stable across reopen (no numeric prefix): "value-prop-nested_prop.vlan_range_plan"
        cy.get('[data-tests-id^="propertyName_"]').first().trigger('click', { force: true });
        cy.get('[data-tests-id="sdc-edit-property-container"]').should('be.visible');
        // dynamic-property flattens the datatype; verify the leaf value
        cy.get('[data-tests-id^="value-prop-"][data-tests-id$="vlan_range_plan"]').should('have.value', 'plan-A');
    });
});
