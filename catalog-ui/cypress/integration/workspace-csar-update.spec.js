import { initCommonFixtures } from "../common/init";

describe('Workspace CSAR Update Flow', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);
        cy.fixture('update-vsp/packages-first').as('packagesFirst');
        cy.fixture('update-vsp/vsp-first').as('vspFirst');
    });

    it('Browse button opens onboarding modal showing available CSARs', function () {
        cy.route('GET', '**/packages', '@packagesFirst');
        cy.route('GET', '**/resources/csar/*', '@vspFirst');
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', 'fixture:update-vsp/metadata-after-checkout');

        const url = '#!/dashboard/workspace/09f56471-cb97-49f9-af25-44eaa1af1f05/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="browseButton"]').should('be.visible');
        cy.get('[data-tests-id="browseButton"]').click({ force: true });
        cy.get('[data-tests-id="csar-row"]').should('be.visible');
    });

    it('Selecting new CSAR version updates the filename display', function () {
        cy.route('GET', '**/packages', 'fixture:update-vsp/packages-second');
        cy.route('GET', '**/resources/csar/*', 'fixture:update-vsp/vsp-second');
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', 'fixture:update-vsp/metadata-after-checkout');
        cy.route('PUT', '**/resources/*', 'fixture:update-vsp/save-vsp');

        const url = '#!/dashboard/workspace/09f56471-cb97-49f9-af25-44eaa1af1f05/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="browseButton"]').click({ force: true });
        cy.get('[data-tests-id="csar-row"]').should('be.visible');
        cy.get('[data-tests-id="csar-row"]').last().click({ force: true });
        cy.get('[data-tests-id="update-csar"]').click({ force: true });
        cy.get('[data-tests-id="filename"]').should('not.contain', '(1.0)');
    });

    it('CSAR update on CHECKOUT state auto-saves without extra checkout', function () {
        cy.route('GET', '**/packages', 'fixture:update-vsp/packages-second');
        cy.route('GET', '**/resources/csar/*', 'fixture:update-vsp/vsp-second');
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', 'fixture:update-vsp/metadata-after-checkout');
        cy.route('PUT', '**/resources/*', 'fixture:update-vsp/save-vsp').as('saveResource');
        cy.route('POST', '**/resources/*/lifecycleState/CHECKOUT', '').as('checkoutCall');

        const url = '#!/dashboard/workspace/09f56471-cb97-49f9-af25-44eaa1af1f05/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="browseButton"]').click({ force: true });
        cy.get('[data-tests-id="csar-row"]').last().click({ force: true });
        cy.get('[data-tests-id="update-csar"]').click({ force: true });

        cy.get('[data-tests-id="check_in"]').should('be.visible');
        cy.get('[data-tests-id="save-warning"]').should('not.be.visible');
    });

    it('After CSAR update, refreshing page shows the new version persisted', function () {
        cy.route('GET', '**/packages', 'fixture:update-vsp/packages-second');
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', 'fixture:update-vsp/metadata-second');

        const url = '#!/dashboard/workspace/09f56471-cb97-49f9-af25-44eaa1af1f05/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="filename"]').contains('test update vsp (2.0)');
    });
});
