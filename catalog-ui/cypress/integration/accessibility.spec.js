describe('Accessibility', () => {
  beforeEach(() => {
    cy.injectAxe();
  });

  it('Dashboard page has no critical accessibility violations', () => {
    cy.visit('/sdc1/#!/dashboard');
    cy.get('.sdc-catalog-header', { timeout: 10000 }).should('be.visible');
    cy.checkA11y(null, {
      runOnly: {
        type: 'tag',
        values: ['wcag2a', 'wcag2aa']
      }
    });
  });

  it('Catalog page has no critical accessibility violations', () => {
    cy.visit('/sdc1/#!/catalog');
    cy.get('.sdc-catalog-header', { timeout: 10000 }).should('be.visible');
    cy.checkA11y(null, {
      runOnly: {
        type: 'tag',
        values: ['wcag2a', 'wcag2aa']
      }
    });
  });
});
