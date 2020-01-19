import {initCommonFixtures} from "../common/init";

describe('Home (Dashboard) Screen', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);

        // Followed Resources for the dashboard screen
        cy.fixture('home/followed').as('followed');
        cy.route('GET', '**/followed', '@followed');
    });

    it('Test that only Certify and Distributed appear in followed projects filter', function () {
        const homePageUrl = '#!/dashboard';
        cy.visit(homePageUrl);

        // Test that tiles are sorted
        cy.get('ui-tile .multiline-ellipsis-content').
            first().should('include.text', 'MyVSP1').
            last().should('include.text', 'MyVSP1');

        // Test that there are 5 components tiles
        cy.get('ui-tile .multiline-ellipsis-content').should('have.length', 5);


        cy.get('[data-tests-id="filter-CERTIFIED"]').should('be.visible');
        cy.get('[data-tests-id="filter-DISTRIBUTED"]').should('be.visible'); // Ready for Testing
        cy.get('[data-tests-id="count-DISTRIBUTED"]').should('have.text', '1');
        cy.get('[data-tests-id="filter-CERTIFICATION_IN_PROGRESS"]').should('not.be.visible');   // In Testing
        cy.get('[data-tests-id="filter-READY_FOR_CERTIFICATION"]').should('not.be.visible'); // Ready for Testing
    });
});