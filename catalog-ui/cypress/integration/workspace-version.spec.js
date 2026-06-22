import { initCommonFixtures } from "../common/init";

describe('Workspace Version Switching', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);
        cy.fixture('workspace-version/vf-metadata-v1').as('vfV1');
        cy.fixture('workspace-version/vf-metadata-v2-checkout').as('vfV2');
    });

    it('Version dropdown shows all available versions', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfV2');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/ver22222-2222-2222-2222-222222222222/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="versionHeader"]').should('be.visible');
        cy.get('[data-tests-id="versionHeader"]').should('contain', '2.0');
    });

    it('Selecting an older version reloads in view-only mode', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfV1');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/ver11111-1111-1111-1111-111111111111/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="check_out"]').should('be.visible');
        cy.get('[data-tests-id="check_in"]').should('not.exist');
        cy.get('[data-tests-id="certify"]').should('not.exist');
    });

    it('View-only mode disables form fields for non-latest version', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfV1');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/ver11111-1111-1111-1111-111111111111/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="name"]').should('have.attr', 'disabled');
    });

    it('Latest version link appears for older versions', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfV1');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/ver11111-1111-1111-1111-111111111111/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="latest-version"]').should('be.visible');
    });

    it('Latest version is shown as the current version for latest', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfV2');
        cy.route('GET', '**/resources/*/dependencies', '[]');

        const url = '#!/dashboard/workspace/ver22222-2222-2222-2222-222222222222/resource/general';
        cy.visit(url);

        cy.get('[data-tests-id="latest-version"]').should('not.exist');
    });
});
