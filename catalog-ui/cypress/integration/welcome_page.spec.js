import { initCommonFixtures } from "../common/init";

describe('The Welcome Page', function () {

    beforeEach(() => {
        cy.server();
        initCommonFixtures(cy);

        cy.fixture('metadata-vf').as('metadata');
        cy.fixture('full-data-vf').as('fullData');
    })

    it.skip('successfully loads', function () {
        cy.visit('/');
        cy.get('.project-icon').should('be.visible');
    })
    it.skip('redirects to the dashboard', function () {
        cy.url().should('contain', 'dashboard')
    })
})