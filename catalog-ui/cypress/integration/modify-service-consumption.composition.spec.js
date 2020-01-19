import { initCommonFixtures } from "../common/init";

describe('Test modify service consumption in service consumption tab', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);
        cy.fixture('properties-assignment/onap-user-data').as('onapUserData');

        cy.fixture('service-proxy-tabs/metadata-service-with-service').as('metadata');
        cy.fixture('service-proxy-tabs/full-data-service-with-service').as('fullData');
        cy.fixture('service-proxy-tabs/service-inputs').as('serviceInputs');
        cy.fixture('service-proxy-tabs/update-operation-input').as('updateOperationInput');
        cy.fixture('service-proxy-tabs/service-input-update-value').as('inputsUpdateValue');
    });

    it('Update property', function () {
        cy.route('GET', '**/authorize', '@onapUserData');
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=nonExcludedPolicies&include=nonExcludedGroups&include=forwardingPaths', '@fullData');
        cy.route('GET', '**/services/*', 'fixture:service-proxy-tabs/service-proxy');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesProperties&include=inputs&include=componentInstances&include=capabilities', 'fixture:service-proxy-tabs/service-with-capabilities-requirment');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesInterfaces&include=componentInstancesProperties&include=componentInstancesInputs&include=inputs&include=componentInstances&include=capabilities','fixture:service-proxy-tabs/full-data-service-proxy')
        cy.route('GET', '**/services/*/consumption/*/interfaces/*/operations/*/inputs', '@serviceInputs');
        cy.route('POST', '**/services/*/consumption/*', '@updateOperationInput');

        const compositionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/composition/details';
        cy.visit(compositionPageUrl);

        // Wait for the canvas to be visible
        cy.get('canvas-search').should('be.visible');

        // Click on the component instance via the testBridge
        cy.window().its('testBridge').invoke('selectComponentInstance', ['childservice_proxy']);

        // Click on operation consumption Tab
        cy.get('.component-details-panel-tabs sdc-tabs ul').children('li[ng-reflect-text="Service Consumption"]').trigger('click', { force: true });
        //update operation property value
        cy.get('.operation-data').trigger('click', { force: true });
        cy.get('.operation-input-name').trigger('click', { force: true });
        cy.wait(1000);
        cy.get('.value-input').clear().type('update property');
        cy.get('[data-tests-id="Save"]').trigger('click', { force: true });
        //check the changed property
        cy.wait(1000);
        cy.route('GET', '**/services/*/consumption/*/interfaces/*/operations/*/inputs', '@inputsUpdateValue');
        cy.get('.operation-data').trigger('click', { force: true });
        cy.wait(1000);
        cy.get('.operation-input-name').trigger('click', { force: true });
        cy.get('.value-input').should('have.value','update property');
    });

});
