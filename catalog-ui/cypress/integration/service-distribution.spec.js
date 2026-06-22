import { initCommonFixtures } from "../common/init";

describe('Service Creation and Distribution', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);
        cy.fixture('service-distribution/service-checkout').as('serviceCheckout');
        cy.fixture('service-distribution/service-after-certify').as('serviceCertified');
        cy.fixture('service-distribution/service-after-distribute').as('serviceDistributed');
    });

    it('Create service form validates required fields', function () {
        cy.route('GET', '**/catalog/services/validate-name/*', '{"isValid": true}');

        const url = '#!/dashboard/workspace/service/general';
        cy.visit(url);

        cy.get('[data-tests-id="create/save"]').should('be.disabled');
        cy.get('[data-tests-id="name"]').type('NewTestService', { force: true });
        cy.get('[data-tests-id="description"]').type('A test service', { force: true });
    });

    it('Submitting create service calls POST and navigates to workspace', function () {
        cy.route('GET', '**/catalog/services/validate-name/*', '{"isValid": true}');
        cy.route('POST', '**/catalog/services', '@serviceCheckout').as('createService');
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceCheckout');
        cy.route('GET', '**/services/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/service/general';
        cy.visit(url);

        cy.get('[data-tests-id="name"]').type('TestService', { force: true });
        cy.get('[data-tests-id="description"]').type('Distribution test', { force: true });
        cy.get('[data-tests-id="selectGeneralCategory"]').select('Mobility', { force: true });
        cy.get('[data-tests-id="create/save"]').click({ force: true });
        cy.wait('@createService');
    });

    it('Certify on uncertified service triggers certification flow', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceCheckout');
        cy.route('POST', '**/services/*/lifecycleState/certify', '@serviceCertified').as('certifyService');
        cy.route('GET', '**/services/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/svc11111-1111-1111-1111-111111111111/service/general';
        cy.visit(url);

        cy.get('[data-tests-id="certify"]').should('be.visible');
        cy.get('[data-tests-id="certify"]').click({ force: true });
        cy.get('[data-tests-id="checkindialog"]').type('Certifying service');
        cy.get('[data-tests-id="confirm-modal-button-ok"]').click({ force: true });
        cy.wait('@certifyService');
    });

    it('Distribute on certified service triggers distribution activation', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceCertified');
        cy.route('POST', '**/services/*/distribution/PROD/activate', '@serviceDistributed').as('distributeService');
        cy.route('GET', '**/services/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/svc22222-2222-2222-2222-222222222222/service/general';
        cy.visit(url);

        cy.get('[data-tests-id="distribute"]').should('be.visible');
        cy.get('[data-tests-id="distribute"]').click({ force: true });
        cy.wait('@distributeService');
    });

    it('After distribution, Redistribute and Check Out are available', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceDistributed');
        cy.route('GET', '**/services/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/svc22222-2222-2222-2222-222222222222/service/general';
        cy.visit(url);

        cy.get('[data-tests-id="redistribute"]').should('be.visible');
        cy.get('[data-tests-id="check_out"]').should('be.visible');
        cy.get('[data-tests-id="distribute"]').should('not.exist');
    });
});
