import {initCommonFixtures} from "../common/init";

describe('General Screen', () => {

    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);

        // Service Metadata Fixture for General Page
        cy.fixture('general-page/service-metadata').as('service-metadata');
        cy.route('GET', '**/catalog/services/*/filteredDataByParams?include=metadata', '@service-metadata');
        cy.route({ method: 'PUT', url: '**/catalog/services/*/metadata'}, '@service-metadata-update-response').as('updateService');
    });

    it('Test that metadata fields are populated', function () {
        const generalPageUrl = '#!/dashboard/workspace/035ca9eb-ab9a-4e03-9bfb-e051a6c47a82/service/general';
        cy.visit(generalPageUrl);

        cy.get('[data-tests-id="serviceFunction"]').should('be.visible');
        cy.get('[data-tests-id="serviceFunction"]').should('have.value', 'serviceFunction1');
        cy.get('[data-tests-id="create/save"]').click();

        // Assert that the request body includes the serviceFunction parameter
        cy.wait('@updateService').then((xhr) => {
            assert.equal(xhr.request.body.serviceFunction, 'serviceFunction1');
        });
    });
});