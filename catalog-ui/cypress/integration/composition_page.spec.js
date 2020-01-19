import { initCommonFixtures } from "../common/init";

describe('The Composition Page', function () {

    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);

        cy.fixture('metadata-vf').as('metadata');
        cy.fixture('full-data-vf').as('fullData');

        cy.fixture('metadata-vf1').as('metadata1');
        cy.fixture('full-data-vf1').as('fullData1');
    })

    it('VF Verify groups policies right panel tabs, name and Icon', function () {
        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@metadata');
        cy.route('GET', '**/resources/*/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=nonExcludedPolicies&include=nonExcludedGroups', '@fullData');

        const compositionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/resource/composition/details';
        cy.visit(compositionPageUrl);

        cy.get('.sdc-composition-graph-wrapper').should('be.visible');      // it should show the correct number of elements on the canvas -- how to??

        //Group / Policy
        cy.get('.policy-zone .sdc-canvas-zone__counter').should('have.text', this.fullData.policies.length.toString()); //4 Policies
        cy.get('.group-zone .sdc-canvas-zone__counter').should('have.html', this.fullData.groups.length.toString()); //2 Groups

        //VF name & Icon
        cy.get('[data-tests-id=selectedCompTitle]').should('contain', this.metadata.metadata.name);// Name is vf
        cy.get('.icon').children('sdc-element-icon').children('div').children('svg-icon').should('exist');// Icon exist
        cy.get('.icon').children('sdc-element-icon').children('div').children('svg-icon').should('have.attr', 'ng-reflect-name', 'defaulticon');
        cy.get('.icon').children('sdc-element-icon').children('div').children('svg-icon').should('have.attr', 'ng-reflect-type','resources_24');
        cy.get('.icon').children('sdc-element-icon').children('div').children('svg-icon').should('have.attr', 'ng-reflect-size','x_large');
        cy.get('.icon').children('sdc-element-icon').children('div').children('svg-icon').should('have.attr', 'ng-reflect-background-shape','circle');
        cy.get('.icon').children('sdc-element-icon').children('div').children('svg-icon').should('have.attr', 'ng-reflect-background-color','purple');

        //Verify the below tabs exist for VF
        cy.get('.component-details-panel-tabs sdc-tabs ul').children('li[ng-reflect-text=Information]').should('exist');//General Info Tab Exist
        cy.get('.component-details-panel-tabs sdc-tabs ul').children('li[ng-reflect-text="Deployment Artifacts"]').should('exist');//General Info Tab Exist
        cy.get('.component-details-panel-tabs sdc-tabs ul').children('li[ng-reflect-text=Properties]').should('exist');//General Info Tab Exist
        cy.get('.component-details-panel-tabs sdc-tabs ul').children('li[ng-reflect-text="Information Artifacts"]').should('exist');//General Info Tab Exist
        cy.get('.component-details-panel-tabs sdc-tabs ul').children('li[ng-reflect-text="Requirements and Capabilities"]').should('exist');//General Info Tab Exist

        cy.get('[data-tests-id="leftPalette.category.Generic"]').should('exist');
        cy.get('[data-tests-id="leftPalette.category.Generic"]').children().get('.sdc-accordion-header').should('exist');
        cy.get('[data-tests-id="leftPalette.category.Generic"]').children().get('.sdc-accordion-header').first().trigger('click');


        // //Drag & Drop
        // cy.get('[data-tests-id="extContrailCP"]').children('palette-element').children('div').trigger("mousedown", { which: 1 })
        //     .trigger("mousemove", { which: 1, clientX: 200, clientY: 200})
        //     .trigger("mouseup")

    })

    it('successfully loads VF', function () {
        //Verify can change ShayTestVF from V1.0 to V1.1
        //Verify the right panel properties

        cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@metadata1');
        cy.route('GET', '**/resources/*/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=nonExcludedPolicies&include=nonExcludedGroups', '@fullData1');

        const compositionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/resource/composition/details'
        cy.visit(compositionPageUrl);

        cy.get('.sdc-composition-graph-wrapper').should('be.visible');      // it should show the correct number of elements on the canvas -- how to??


        // TODO:
        // 1. Verify element exist on Canvas
        // 2. Drag and drop new Element
        

        // //Drag & Drop
        // // cy.get('[data-tests-id="Port"]').children('palette-element').children('div').children('sdc-element-icon').trigger("mousedown", { which: 1 })
        // cy.get('[data-tests-id="extContrailCP"]').children('palette-element').children('div').trigger("mousedown", { which: 1 })
        // // cy.get('.zoom-icons').first()
        //     .trigger("mousemove", { which: 1, clientX: 200, clientY: 200})
        //     // .trigger("mousemove")
        //     .trigger("mouseup")

    })

    // it('can add a deployment artifact', function() {

    // });
    // it('can delete a deployment artifact', function() {

    // });
    // it('can delete a component instance on the canvas', function() {

    // });
    // it('can add a component instance to the canvas', function() {

    // });
    // it('can add a group', function() {

    // });
    // it('can add targets to a group', function() {

    // });

})