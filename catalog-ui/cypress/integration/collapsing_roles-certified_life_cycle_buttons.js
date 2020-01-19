import { initCommonFixtures } from "../common/init";

describe('Collapsing Roles', () => {
  beforeEach(() => {
    cy.server();
    initCommonFixtures(cy);

    cy.fixture('common/service-metadata').as('serviceMetaData');
    cy.fixture('common/vf-metadata').as('vfMetaData');
    cy.fixture('common/service-certifyResponse').as('serviceCertifyResponse');
    cy.fixture('common/dependencies').as('dependenciesResponse');
    cy.fixture('common/packages').as('packagesResponse');
  });

  it('Service - Verify UpdateServices, CheckOut, Distribute and Archive exist for a Certified Service that was not Distributed', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceMetaData');
    cy.route('GET', '**/services/*/dependencies',[]);

    this.serviceMetaData.metadata.distributionStatus = 'DISTRIBUTION_NOT_APPROVED';

    const generalPageUrl = '#!/dashboard/workspace/' + this.serviceMetaData.metadata.uniqueId + '/service/general';
    cy.visit(generalPageUrl);

    console.log('Verify life cycle is Distribution Approved');
    cy.get('[data-tests-id="formlifecyclestate"]').should('be.visible');
    cy.get('[data-tests-id="formlifecyclestate"]').should('have.text','Waiting For Distribution');

    console.log('Verify Upgrade Services Button');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('be.visible');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('have.text','Update Services');

    console.log('Verify Distribute Services Button');
    cy.get('[data-tests-id="distribute"]').should('be.visible');
    cy.get('[data-tests-id="distribute"]').should('contain.text','Distribute');

    console.log('Verify Check Out Button');
    cy.get('[data-tests-id="check_out"]').should('be.visible');
    cy.get('[data-tests-id="check_out"]').should('contain.text','Check Out');

    console.log('Verify Archive Button');
    cy.get('[data-tests-id="archive-component-button"]').should('be.visible');
    cy.get('[data-tests-id="archive-component-button"]').should('have.text','Archive');

    console.log('Verify Redistribute Button Not Exist');
    cy.get('[data-tests-id="redistribute"]').should('not.be.visible');
    cy.get('[data-tests-id="redistribute"]').should('not.exist');

    console.log("Verify that Certify button does not exist")
    cy.get('[data-tests-id="certify"]').should('not.be.visible');
  });

  it('Service - Verify UpdateServices, CheckOut, Redistribute and Archive exist for a Certified Service that was Distributed', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceMetaData');
    cy.route('GET', '**/services/*/dependencies',[]);

    this.serviceMetaData.metadata.distributionStatus = 'DISTRIBUTED';

    const generalPageUrl = '#!/dashboard/workspace/' + this.serviceMetaData.metadata.uniqueId + '/service/general';
    cy.visit(generalPageUrl);

    console.log('Verify life cycle is Distribution Approved');
    cy.get('[data-tests-id="formlifecyclestate"]').should('be.visible');
    cy.get('[data-tests-id="formlifecyclestate"]').should('have.text','Distributed');

    console.log('Verify Upgrade Services Button');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('be.visible');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('have.text','Update Services');

    console.log('Verify Distribute Services Button');
    cy.get('[data-tests-id="redistribute"]').should('be.visible');
    cy.get('[data-tests-id="redistribute"]').should('contain.text','Redistribute');

    console.log('Verify Check Out Button');
    cy.get('[data-tests-id="check_out"]').should('be.visible');
    cy.get('[data-tests-id="check_out"]').should('contain.text','Check Out');

    console.log('Verify Archive Button');
    cy.get('[data-tests-id="archive-component-button"]').should('be.visible');
    cy.get('[data-tests-id="archive-component-button"]').should('have.text','Archive');

    console.log('Verify Distribute Button Not Exist');
    cy.get('[data-tests-id="distribute"]').should('not.be.visible');
    cy.get('[data-tests-id="distribute"]').should('not.exist');

    console.log("Verify that Certify button does not exist")
    cy.get('[data-tests-id="certify"]').should('not.be.visible');
  });

  it('Service - Verify UpdateServices button is disabled in case there are no Dependencies for a Certified Service', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceMetaData');
    cy.route('GET', '**/services/*/dependencies',[{dependencies: null}]);

    this.serviceMetaData.metadata.distributionStatus = 'DISTRIBUTED';

    const generalPageUrl = '#!/dashboard/workspace/' + this.serviceMetaData.metadata.uniqueId + '/service/general';
    cy.visit(generalPageUrl);

    console.log('Verify life cycle is Distribution Approved');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('be.visible');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('be.disabled');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('have.text','Update Services');

    console.log("Verify that Certify button does not exist")
    cy.get('[data-tests-id="certify"]').should('not.be.visible');
  });

  it('Service - Verify UpdateServices button is enabled in case there are Dependencies for a Certified Service', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceMetaData');
    cy.route('GET', '**/services/*/dependencies',[{dependencies: {}}]);

    this.serviceMetaData.metadata.distributionStatus = 'DISTRIBUTED';

    const generalPageUrl = '#!/dashboard/workspace/' + this.serviceMetaData.metadata.uniqueId + '/service/general';
    cy.visit(generalPageUrl);

    console.log('Verify life cycle is Distribution Approved');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('be.visible');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('be.enabled');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('have.text','Update Services');

    console.log("Verify that Certify button does not exist")
    cy.get('[data-tests-id="certify"]').should('not.be.visible');
  });

  it('Service - Verify UpdateServices Not exist and Certify Exist in case Service is in state NOT_CERTIFIED_CHECKOUT', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceMetaData');
    cy.route('GET', '**/services/*/dependencies',[{dependencies: {}}]);

    this.serviceMetaData.metadata.lifecycleState = 'NOT_CERTIFIED_CHECKOUT';

    const generalPageUrl = '#!/dashboard/workspace/' + this.serviceMetaData.metadata.uniqueId + '/service/general';
    cy.visit(generalPageUrl);

    console.log('Verify life cycle is Distribution Approved');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('not.be.visible');

    cy.get('[data-tests-id="certify"]').should('be.visible').click();
    cy.get('[data-tests-id="checkindialog"]').should('be.visible');
  });

  it('Service - Verify UpdateServices Not exist and Certify Exist in case Service is in state NOT_CERTIFIED_CHECKIN', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceMetaData');
    cy.route('GET', '**/services/*/dependencies',[{dependencies: {}}]);

    this.serviceMetaData.metadata.lifecycleState = 'NOT_CERTIFIED_CHECKIN';

    const generalPageUrl = '#!/dashboard/workspace/' + this.serviceMetaData.metadata.uniqueId + '/service/general';
    cy.visit(generalPageUrl);

    cy.get('[data-tests-id="certify"]').should('be.visible').click();
    cy.get('[data-tests-id="checkindialog"]').should('be.visible');
  });


  it('Service - When Service is being Certified, and has Dependencies, Update Service modal will be opened automatically', function () {
      cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@serviceMetaData');
      cy.route('GET', '**/services/*/dependencies','@dependenciesResponse');
      cy.route('POST', '**/services/*/lifecycleState/certify','@serviceCertifyResponse');
      cy.route('GET', '**/catalog/services/validate-name/*', { isValid: true})
      this.serviceMetaData.metadata.lifecycleState = 'NOT_CERTIFIED_CHECKOUT';

      const generalPageUrl = '#!/dashboard/workspace/' + this.serviceMetaData.metadata.uniqueId + '/service/general';
      cy.visit(generalPageUrl);

      console.log('Verify life cycle is Distribution Approved');
      cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('not.be.visible');

      console.log('Click Certify');
      cy.get('[data-tests-id="certify"]').should('be.visible').click();

      console.log('Insert confirm text');
      cy.get('[data-tests-id="checkindialog"]').should('be.visible').type('Test_1234');

      console.log('Click OK');
      cy.get('[data-tests-id="confirm-modal-button-ok"]').should('be.visible').click();

      console.log('Close the Modal');
      cy.get('[data-tests-id="upgradeVspModal-button-close"]').should('be.visible').click();
  });

  it('VF - Verify Certify and Check in exist for a NOT_CERTIFIED_CHECKOUT VF', function () {
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfMetaData');
    cy.route('GET', '**/onboarding-api/*/vendor-software-products/packages', '@packagesResponse');

    const generalPageUrl = '#!/dashboard/workspace/' + this.vfMetaData.metadata.uniqueId + '/resource/general';
    cy.visit(generalPageUrl);

    console.log('Verify life cycle is IN DESIGN CHECK OUT');
    cy.get('[data-tests-id="formlifecyclestate"]').should('be.visible');
    cy.get('[data-tests-id="formlifecyclestate"]').should('have.text','In Design Check Out');

    console.log('Verify Certify button exist');
    cy.get('[data-tests-id="certify"]').should('be.visible').click();
    cy.get('[data-tests-id="checkindialog"]').should('be.visible');

    console.log('Verify Check in button exist');
    cy.get('[data-tests-id="check_in"]').should('be.visible');

   });

  it('VF - Verify Certify and Check out exist for a NOT_CERTIFIED_CHECKOUT VF', function () {
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfMetaData');
    cy.route('GET', '**/onboarding-api/*/vendor-software-products/packages', '@packagesResponse');

    this.vfMetaData.metadata.lifecycleState = 'NOT_CERTIFIED_CHECKIN';

    const generalPageUrl = '#!/dashboard/workspace/' + this.vfMetaData.metadata.uniqueId + '/resource/general';
    cy.visit(generalPageUrl);

    console.log('Verify life cycle is IN DESIGN CHECK IN');
    cy.get('[data-tests-id="formlifecyclestate"]').should('be.visible');
    cy.get('[data-tests-id="formlifecyclestate"]').should('have.text','In Design Check In');

    console.log('Verify Certify button exist');
    cy.get('[data-tests-id="certify"]').should('be.visible').click();
    cy.get('[data-tests-id="checkindialog"]').should('be.visible');

    console.log('Verify Check Out button exist');
    cy.get('[data-tests-id="check_out"]').should('be.visible');

   });

  it('VF - Verify UpgradeServices is Disabled in case there are no Dependencies for a Certified VF', function () {
      cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfMetaData');
      cy.route('GET', '**/resources/*/dependencies',[{dependencies: null}]);

        this.vfMetaData.metadata.lifecycleState = 'CERTIFIED';

      const generalPageUrl = '#!/dashboard/workspace/' + this.vfMetaData.metadata.uniqueId + '/resource/general';
      cy.visit(generalPageUrl);

        console.log('Verify life cycle is Certified');
        cy.get('[data-tests-id="formlifecyclestate"]').should('be.visible');
        cy.get('[data-tests-id="formlifecyclestate"]').should('have.text','Certified');

        console.log('Verify life cycle is Distribution Approved');
        cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('be.visible');
        cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('be.disabled');
        cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('have.text','Upgrade Services');

        console.log('Verify Check Out button exist');
        cy.get('[data-tests-id="check_out"]').should('be.visible');

        console.log('Verify Archive Button');
        cy.get('[data-tests-id="archive-component-button"]').should('be.visible');
        cy.get('[data-tests-id="archive-component-button"]').should('have.text','Archive');

    });

  it('VF - Verify UpgradeServices is Enabled and Modal is opened in case there are no Dependencies for a Certified VF', function () {
    cy.route('GET', '**/resources/*/filteredDataByParams?include=metadata', '@vfMetaData');
    cy.route('GET', '**/resources/*/dependencies',[{dependencies: {}}]);
    cy.route('GET', '**/onboarding-api/*/vendor-software-products/packages', '@packagesResponse');

    this.vfMetaData.metadata.lifecycleState = 'CERTIFIED';

    const generalPageUrl = '#!/dashboard/workspace/' + this.vfMetaData.metadata.uniqueId + '/resource/general';
    cy.visit(generalPageUrl);

    console.log('Verify life cycle is Certified');
    cy.get('[data-tests-id="formlifecyclestate"]').should('be.visible');
    cy.get('[data-tests-id="formlifecyclestate"]').should('have.text','Certified');

    console.log('Verify life cycle is Distribution Approved');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('be.visible');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('be.enabled');
    cy.get('[data-tests-id="open-upgrade-vsp-popup"]').should('have.text','Upgrade Services').click();
    cy.get('[class="sdc-modal__wrapper sdc-modal-type-custom"]').should('be.visible');
    cy.get('[data-tests-id="upgradeVspModal-button-close"]').should('be.enabled').click();

    console.log('Verify Check Out button exist');
    cy.get('[data-tests-id="check_out"]').should('be.visible');

    console.log('Verify Archive Button');
    cy.get('[data-tests-id="archive-component-button"]').should('be.visible');
    cy.get('[data-tests-id="archive-component-button"]').should('have.text','Archive');

  });

});
