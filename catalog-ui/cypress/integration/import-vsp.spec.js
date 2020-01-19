import { initCommonFixtures } from "../common/init";

describe('Update vsp', () => {
  beforeEach(() => {
    cy.server();
    initCommonFixtures(cy);
    // Followed Resources for the dashboard screen
    cy.fixture('home/followed').as('followed');
    cy.fixture('update-vsp/packages-first').as('packagesFirst');
    cy.fixture('update-vsp/vsp-first').as('vspFirst');
    cy.fixture('update-vsp/vf-metadata-first').as('vfMetadataFirst');

    cy.fixture('update-vsp/checkout-vsp').as('checkoutVsp');
    cy.fixture('update-vsp/metadata-after-checkout').as('metadataAfterCheckout');

    cy.fixture('update-vsp/metadata-after-save-fails').as('metadataAfterSaveFails');

    cy.fixture('update-vsp/packages-second').as('packagesSecond');
    cy.fixture('update-vsp/metadata-second').as('metadataSecond');
    cy.fixture('update-vsp/checkout-vsp-second').as('checkoutVspSecond');
    cy.fixture('update-vsp/metadata-third').as('metadataThird');

    cy.fixture('update-vsp/packages-browse').as('packagesBrowse');
    cy.fixture('update-vsp/metadata-browse').as('metadatabrowse');

    cy.route('GET', '**/followed', '@followed');

  });

  it('Test if checkout fail when update vsp we display the old version', function () {
    const homePageUrl = '#!/dashboard';
    cy.visit(homePageUrl);

    cy.route('GET', '**/packages', '@packagesFirst');
    cy.route('GET', '**/resources/csar/*', 'fixture:update-vsp/vsp-first');
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfMetadataFirst');
    cy.route({
      method: 'POST',
      url: '**/resources/*/lifecycleState/CHECKOUT',
      status: 404,
      response: {
        "requestError": {
          "serviceException": {
            "messageId": "SVC4063",
            "text": "Error: Requested \u0027%1\u0027 resource was not found.",
            "ecompRequestId": "87c1efc1-9f8e-4953-9394-654d28e48bbd",
            "variables": [
              ""
            ]
          }
        }
      }
    });

    cy.get('[data-tests-id="repository-icon"]').should('be.visible');
    cy.get('[data-tests-id="repository-icon"]').click({ force: true });
    cy.get('[data-tests-id="csar-row"]').should('be.visible');
    cy.get('[data-tests-id="csar-row"]').last().click({ force: true });
    cy.get('[data-tests-id="update-csar"]').click({ force: true });
    cy.get('[data-tests-id="error-modal-button-ok"]').click({ force: true });
    cy.get('[data-tests-id="check_out"]').should('be.visible');
    cy.get('[data-tests-id="filename"]').should('be.visible');
    cy.get('[data-tests-id="browseButton"]').should('be.visible');
    cy.get('[data-tests-id="filename"]').contains('test update vsp (1.0)');

  });
  it('Test if save fail when update vsp and the mode is check in. we did check out and see the unsave warning', function () {
    const homePageUrl = '#!/dashboard';
    cy.visit(homePageUrl);
    cy.route('GET', '**/packages', '@packagesFirst');
    cy.route('GET', '**/resources/csar/*', 'fixture:update-vsp/vsp-first');
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfMetadataFirst');
    cy.route('POST', '**/resources/*/lifecycleState/CHECKOUT', '@checkoutVsp');
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@metadataAfterCheckout');
    cy.route({
      method: 'PUT',
      url: '**/resources/*',
      status: 404,
      response: {
        "requestError": {
          "serviceException": {
            "messageId": "SVC4063",
            "text": "Error: Requested \u0027%1\u0027 resource was not found.",
            "ecompRequestId": "591b0702-115d-4b6a-94c4-ed2bbbc452bc",
            "variables": [
              null
            ]
          }
        }
      }
    });

    cy.get('[data-tests-id="repository-icon"]').should('be.visible');
    cy.get('[data-tests-id="repository-icon"]').click({ force: true });
    cy.get('[data-tests-id="csar-row"]').should('be.visible');
    cy.get('[data-tests-id="csar-row"]').last().click({ force: true });
    cy.get('[data-tests-id="update-csar"]').click({ force: true });
    cy.get('[data-tests-id="check_in"]').should('be.visible');
    cy.get('[data-tests-id="error-modal-button-ok"]').click({ force: true });
    cy.get('[data-tests-id="filename"]').should('be.visible');
    cy.get('[data-tests-id="browseButton"]').should('be.visible');
    cy.get('[data-tests-id="filename"]').contains('test update vsp (2.0)');
    cy.get('[data-tests-id="save-warning"]').should('be.visible');

  });
  it('Test after save fails when refresh the page the new version did not update ', function () {
    cy.route('GET', '**/packages', '@packagesFirst');
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@metadataAfterSaveFails');
    const vspUrl = '#!/dashboard/workspace/92582379-f0d5-4655-a0f1-f92b3038d853/resource/general';
    cy.visit(vspUrl);
    cy.get('[data-tests-id="filename"]').contains('test update vsp (1.0)');
  });
  it('Test when update vsp and the mode is check out. we save the new version', function () {
    const homePageUrl = '#!/dashboard';
    cy.visit(homePageUrl);

    cy.route('GET', '**/packages', '@packagesFirst');
    cy.route('GET', '**/resources/csar/*', 'fixture:update-vsp/vsp-first');
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@metadataAfterCheckout');
    cy.route('PUT', '**/resources/*', 'fixture:update-vsp/save-vsp');


    cy.get('[data-tests-id="repository-icon"]').should('be.visible');
    cy.get('[data-tests-id="repository-icon"]').click({ force: true });
    cy.get('[data-tests-id="csar-row"]').should('be.visible');
    cy.get('[data-tests-id="csar-row"]').last().click({ force: true });
    cy.get('[data-tests-id="update-csar"]').click({ force: true });

    cy.get('[data-tests-id="check_in"]').should('be.visible');
    cy.get('[data-tests-id="filename"]').contains('test update vsp (2.0)');
    cy.get('[data-tests-id="save-warning"]').should('not.be.visible');


  });
  it('Test after save succeeded when refresh the page the new version update', function () {
    cy.route('GET', '**/packages', '@packagesSecond');
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@metadataSecond');
    const vspUrl = '#!/dashboard/workspace/09f56471-cb97-49f9-af25-44eaa1af1f05/resource/general';
    cy.visit(vspUrl);
    cy.get('[data-tests-id="filename"]').contains('test update vsp (2.0)');

  });

  it('Test when update vsp and the mode is check in. we do checkout and save the new version', function () {
    const homePageUrl = '#!/dashboard';
    cy.visit(homePageUrl);

    cy.route('GET', '**/packages', '@packagesSecond');
    cy.route('GET', '**/resources/csar/*', 'fixture:update-vsp/vsp-second');
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@metadataSecond');
    cy.route('POST', '**/resources/*/lifecycleState/CHECKOUT', '@checkoutVspSecond');
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@metadataThird');
    cy.route('PUT', '**/resources/*', 'fixture:update-vsp/save-vsp-second');

    cy.get('[data-tests-id="repository-icon"]').should('be.visible');
    cy.get('[data-tests-id="repository-icon"]').click({ force: true });
    cy.get('[data-tests-id="csar-row"]').should('be.visible');
    cy.get('[data-tests-id="csar-row"]').last().click({ force: true });
    cy.get('[data-tests-id="update-csar"]').click({ force: true });
    cy.get('[data-tests-id="check_in"]').should('be.visible');
    cy.get('[data-tests-id="filename"]').contains('test update vsp (3.0)');
    cy.get('[data-tests-id="save-warning"]').should('not.be.visible');
  });

  it('Test update vsp from browse button, and version in the vf is the same of vsp, check that check out and save finish succeessfully', function () {
    
    cy.route('GET', '**/packages', '@packagesSecond');
    cy.route('GET', '**/resources/csar/*', 'fixture:update-vsp/vsp-second');
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@metadatabrowse');
    cy.route('PUT', '**/resources/*', 'fixture:update-vsp/save-vsp-second');

    const homePageUrl = '#!/dashboard/workspace/09f56471-cb97-49f9-af25-44eaa1af1f05/resource/general';
    cy.visit(homePageUrl);

    cy.get('[data-tests-id="filename"]').contains('test update vsp (3.0)');
    cy.get('[data-tests-id="browseButton"]').click({ force: true });
    cy.get('[data-tests-id="csar-row"]').should('be.visible');
    cy.get('[data-tests-id="csar-row"]').last().click({ force: true });
    cy.get('[data-tests-id="update-csar"]').click({ force: true });
    cy.get('[data-tests-id="check_in"]').should('be.visible');
    cy.get('[data-tests-id="filename"]').contains('test update vsp (3.0)');
    cy.get('[data-tests-id="save-warning"]').should('not.be.visible');

  });

  it('Test update vsp from browse button check out and save succeessfully', function () {
    const homePageUrl = '#!/dashboard/workspace/09f56471-cb97-49f9-af25-44eaa1af1f05/resource/general';
    cy.visit(homePageUrl);

    cy.route('GET', '**/packages', '@packagesBrowse');
    cy.route('GET', '**/resources/csar/*', 'fixture:update-vsp/vsp-browse');
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@metadatabrowse');
    cy.route('PUT', '**/resources/*', 'fixture:update-vsp/save-vsp-browse');

    cy.get('[data-tests-id="filename"]').contains('test update vsp (3.0)');
    cy.get('[data-tests-id="browseButton"]').click({ force: true });
    cy.get('[data-tests-id="csar-row"]').should('be.visible');
    cy.get('[data-tests-id="csar-row"]').last().click({ force: true });
    cy.get('[data-tests-id="update-csar"]').click({ force: true });
    cy.get('[data-tests-id="check_in"]').should('be.visible');
    cy.get('[data-tests-id="filename"]').contains('test update vsp (4.0)');
    cy.get('[data-tests-id="save-warning"]').should('not.be.visible');

  });
})