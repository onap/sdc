export const initCommonFixtures = cy => {
    cy.fixture('common/user-data').as('user-data');
    cy.fixture('common/setup-ui').as('setupUi');
    cy.fixture('common/data-types').as('data-types');
    cy.fixture('common/dashboard').as('dashboard');
    cy.fixture('common/plugins').as('plugins');
    cy.fixture('common/left-palette-elements-for-resource').as('left-palette-resource');
    cy.fixture('common/left-palette-elements-for-service').as('left-palette-service');
    cy.fixture('common/group-types').as('group-types');
    cy.fixture('common/policy-types').as('policy-types');

    cy.route('GET', '**/authorize', '@user-data');
    cy.route('GET', '**/setup/ui', '@setupUi');
    cy.route('GET', '**/plugins', '@plugins');
    cy.route('GET', '**/dataTypes', '@data-types');
    cy.route('GET', '**/followed', '@dashboard');
    cy.route('GET', '**/latestversion/notabstract/metadata?internalComponentType=VF', '@left-palette-resource');
    cy.route('GET', '**/latestversion/notabstract/metadata?internalComponentType=SERVICE', '@left-palette-service');
    cy.route('GET', '**/catalog/groupTypes?internalComponentType=VF', '@group-types');
    cy.route('GET', '**/catalog/groupTypes?internalComponentType=SERVICE', '@group-types');
    cy.route('GET', '**/catalog/policyTypes?internalComponentType=VF', '@policy-types');
    cy.route('GET', '**/catalog/policyTypes?internalComponentType=SERVICE', '@policy-types');
    cy.viewport(1920, 1080);
};
