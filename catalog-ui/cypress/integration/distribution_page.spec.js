import { initCommonFixtures } from "../common/init";

describe('Distribution Page', () => {
    beforeEach(() => {
    cy.server();
    initCommonFixtures(cy);

    cy.fixture('deployment-artifacts/metadata-service-with-vsp').as('metadata');
    cy.fixture('distribution-page/distributionList').as('distributionList');
    cy.fixture('distribution-page/distributionStatus').as('distributionStatus');
});

it('Test the No Distribution Label exist in case no distributions', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');

    const distributionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/distribution';
    cy.visit(distributionPageUrl);

    console.log('Make sure No Distributions To Present is visible');
    cy.get('[data-tests-id="noDistributionsLabel"]').should('be.visible');
    cy.get('[data-tests-id="noDistributionsLabel"]').should('have.text','No Distributions To Present');
});


it('Test Distribution Sum, Search and Refresh exist in case there are distributions', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
    cy.route('GET', '**/services/*/distribution', '@distributionList');

    const distributionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/distribution';
    cy.visit(distributionPageUrl);

    console.log('Make sure No Distributions To Present is visible');
    cy.get('[data-tests-id="noDistributionsLabel"]').should('not.be.visible')

    console.log('Make sure Number of Distributions is as expected');
    cy.get('[data-tests-id="DistributionsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalArtifacts"]').should('be.visible');
    cy.get('[data-tests-id="DistributionsLabel"]').should('have.text','DISTRIBUTION [2]');

    console.log('Make sure Filter text box appear is as expected');
    cy.get('[data-tests-id="searchTextbox"]').should('be.visible');

    console.log('Make sure refresh button appear is as expected');
    cy.get('[data-tests-id="refreshButton"]').should('be.visible');
});

it('Test Filter Distributions as expectd', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
    cy.route('GET', '**/services/*/distribution', '@distributionList');

    const distributionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/distribution';
    cy.visit(distributionPageUrl);

    console.log('Make sure 2 Distributions are presented in the table');
    cy.get('[data-tests-id="expandIcon_d2595"]').should('be.visible');

    cy.get('[data-tests-id="distID_d2595"]').should('be.visible');
    cy.get('[data-tests-id="userID_d2595"]').should('be.visible');
    cy.get('[data-tests-id="timeStamp_d2595"]').should('be.visible');
    cy.get('[data-tests-id="status_d2595"]').should('be.visible');

    cy.get('[data-tests-id="distID_9a5b5"]').should('be.visible');
    cy.get('[data-tests-id="userID_9a5b5"]').should('be.visible');
    cy.get('[data-tests-id="timeStamp_9a5b5"]').should('be.visible');
    cy.get('[data-tests-id="status_9a5b5"]').should('be.visible');

    console.log('Verify the Distribution details in the Distributions Table');
    cy.get('[data-tests-id="distID_9a5b5"]').should('have.text','9a5b5a9d-5d52-40df-bd62-fe71e7c01b85');
    cy.get('[data-tests-id="userID_9a5b5"]').should('have.text','Aretha Franklin(op0001)');
    cy.get('[data-tests-id="timeStamp_9a5b5"]').should('have.text','2019-07-25 09:47:55.849 UTC ');
    cy.get('[data-tests-id="status_9a5b5"]').should('contains.text','Distributed');

    console.log('Insert filter criteria to the input text box');
    cy.get('[data-tests-id="searchTextbox"]').type('9a');

    console.log('Verify that distribution that should not be displayed is not in the table');
    cy.get('[data-tests-id="distID_d2595"]').should('not.be.visible');
    cy.get('[data-tests-id="userID_d2595"]').should('not.be.visible');
    cy.get('[data-tests-id="timeStamp_d2595"]').should('not.be.visible');
    cy.get('[data-tests-id="status_d2595"]').should('not.be.visible');

    console.log('Clear the filter');
    cy.get('[data-tests-id="searchTextbox"]').clear();

    console.log('Verify all appear as expected');
    cy.get('[data-tests-id="distID_d2595"]').should('be.visible');
    cy.get('[data-tests-id="userID_d2595"]').should('be.visible');
    cy.get('[data-tests-id="timeStamp_d2595"]').should('be.visible');
    cy.get('[data-tests-id="status_d2595"]').should('be.visible');

    cy.get('[data-tests-id="distID_9a5b5"]').should('be.visible');
    cy.get('[data-tests-id="userID_9a5b5"]').should('be.visible');
    cy.get('[data-tests-id="timeStamp_9a5b5"]').should('be.visible');
    cy.get('[data-tests-id="status_9a5b5"]').should('be.visible');
});

    it('Test Distribution and Component Statuses for specific Distribution ID', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
        cy.route('GET', '**/services/*/distribution', '@distributionList');
        cy.route('GET', '**/services/distribution/9a5b5a9d-5d52-40df-bd62-fe71e7c01b85', '@distributionStatus');

    const distributionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/distribution';
    cy.visit(distributionPageUrl);

    console.log('Click on specific DistributionID to display its status');
    cy.get('[data-tests-id="expandIcon_9a5b5"]').click();

    console.log('Verify Total Artifacts label for Distribution');
    cy.get('[data-tests-id="totalDistributionArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalDistributionArtifactsLabel"]').should('have.text','Total Artifacts 8 ');

    console.log('Verify Total Notified label for Distribution');
    cy.get('[data-tests-id="totalDistributionNotifiedArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalDistributionNotifiedArtifactsLabel"]').should('have.text','Notified 1');

    console.log('Verify Total Downloaded label for Distribution');
    cy.get('[data-tests-id="totalDistributionDownloadedArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalDistributionDownloadedArtifactsLabel"]').should('have.text','Downloaded 1');

    console.log('Verify Total Deployed label for Distribution');
    cy.get('[data-tests-id="totalDistributionDeployedArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalDistributionDeployedArtifactsLabel"]').should('have.text','Deployed 1');

    console.log('Verify Total Not Notified label for Distribution');
    cy.get('[data-tests-id="totalDistributionNotNotifiedArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalDistributionNotNotifiedArtifactsLabel"]').should('have.text','Not Notified 1');

    console.log('Verify Total Download Errors label for Distribution');
    cy.get('[data-tests-id="totalDistributionDownloadErrorArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalDistributionDownloadErrorArtifactsLabel"]').should('have.text','Download Errors 1');

    console.log('Verify Total Deploy Errors label for Distribution');
    cy.get('[data-tests-id="totalDistributionDeployErrorArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalDistributionDeployErrorArtifactsLabel"]').should('have.text','Deploy Errors 1');



    console.log('Verify Total Artifacts label for Consumer1');
    cy.get('[data-tests-id="totalConsumer1ArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalConsumer1ArtifactsLabel"]').should('have.text','Consumer1 1');

    console.log('Verify Total Notified label for Consumer1');
    cy.get('[data-tests-id="totalConsumer1NotifiedArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalConsumer1NotifiedArtifactsLabel"]').should('have.text','Notified 1');

    console.log('Verify Total Downloaded label for Consumer1');
    cy.get('[data-tests-id="totalConsumer1DownloadedArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalConsumer1DownloadedArtifactsLabel"]').should('have.text','Downloaded 0');

    console.log('Verify Total Deployed label for Consumer1');
    cy.get('[data-tests-id="totalConsumer1DeployedArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalConsumer1DeployedArtifactsLabel"]').should('have.text','Deployed 0');

    console.log('Verify Total Not Notified label for Consumer1');
    cy.get('[data-tests-id="totalConsumer1NotNotifiedArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalConsumer1NotNotifiedArtifactsLabel"]').should('have.text','Not Notified 0');

    console.log('Verify Total Download Errors label for Consumer1');
    cy.get('[data-tests-id="totalConsumer1DownloadErrorsArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalConsumer1DownloadErrorsArtifactsLabel"]').should('have.text','Download Errors 0');

    console.log('Verify Total Deploy Errors label for Consumer1');
    cy.get('[data-tests-id="totalConsumer1DeployErrorsArtifactsLabel"]').should('be.visible');
    cy.get('[data-tests-id="totalConsumer1DeployErrorsArtifactsLabel"]').should('have.text','Deploy Errors 0');




    console.log('Verify Total Artifacts label for Consumer2');
    cy.get('[data-tests-id="totalConsumer2ArtifactsLabel"]').should('have.text','Consumer2 2');

    console.log('Verify Total Notified label for Consumer2');
    cy.get('[data-tests-id="totalConsumer2NotifiedArtifactsLabel"]').should('have.text','Notified 0');

    console.log('Verify Total Downloaded label for Consumer2');
    cy.get('[data-tests-id="totalConsumer2DownloadedArtifactsLabel"]').should('have.text','Downloaded 1');

    console.log('Verify Total Deployed label for Consumer2');
    cy.get('[data-tests-id="totalConsumer2DeployedArtifactsLabel"]').should('have.text','Deployed 1');

    console.log('Verify Total Not Notified label for Consumer2');
    cy.get('[data-tests-id="totalConsumer2NotNotifiedArtifactsLabel"]').should('have.text','Not Notified 0');

    console.log('Verify Total Download Errors label for Consumer2');
    cy.get('[data-tests-id="totalConsumer2DownloadErrorsArtifactsLabel"]').should('have.text','Download Errors 0');

    console.log('Verify Total Deploy Errors label for Consumer2');
    cy.get('[data-tests-id="totalConsumer2DeployErrorsArtifactsLabel"]').should('have.text','Deploy Errors 0');





    console.log('Verify Total Artifacts label for Consumer3');
    cy.get('[data-tests-id="totalConsumer3ArtifactsLabel"]').should('have.text','Consumer3 2');

    console.log('Verify Total Notified label for Consumer3');
    cy.get('[data-tests-id="totalConsumer3NotifiedArtifactsLabel"]').should('have.text','Notified 0');

    console.log('Verify Total Downloaded label for Consumer3');
    cy.get('[data-tests-id="totalConsumer3DownloadedArtifactsLabel"]').should('have.text','Downloaded 0');

    console.log('Verify Total Deployed label for Consumer3');
    cy.get('[data-tests-id="totalConsumer3DeployedArtifactsLabel"]').should('have.text','Deployed 0');

    console.log('Verify Total Not Notified label for Consumer3');
    cy.get('[data-tests-id="totalConsumer3NotNotifiedArtifactsLabel"]').should('have.text','Not Notified 1');

    console.log('Verify Total Download Errors label for Consumer3');
    cy.get('[data-tests-id="totalConsumer3DownloadErrorsArtifactsLabel"]').should('have.text','Download Errors 0');

    console.log('Verify Total Deploy Errors label for Consumer3');
    cy.get('[data-tests-id="totalConsumer3DeployErrorsArtifactsLabel"]').should('have.text','Deploy Errors 1');






    console.log('Verify Total Artifacts label for Consumer4');
    cy.get('[data-tests-id="totalConsumer4ArtifactsLabel"]').should('have.text','Consumer4 1');

    console.log('Verify Total Notified label for Consumer4');
    cy.get('[data-tests-id="totalConsumer4NotifiedArtifactsLabel"]').should('have.text','Notified 0');

    console.log('Verify Total Downloaded label for Consumer4');
    cy.get('[data-tests-id="totalConsumer4DownloadedArtifactsLabel"]').should('have.text','Downloaded 0');

    console.log('Verify Total Deployed label for Consumer4');
    cy.get('[data-tests-id="totalConsumer4DeployedArtifactsLabel"]').should('have.text','Deployed 0');

    console.log('Verify Total Not Notified label for Consumer4');
    cy.get('[data-tests-id="totalConsumer4NotNotifiedArtifactsLabel"]').should('have.text','Not Notified 0');

    console.log('Verify Total Download Errors label for Consumer4');
    cy.get('[data-tests-id="totalConsumer4DownloadErrorsArtifactsLabel"]').should('have.text','Download Errors 0');

    console.log('Verify Total Deploy Errors label for Consumer4');
    cy.get('[data-tests-id="totalConsumer4DeployErrorsArtifactsLabel"]').should('have.text','Deploy Errors 0');



    console.log('Verify Total Artifacts label for Consumer5');
    cy.get('[data-tests-id="totalConsumer5ArtifactsLabel"]').should('have.text','Consumer5 1');



    console.log('Verify Total Artifacts label for Consumer6');
    cy.get('[data-tests-id="totalConsumer6ArtifactsLabel"]').should('have.text','Consumer6 1');

    console.log('Verify Total Download Errors label for Consumer6');
    cy.get('[data-tests-id="totalConsumer6DownloadErrorsArtifactsLabel"]').should('have.text','Download Errors 1');

});

it('Test Artifacts table for Specific Consumer and specific Artifact', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
    cy.route('GET', '**/services/*/distribution', '@distributionList');
    cy.route('GET', '**/services/distribution/9a5b5a9d-5d52-40df-bd62-fe71e7c01b85', '@distributionStatus');

    const distributionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/distribution';
    cy.visit(distributionPageUrl);

    console.log('Click on specific DistributionID icon to display its status');
    cy.get('[data-tests-id="expandIcon_9a5b5"]').click();

    console.log('Click on specific ServiceID icon to display its artifacts');
    cy.get('[data-tests-id="expandIcon_Consumer1"]').click();

    console.log('Verify ComponentID for Consumer: Consumer1, artifact: artifact1');
    cy.get('[data-tests-id="compID_Consumer1_artifact1.env"]').should('be.visible');
    cy.get('[data-tests-id="compID_Consumer1_artifact1.env"]').should('contain.text','Consumer1');

    console.log('Verify Artifact Name for Consumer: Consumer1, artifact: artifact1');
    cy.get('[data-tests-id="artName_Consumer1_artifact1.env"]').should('be.visible');
    cy.get('[data-tests-id="artName_Consumer1_artifact1.env"]').should('have.text','artifact1.env');

    console.log('Verify URL for Consumer: Consumer1, artifact: artifact1');
    cy.get('[data-tests-id="url_Consumer1_artifact1.env"]').should('have.text','url/artifacts/artifact1.env');

    console.log('Verify Time for Consumer: Consumer1, artifact: artifact1');
    cy.get('[data-tests-id="time_Consumer1_artifact1.env"]').should('contain.text','7/25/19, 9:47 AM');

    console.log('Verify Status for Consumer: Consumer1, artifact: artifact1');
    cy.get('[data-tests-id="status_Consumer1_artifact1.env"]').should('have.text','NOTIFIED');

    cy.get('[data-tests-id="expandIcon_Consumer2"]').click();
    cy.get('[data-tests-id="compID_Consumer2_artifact2_1.csar"]').should('contain.text','Consumer2');
    cy.get('[data-tests-id="artName_Consumer2_artifact2_1.csar"]').should('contain.text','artifact2_1.csar');
    cy.get('[data-tests-id="time_Consumer2_artifact2_1.csar"]').should('contain.text','7/25/19, 9:47 AM');
    cy.get('[data-tests-id="status_Consumer2_artifact2_1.csar"]').should('contain.text','DOWNLOAD_OK');

    cy.get('[data-tests-id="compID_Consumer2_artifact2_2.csar"]').should('contain.text','Consumer2');
    cy.get('[data-tests-id="artName_Consumer2_artifact2_2.csar"]').should('contain.text','artifact2_2.csar');
    cy.get('[data-tests-id="time_Consumer2_artifact2_2.csar"]').should('contain.text','7/25/19, 9:47 AM');
    cy.get('[data-tests-id="status_Consumer2_artifact2_2.csar"]').should('contain.text','DEPLOY_OK');

});

it('Test Artifacts Notifications for Specific Consumer and specific Artifact', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
    cy.route('GET', '**/services/*/distribution', '@distributionList');
    cy.route('GET', '**/services/distribution/9a5b5a9d-5d52-40df-bd62-fe71e7c01b85', '@distributionStatus');

    const distributionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/distribution';
    cy.visit(distributionPageUrl);

    console.log('Click on specific DistributionID to display its status');
    cy.get('[data-tests-id="expandIcon_9a5b5"]').click();

    console.log('Click on specific ServiceID icon to display its artifacts');
    cy.get('[data-tests-id="expandIcon_Consumer5"]').click();

    console.log('Click on specific artifact to verify its notifications');
    cy.get('[data-tests-id="expandIcon_compID_Consumer5_ArtifactWithTwoStatuses"]').click();

    console.log('Verify EARLIER_STATUS Time Stamp');
    cy.get('[data-tests-id="statusTimeStamp_EARLIER_STATUS_Consumer5_ArtifactWithTwoStatuses"]').should('be.visible');
    cy.get('[data-tests-id="statusTimeStamp_EARLIER_STATUS_Consumer5_ArtifactWithTwoStatuses"]').should('have.text','7/24/19, 9:47 AM');

    console.log('Verify EARLIER_STATUS Status Value');
    cy.get('[data-tests-id="statusValue_EARLIER_STATUS_Consumer5_ArtifactWithTwoStatuses"]').should('be.visible');
    cy.get('[data-tests-id="statusValue_EARLIER_STATUS_Consumer5_ArtifactWithTwoStatuses"]').should('have.text','EARLIER_STATUS');

    console.log('Verify LATEST_STATUS Time Stamp');
    cy.get('[data-tests-id="statusTimeStamp_LATEST_STATUS_Consumer5_ArtifactWithTwoStatuses"]').should('be.visible');
    cy.get('[data-tests-id="statusTimeStamp_LATEST_STATUS_Consumer5_ArtifactWithTwoStatuses"]').should('have.text','7/25/19, 9:47 AM');

    console.log('Verify LATEST_STATUS Status Value');
    cy.get('[data-tests-id="statusValue_LATEST_STATUS_Consumer5_ArtifactWithTwoStatuses"]').should('be.visible');
    cy.get('[data-tests-id="statusValue_LATEST_STATUS_Consumer5_ArtifactWithTwoStatuses"]').should('have.text','LATEST_STATUS');

    console.log('Verify LATEST_STATUS Status apear in the component (verify that the last time stamp appear)');
    cy.get('[data-tests-id="status_Consumer5_ArtifactWithTwoStatuses"]').should('have.text','LATEST_STATUS');
});

    it('Test Open Notified Modal for specific Distribution ID', function () {
        cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
        cy.route('GET', '**/services/*/distribution', '@distributionList');
        cy.route('GET', '**/services/distribution/9a5b5a9d-5d52-40df-bd62-fe71e7c01b85', '@distributionStatus');

    const distributionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/distribution';
    cy.visit(distributionPageUrl);

    console.log('Click on specific DistributionID to display its status');
    cy.get('[data-tests-id="expandIcon_9a5b5"]').click();

    //Clicks on "Total Artifacts Label"
    console.log('Click on Notified to display its Modal');
    cy.get('[data-tests-id="totalDistributionNotifiedArtifactsLabel"]').click();


    console.log('Make sure the Distributions are presented in the table');
    cy.get('[data-tests-id="distID_9a5b5_Modal"]').should('be.visible');

    console.log('Verify the Distribution details in the Distributions Table');
    cy.get('[data-tests-id="distID_9a5b5_Modal"]').should('have.text','9a5b5a9d-5d52-40df-bd62-fe71e7c01b85');

    console.log('Open the Modal details')
    cy.get('[data-tests-id="expandIcon_9a5b5_Modal"]').click()

    console.log('Verify Status Notify and its summary in the Modal');
    cy.get('[data-tests-id="modalStatusLabel"]').should('be.visible');
    cy.get('[data-tests-id="modalStatusLabel"]').should('have.text','Status NOTIFIED 1');


    console.log('Verify Component1 and Component2, 3, 4 & 5 and their summary in the Modal');
    cy.get('[data-tests-id="modalComponentLabel"]').should('be.visible');
    cy.get('[data-tests-id="modalComponentLabel"]').should('contains.text','Consumer1 1');
    cy.get('[data-tests-id="modalComponentLabel"]').should('contains.text','Consumer2 0');
    cy.get('[data-tests-id="modalComponentLabel"]').should('contains.text','Consumer3 0');
    cy.get('[data-tests-id="modalComponentLabel"]').should('contains.text','Consumer4 0');
    cy.get('[data-tests-id="modalComponentLabel"]').should('contains.text','Consumer5 0');
    cy.get('[data-tests-id="modalComponentLabel"]').should('contains.text','Consumer6 0');

    console.log('Open to display he component statuses');
    cy.get('[data-tests-id="expandIcon_Consumer1_ForModal"]').click();
    cy.get('[data-tests-id="expandIcon_compID_Consumer1_artifact1.env"]').click();

    console.log('Verify Status & TimeStamp');

    cy.get('[data-tests-id="time_Consumer1_artifact1.env"]').should('contain.text','7/25/19, 9:47 AM');
    cy.get('[data-tests-id="statusValue_NOTIFIED_Consumer1_artifact1.env"]').should('have.text','NOTIFIED');

    console.log('Close the Modal');
    cy.get('[data-tests-id="button-close"]').should('be.visible');
    cy.get('[data-tests-id="button-close"]').click();
});

it('Verify Mark As Deploy button appear for Designer', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
    cy.route('GET', '**/services/*/distribution', '@distributionList');

    const distributionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/distribution';
    cy.visit(distributionPageUrl);

    console.log('Make sure Mark As Deploy button Exist');
    cy.get('[class="btnMarkAsDistributed"]').should('be.visible');
});

it('Verify COMPONENT_DONE_OK and COMPONENT_DONE_ERROR appear for relevant components', function () {
    cy.route('GET', '**/services/*/filteredDataByParams?include=metadata', '@metadata');
    cy.route('GET', '**/services/*/distribution', '@distributionList');
    cy.route('GET', '**/services/distribution/9a5b5a9d-5d52-40df-bd62-fe71e7c01b85', '@distributionStatus');

    const distributionPageUrl = '#!/dashboard/workspace/' + this.metadata.metadata.uniqueId + '/service/distribution';
    cy.visit(distributionPageUrl);

    console.log('Click on specific DistributionID icon to display its status');
    cy.get('[data-tests-id="expandIcon_9a5b5"]').click();


    cy.get('[class="msoStatus green"]').should('contain.text','COMPONENT_DONE_OK');
    cy.get('[class="msoStatus red"]').should('contain.text','COMPONENT_DONE_ERROR');
});

});
