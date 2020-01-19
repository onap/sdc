import { initCommonFixtures } from "../common/init";


describe('Test add property to self in service at property assignment page', () => {
    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);
         cy.fixture('properties-assignment/onap-user-data').as('onapUserData');

        cy.fixture('service-proxy-tabs/metadata-service-with-service').as('metadata');
        cy.fixture('service-proxy-tabs/full-data-service-with-service').as('fullData');
        cy.fixture('properties-assignment/service-include-policies').as('serviceIncludePolicies');
        cy.fixture('properties-assignment/service-properties').as('serviceProperty');
        cy.fixture('properties-assignment/service-update-properties').as('serviceUpdateProperty');
        cy.fixture('properties-assignment/service-proxy-properties').as('serviceProxyProperties');
        cy.fixture('properties-assignment/create-policies').as('createPolicy');
        cy.fixture('properties-assignment/undeclare-policy').as('undeclarePolicy');
        //vl
        cy.fixture('properties-assignment/vl-properties').as('vlProperties');
        cy.fixture('properties-assignment/vl-list-input').as('vlListInput');
        cy.fixture('properties-assignment/vl-properties-update').as('vlPropertiesUpdate');
    });

    it('add property to self, declare policy, create inputs list', function () {
        cy.route('GET', '**/authorize', '@onapUserData');
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
        cy.route('GET', '**/services/*/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=nonExcludedPolicies&include=nonExcludedGroups&include=forwardingPaths', '@fullData');
        cy.route('GET', '**/services/*/filteredDataByParams?include=inputs&include=componentInstances&include=componentInstancesProperties&include=properties','fixture:service-proxy-tabs/full-properties');
        cy.route('GET','**/services/*/filteredDataByParams?include=componentInstances&include=policies&include=nonExcludedGroups', '@serviceIncludePolicies');
        cy.route('GET', '**/services/*/properties', '@serviceProperty');
        cy.route('POST', '**/services/*/properties', '@serviceUpdateProperty');
        cy.route('GET','**/services/*/componentInstances/*/properties','@serviceProxyProperties');
        cy.route('POST', '**/services/*/create/policies', '@createPolicy');
        cy.route('PUT', '**/services/*/policies/*/undeclare','@undeclarePolicy');

        const compositionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/properties_assignment';
        cy.visit(compositionPageUrl);
        // Add property to service
        cy.get('.table-row').should('have.length', 2);
        cy.get('.add-btn').click();
        cy.get('[data-tests-id="property-name"]').type('new_property');
        cy.get('[data-tests-id="value-property-type"]').select('string');
        cy.get('[data-tests-id="property-description"]').type('new service property');
        cy.get('[data-tests-id="Save"]').trigger('click', {force: true});
        cy.get('.table-row').should('have.length', 3);
        //declare policy for service proxy
        cy.get('[data-tests-id="childservice_proxy 0"]').trigger('click', {force: true});
        cy.get('.table-body > :nth-child(2) > .col1').find('[type="checkbox"]').check({force: true});
        cy.get('[data-tests-id="declare-button declare-policy"]').trigger('click', {force: true});
        cy.get('[data-tests-id="Policies"]').trigger('click', {force: true});
        cy.get('.properties-table').contains('contrailv2virtualnetwork0_segmentation_id');
        cy.get(':nth-child(3) > .valueCol > .delete-button-container > .sprite-new').trigger('click', {force: true});
        cy.get('[data-tests-id="Delete"]').trigger('click', {force: true});
        cy.get('.properties-table').contains('contrailv2virtualnetwork0_segmentation_id');
        cy.wait(100);
        //create list inputs for vl 0
        cy.route('GET','**/services/*/componentInstances/*/properties','@vlProperties');
        cy.route('POST', '**/services/*/create/listInput', '@vlListInput');
        cy.get('[data-tests-id="Composition"]').trigger('click', {force: true});
        cy.get('[data-tests-id="Properties"]').trigger('click', {force: true});
        cy.get('[data-tests-id="VL 0"]').trigger('click', {force: true});

        cy.get('.table-body > :nth-child(5) > .col1').find('[type="checkbox"]').check({force: true});
        cy.get('.table-body > :nth-child(6) > .col1').find('[type="checkbox"]').check({force: true});

        cy.get('[data-tests-id="declare-button declare-list-input"]').trigger('click', {force: true});
        cy.get('[data-tests-id="property-name"]').type('vl 0 list');
        cy.get('input[name="typeName"]').type('new schema');
        cy.get('[data-tests-id="property-description"]').type('new list for vl 0 properties');
        cy.get('[data-tests-id="Save"]').trigger('click', {force: true});
        cy.get('[data-tests-id="Inputs"]').trigger('click', {force: true});
        cy.get('.properties-table').contains('vl 0 list');
    });

});
