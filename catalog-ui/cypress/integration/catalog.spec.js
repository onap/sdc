import {initCommonFixtures} from "../common/init";

describe('Catalog Screen', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);

        // Followed Resources for the dashboard screen
        cy.fixture('catalog/catalog').as('catalog');
        cy.route('GET', '**/catalog?*', '@catalog');
    });

    it('Test that only In Design/Distributed/Certify appears in catalog left panel filters', function () {
        const catalogPageUrl = '#!/catalog?*';
        cy.visit(catalogPageUrl);

        cy.get('[data-tests-id="checklist-status-in-design"]').should('exist');
        cy.get('[data-tests-id="checklist-status-certified"]').should('exist');
        cy.get('[data-tests-id="checklist-status-distributed"]').should('exist');
    });
});