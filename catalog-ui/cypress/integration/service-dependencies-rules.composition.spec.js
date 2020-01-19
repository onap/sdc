import { initCommonFixtures } from "../common/init";


describe('Test dependencies rule in service dependencies tab', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);
        cy.fixture('properties-assignment/onap-user-data').as('onapUserData');

        cy.fixture('service-proxy-tabs/metadata-service-with-service').as('metadata');
        cy.fixture('service-proxy-tabs/full-data-service-with-service').as('fullData');
        cy.fixture('service-proxy-tabs/service-inputs').as('serviceInputs')
        cy.fixture('service-proxy-tabs/service-proxy-properties').as('serviceProxyProperties');
        cy.fixture('service-proxy-tabs/service-proxy-node-filter').as('serviceProxyNodeFilter');
        cy.fixture('service-proxy-tabs/properties-rules-result').as('getRules');
        cy.fixture('service-proxy-tabs/add-rule-result').as('addRuleResult');
        cy.fixture('service-proxy-tabs/update-rule-result').as('updateRuleResult');
        cy.fixture('service-proxy-tabs/delete-rule-result').as('deleteRuleResult');
        cy.fixture('service-proxy-tabs/delete-all-rule').as('deleteAllRules');
        cy.fixture('service-proxy-tabs/service-proxy-after-delete-rules').as('serviceProxydeleteAllRules');

    });

    it('Create update and delete Rule', function () {
        cy.route('GET', '**/authorize', '@onapUserData');
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=nonExcludedPolicies&include=nonExcludedGroups&include=forwardingPaths', '@fullData');
        cy.route('GET', '**/services/*', 'fixture:service-proxy-tabs/service-proxy');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesProperties', '@serviceProxyProperties')
        cy.route('GET', '**/services/*/filteredDataByParams?include=nodeFilter', '@serviceProxyNodeFilter');
        cy.route('GET', '**/services/*/filteredDataByParams?include=inputs&include=componentInstances&include=componentInstancesProperties&include=properties','fixture:service-proxy-tabs/full-properties');

        cy.route('POST', '**/services/*/resourceInstances/*/nodeFilter', '@getRules');
        cy.route('POST', '**/services/*/resourceInstances/*/nodeFilter', '@addRuleResult');
        cy.route('PUT', '**/services/*/resourceInstances/*/nodeFilter', '@updateRuleResult');
        cy.route('DELETE', '**/services/*/resourceInstances/*/nodeFilter/*', '@deleteRuleResult');
        cy.route('DELETE', '**/services/*/resourceInstances/*/nodeFilter/*', '@deleteAllRules');
        cy.route('POST','**/services/*/resourceInstance/*', '@serviceProxydeleteAllRules');

        const compositionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/composition/details';
        cy.visit(compositionPageUrl);

        // Wait for the canvas to be visible
        cy.get('canvas-search').should('be.visible');

        // Click on the component instance via the testBridge
        cy.window().its('testBridge').invoke('selectComponentInstance', ['childservice_proxy']);

        // Click on Service Proxy
        cy.get('.component-details-panel-tabs sdc-tabs ul').children('li[ng-reflect-text="Service Dependencies"]').trigger('click', { force: true });
         cy.wait(100);
        //Add new rule
        cy.get('[data-tests-id="add-rule-button"]').trigger('click', { force: true });
        cy.get(':nth-child(1) > .i-sdc-form-select > .ng-valid').select('internalvl0_dhcp_enabled').should('have.value', '0: internalvl0_dhcp_enabled');
        cy.get('.rule-assigned-value > ui-element-dropdown > .ng-pristine').select('TRUE').should('have.value', '0: true');
        cy.get('.ng2-modal-footer > .blue').trigger('click', { force: true });
        //Update rule
        cy.get(':nth-child(1) > .rule-details > .rule-desc').trigger('click', {force: true});
        cy.get('.rule-assigned-value > ui-element-dropdown > .ng-pristine').should('have.value', '0: true');
        cy.get('.rule-assigned-value > ui-element-dropdown > .ng-pristine').select('FALSE').should('have.value', '1: false');
        cy.get('.ng2-modal-footer > .blue').trigger('click', { force: true });
        //delete rule
        cy.get(':nth-child(1) > .rule-details > .delete-icon').trigger('click', {force: true});
        cy.get('[data-tests-id="Delete"]').trigger('click', {force: true});
        //delete all role
        cy.get('[type="checkbox"]').uncheck({ force: true });
        cy.get('[data-tests-id="Yes"]').trigger('click', {force: true});
    });

});
