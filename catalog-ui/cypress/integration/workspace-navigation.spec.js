import { initCommonFixtures } from "../common/init";

describe('Workspace Navigation and Auto-Save', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);
        cy.fixture('workspace-navigation/service-metadata-checkout').as('serviceCheckout');
    });

    it('Editing a field and navigating to another tab triggers auto-save', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceCheckout');
        cy.route('PUT', '**/catalog/services/*/metadata', '@serviceCheckout').as('saveService');
        cy.route('GET', '**/services/*/dependencies', '[]');
        cy.route('GET', '**/catalog/services/validate-name/*', '{"isValid": true}');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesRelations*', '{}');

        const url = '#!/dashboard/workspace/nav11111-1111-1111-1111-111111111111/service/general';
        cy.visit(url);

        cy.get('[data-tests-id="description"]').clear({ force: true });
        cy.get('[data-tests-id="description"]').type('Updated description', { force: true });
        cy.get('[data-tests-id="menulink-composition"]').click({ force: true });
        cy.wait('@saveService');
    });

    it('Invalid form prevents navigation to another tab', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceCheckout');
        cy.route('GET', '**/services/*/dependencies', '[]');
        cy.route('GET', '**/catalog/services/validate-name/*', '{"isValid": false}');

        const url = '#!/dashboard/workspace/nav11111-1111-1111-1111-111111111111/service/general';
        cy.visit(url);

        cy.get('[data-tests-id="name"]').clear({ force: true });
        cy.get('[data-tests-id="menulink-composition"]').click({ force: true });
        cy.url().should('contain', '/general');
    });

    it('No save triggered when form is unchanged and navigating', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceCheckout');
        cy.route('PUT', '**/catalog/services/*/metadata', '@serviceCheckout').as('saveService');
        cy.route('GET', '**/services/*/dependencies', '[]');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesRelations*', '{}');

        const url = '#!/dashboard/workspace/nav11111-1111-1111-1111-111111111111/service/general';
        cy.visit(url);

        cy.get('[data-tests-id="menulink-composition"]').click({ force: true });
        cy.get('@saveService').should('not.exist');
    });

    it('Save button updates metadata and clears dirty state', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceCheckout');
        cy.route('PUT', '**/catalog/services/*/metadata', '@serviceCheckout').as('saveService');
        cy.route('GET', '**/services/*/dependencies', '[]');
        cy.route('GET', '**/catalog/services/validate-name/*', '{"isValid": true}');

        const url = '#!/dashboard/workspace/nav11111-1111-1111-1111-111111111111/service/general';
        cy.visit(url);

        cy.get('[data-tests-id="description"]').clear({ force: true });
        cy.get('[data-tests-id="description"]').type('New description', { force: true });
        cy.get('[data-tests-id="create/save"]').click({ force: true });
        cy.wait('@saveService');
        cy.get('[data-tests-id="save-warning"]').should('not.be.visible');
    });

    it('Revert button restores original values', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceCheckout');
        cy.route('GET', '**/services/*/dependencies', '[]');
        cy.route('GET', '**/catalog/services/validate-name/*', '{"isValid": true}');

        const url = '#!/dashboard/workspace/nav11111-1111-1111-1111-111111111111/service/general';
        cy.visit(url);

        cy.get('[data-tests-id="description"]').clear({ force: true });
        cy.get('[data-tests-id="description"]').type('Modified text', { force: true });
        cy.get('[data-tests-id="revert"]').click({ force: true });
        cy.get('[data-tests-id="description"]').should('have.value', 'Navigation test service');
    });
});
