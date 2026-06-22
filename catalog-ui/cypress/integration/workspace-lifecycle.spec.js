import { initCommonFixtures } from "../common/init";

describe('Workspace Lifecycle State Transitions', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);
        cy.fixture('workspace-lifecycle/vf-metadata-checkout').as('vfCheckout');
        cy.fixture('workspace-lifecycle/vf-after-checkin').as('vfCheckin');
        cy.fixture('workspace-lifecycle/vf-after-certify').as('vfCertified');
        cy.fixture('workspace-lifecycle/checkin-response').as('checkinResponse');
        cy.fixture('workspace-lifecycle/certify-response').as('certifyResponse');
    });

    it('Check In opens comment dialog and submits CHECKIN', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfCheckout');
        cy.route('POST', '**/resources/*/lifecycleState/CHECKIN', '@checkinResponse').as('checkinCall');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/aaa11111-1111-1111-1111-111111111111/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="check_in"]').should('be.visible');
        cy.get('[data-tests-id="check_in"]').click({ force: true });
        cy.get('[data-tests-id="checkindialog"]').should('be.visible');
        cy.get('[data-tests-id="checkindialog"]').type('Checking in for test');
        cy.get('[data-tests-id="confirm-modal-button-ok"]').click({ force: true });
        cy.wait('@checkinCall');
    });

    it('Check Out on CHECKIN state transitions to CHECKOUT', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfCheckin');
        cy.route('POST', '**/resources/*/lifecycleState/CHECKOUT', '@vfCheckout').as('checkoutCall');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/aaa11111-1111-1111-1111-111111111111/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="check_out"]').should('be.visible');
        cy.get('[data-tests-id="check_out"]').click({ force: true });
        cy.wait('@checkoutCall');
    });

    it('Certify opens comment dialog and calls certify API', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfCheckout');
        cy.route('POST', '**/resources/*/lifecycleState/certify', '@certifyResponse').as('certifyCall');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/aaa11111-1111-1111-1111-111111111111/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="certify"]').should('be.visible');
        cy.get('[data-tests-id="certify"]').click({ force: true });
        cy.get('[data-tests-id="checkindialog"]').should('be.visible');
        cy.get('[data-tests-id="checkindialog"]').type('Certifying component');
        cy.get('[data-tests-id="confirm-modal-button-ok"]').click({ force: true });
        cy.wait('@certifyCall');
    });

    it('Delete version calls UNDOCHECKOUT', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfCheckout');
        cy.route('POST', '**/resources/*/lifecycleState/UNDOCHECKOUT', '@vfCheckin').as('undoCheckoutCall');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/aaa11111-1111-1111-1111-111111111111/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="delete_version"]').should('be.visible');
        cy.get('[data-tests-id="delete_version"]').click({ force: true });
        cy.get('[data-tests-id="confirm-modal-button-ok"]').click({ force: true });
        cy.wait('@undoCheckoutCall');
    });

    it('After certification, Check Out button is visible and Certify is not', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfCertified');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/bbb22222-2222-2222-2222-222222222222/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="check_out"]').should('be.visible');
        cy.get('[data-tests-id="certify"]').should('not.exist');
    });

    it('Certify from CHECKIN state works directly', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfCheckin');
        cy.route('POST', '**/resources/*/lifecycleState/certify', '@certifyResponse').as('certifyCall');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/aaa11111-1111-1111-1111-111111111111/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="certify"]').should('be.visible');
        cy.get('[data-tests-id="certify"]').click({ force: true });
        cy.get('[data-tests-id="checkindialog"]').should('be.visible');
        cy.get('[data-tests-id="checkindialog"]').type('Certify from checkin');
        cy.get('[data-tests-id="confirm-modal-button-ok"]').click({ force: true });
        cy.wait('@certifyCall');
    });
});
