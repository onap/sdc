import { initCommonFixtures } from "../common/init";

describe('Deployment Artifacts', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);

        cy.fixture('deployment-artifacts/metadata-service-with-vsp').as('metadata');
        cy.fixture('deployment-artifacts/metadata-service-checkedin-with-vsp').as('metadataCheckedInService');
        cy.fixture('deployment-artifacts/full-data-service-with-vsp').as('fullData');
        cy.fixture('deployment-artifacts/vsp-deployment-artifacts').as('vsp-deployment-artifacts');
        cy.fixture('deployment-artifacts/updated-vsp-deployment-artifacts').as('updated-vsp-deployment-artifacts');
        cy.fixture('deployment-artifacts/heat-env-post-result').as('heat-env-result');
        cy.fixture('deployment-artifacts/vsp.json').as('vsp');
        cy.fixture('deployment-artifacts/service-deployment-artifacts.json').as('service-deployment-artifacts');

        cy.route('GET', '**/services/*/filteredDataByParams?include=deploymentArtifacts', '@service-deployment-artifacts');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=nonExcludedPolicies&include=nonExcludedGroups&include=forwardingPaths', '@fullData');
        cy.route('GET', '**/services/*/resourceInstances/*/artifactsByType/DEPLOYMENT', '@vsp-deployment-artifacts');
        cy.route('GET', '**/resources/*', '@vsp');
        cy.route('POST', '**/services/*/resourceInstance/*/artifacts/*', '@heat-env-result');
    });

    it('Test the timeout update in composition deployment artifacts tab', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');

        const compositionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/composition/details';
        cy.visit(compositionPageUrl);

        console.log('Wait for the canvas to be visible');
        cy.get('canvas-search').should('be.visible');

        console.log('Click on the component instance via the testBridge');
        cy.window().its('testBridge').invoke('selectComponentInstance', ['VSP1']);

        console.log('Click on Deployment Artifacts Tab');
        cy.get('.component-details-panel-tabs sdc-tabs ul').children('li[ng-reflect-text="Deployment Artifacts"]').trigger('click', { force: true });

        console.log('Click on edit artifact - base_ldsa (modal should be opened');
        cy.get('[data-tests-id="edit-parameters-of-base_ldsa"]').click({ force: true }); // edit icon is not visible until we hover on it so we force to click here

        console.log('Make sure timeout field is visible');
        cy.get('[data-tests-id="deploymentTimeout"]').should('be.visible').should('have.value', '2');

        console.log('Update the route to return the new updated artifact');
        cy.route('GET', '**/services/*/resourceInstances/*/artifactsByType/DEPLOYMENT', '@updated-vsp-deployment-artifacts');

        console.log('Click 3 as input changing the input value from 2 to 23. Then click Save. Modal should be closed');
        cy.get('[data-tests-id="deploymentTimeout"]').type('3');
        cy.get('[data-tests-id="envParams-button-save"]').click();
        cy.wait(1000);
        console.log('Click edit again to open modal and then make sure the updated value is in the field.');
        cy.get('[data-tests-id="artifactName-base_ldsa"]').should('be.visible'); // Wait for modal to be be closed (wait for artifact label to be visible
        cy.get('[data-tests-id="edit-parameters-of-base_ldsa"]').click({ force: true }); // edit icon is not visible until we hover on it so we force to click here
        cy.get('[data-tests-id="deploymentTimeout"]').should('be.visible').should('have.value', '23'); // Check value

        console.log('Make sure artifact timeout values (min and max) are set according to configuration that was accepted from server (setup-ui.json fixture)');
        cy.get('[data-tests-id="deploymentTimeout"]').should('have.attr', 'max', '150')
        cy.get('[data-tests-id="deploymentTimeout"]').should('have.attr', 'min', '2')
    });

    it('Test that readonly modal is opened in case service is in Checkin state', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadataCheckedInService');

        const compositionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/composition/details';
        cy.visit(compositionPageUrl);

        console.log('Wait for the canvas to be visible');
        cy.get('canvas-search').should('be.visible');

        console.log('Click on the component instance via the testBridge');
        cy.window().its('testBridge').invoke('selectComponentInstance', ['VSP1']);

        console.log('Click on Deployment Artifacts Tab');
        cy.get('.component-details-panel-tabs sdc-tabs ul').children('li[ng-reflect-text="Deployment Artifacts"]').trigger('click', { force: true });

        console.log('Click on edit artifact - base_ldsa (modal should be opened');
        cy.get('[data-tests-id="view-parameters-of-base_ldsa"]').click({ force: true }); // edit icon is not visible until we hover on it so we force to click here

        cy.get('[data-tests-id="deploymentTimeout"]').should('be.disabled').should('have.value', '2');
        cy.get('[data-tests-id="value-field-of-vnf_name"]').should('be.disabled');

        cy.get('[data-tests-id="envParams-button-save"]').should('not.be.visible');
    });

});