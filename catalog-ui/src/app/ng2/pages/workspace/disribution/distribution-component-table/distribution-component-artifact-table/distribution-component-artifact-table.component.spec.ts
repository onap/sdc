import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture } from '@angular/core/testing';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiServices } from 'onap-ui-angular';
import { ConfigureFn, configureTests } from '../../../../../../../jest/test-config.helper';
import { DistributionService } from '../../distribution.service';
import { DistributionComponentArtifactTableComponent } from './distribution-component-artifact-table.component';

describe('DistributionComponentArtifactTableComponent', () => {
  let fixture: ComponentFixture<DistributionComponentArtifactTableComponent>;
  let distibutionServiceMock: Partial<DistributionService>;

  const mockArtifactsForDistributionAndComponentName = [
      {
        name: 'Artifact1',
        statuses: [
          {timeStamp: '7/25/2019 12:48AM', status: 'DEPLOY_OK'},
          {timeStamp: '7/25/2019 12:48AM', status: 'DOWNLOAD_OK'},
          {timeStamp: '7/25/2019 12:48AM', status: 'NOTIFIED'}
        ],
        url: 'URL1',
      },
      {
        name: 'Artifact2',
        statuses: [
          {timeStamp: '7/26/2019 12:48AM', status: 'STATUS_TO_DISPLAY'},
          {timeStamp: '7/25/2019 12:48AM', status: 'DOWNLOAD_OK'},
          {timeStamp: '7/25/2019 12:48AM', status: 'NOTIFIED'}
        ],
        url: 'URL2',
      },
      {
        name: 'ArtifactWithNoStatuses',
        url: 'URL2',
      }
    ];

  beforeEach(() => {

    distibutionServiceMock = {
      getArtifactstByDistributionIDAndComponentsName: jest.fn().mockReturnValue(mockArtifactsForDistributionAndComponentName),
    };

    const configure: ConfigureFn = (testBed) => {
      testBed.configureTestingModule({
        declarations: [DistributionComponentArtifactTableComponent],
        imports: [NgxDatatableModule],
        schemas: [NO_ERRORS_SCHEMA],
        providers: [
          {provide: DistributionService, useValue: distibutionServiceMock}
        ],
      });
    };

    configureTests(configure).then((testBed) => {
      fixture = testBed.createComponent(DistributionComponentArtifactTableComponent);
    });

  });

  it('Get Latest Artifact (status and timeStamp) - So the Component Table will display the last time stamp of the notification', async () => {
    await fixture.componentInstance.ngOnInit();
    expect(fixture.componentInstance.getLatestArtifact('Artifact2')).toEqual({status: 'STATUS_TO_DISPLAY', timeStamp: '7/26/2019 12:48AM'});
    expect(fixture.componentInstance.getLatestArtifact('ArtifactWithNoStatuses')).toEqual(null);
  });

  it('Once the Distribution Component Artifact Table Component is created - artifacts will keep the relevant artifacts for a specific distributionID and Component Name', async () => {
    await fixture.componentInstance.ngOnInit();
    // tslint:disable:no-string-literal
    expect(fixture.componentInstance.artifacts.length).toBe(3);
    expect(fixture.componentInstance.artifacts[0].name).toBe('Artifact1');
    expect(fixture.componentInstance.artifacts[0].url).toBe('URL1');
    expect(fixture.componentInstance.artifacts[0].statuses.length).toBe(3);

    expect(fixture.componentInstance.artifacts[1].name).toBe('Artifact2');
  });

  it('Once the Distribution Component Artifact Table Component is created for Modal- artifacts will keep the relevant artifacts for a ' +
      'specific distributionID and Component Name filtered by Status', async () => {
    fixture.componentInstance.statusFilter = 'DOWNLOAD_OK';
    await fixture.componentInstance.ngOnInit();
    expect(fixture.componentInstance.artifacts.length).toBe(3);
    expect(fixture.componentInstance.artifacts[0].name).toBe('Artifact1');
    expect(fixture.componentInstance.artifacts[0].url).toBe('URL1');

    expect(fixture.componentInstance.artifacts[0].statuses.length).toBe(1);
    expect(fixture.componentInstance.artifacts[0].statuses[0]).toEqual({status: 'DOWNLOAD_OK', timeStamp: '7/25/2019 12:48AM'});

  });
});
