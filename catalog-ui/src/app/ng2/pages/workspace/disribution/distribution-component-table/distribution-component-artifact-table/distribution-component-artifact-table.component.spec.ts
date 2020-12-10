import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture } from '@angular/core/testing';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
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
          {timeStamp: '7/25/2019 12:48AM', status: 'DEPLOY_OK', errorReason: 'Sample message 1'},
          {timeStamp: '7/25/2019 12:48AM', status: 'DOWNLOAD_OK', errorReason: 'Sample message 2'},
          {timeStamp: '7/25/2019 12:48AM', status: 'NOTIFIED', errorReason: 'Sample message 3'}
        ],
        url: 'URL1',
      },
      {
        name: 'Artifact2',
        statuses: [
          {timeStamp: '7/26/2019 12:48AM', status: 'STATUS_TO_DISPLAY', errorReason: 'null'},
          {timeStamp: '7/25/2019 12:48AM', status: 'DOWNLOAD_OK'},
          {timeStamp: '7/25/2019 12:48AM', status: 'NOTIFIED'}
        ],
        url: 'URL2',
      },
      {
        name: 'Artifact3',
        statuses: [
          {timeStamp: '12/12/2020 13:30AM', status: 'DEPLOY_OK', errorReason: null},
          {timeStamp: '12/11/2020 13:20AM', status: 'DOWNLOAD_OK', errorReason: 'Sample error reason'}
        ],
        url: 'URL3',
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

  it('Get Latest Artifact (status, timeStamp, errorReason) - So the Component Table will display the last time stamp of the notification', async () => {
    await fixture.componentInstance.ngOnInit();
    expect(fixture.componentInstance.getLatestArtifact('Artifact2')).toEqual({timeStamp: '7/26/2019 12:48AM', status: 'STATUS_TO_DISPLAY', errorReason: ''});
    expect(fixture.componentInstance.getLatestArtifact('ArtifactWithNoStatuses')).toEqual(null);
  });

  it('Get Latest Artifact (status, timeStamp, errorReason) - So the Component Table will display correct Message', async () => {
    await fixture.componentInstance.ngOnInit();
    expect(fixture.componentInstance.getLatestArtifact('Artifact1').errorReason).toEqual('Sample message 1');
    expect(fixture.componentInstance.getLatestArtifact('Artifact2').errorReason).toEqual('');
    expect(fixture.componentInstance.getLatestArtifact('Artifact3').errorReason).toEqual('');
  });

  it('Once the Distribution Component Artifact Table Component is created - artifacts will keep the relevant artifacts for a specific distributionID and Component Name', async () => {
    await fixture.componentInstance.ngOnInit();
    // tslint:disable:no-string-literal
    expect(fixture.componentInstance.artifacts.length).toBe(4);
    expect(fixture.componentInstance.artifacts[0].name).toBe('Artifact1');
    expect(fixture.componentInstance.artifacts[0].url).toBe('URL1');
    expect(fixture.componentInstance.artifacts[0].statuses.length).toBe(3);

    expect(fixture.componentInstance.artifacts[1].name).toBe('Artifact2');
  });

  it('Once the Distribution Component Artifact Table Component is created for Modal- artifacts will keep the relevant artifacts for a ' +
      'specific distributionID and Component Name filtered by Status', async () => {
    fixture.componentInstance.statusFilter = 'DOWNLOAD_OK';
    await fixture.componentInstance.ngOnInit();
    expect(fixture.componentInstance.artifacts.length).toBe(4);
    expect(fixture.componentInstance.artifacts[0].name).toBe('Artifact1');
    expect(fixture.componentInstance.artifacts[0].url).toBe('URL1');

    expect(fixture.componentInstance.artifacts[0].statuses.length).toBe(1);
    expect(fixture.componentInstance.artifacts[0].statuses[0]).toEqual({status: 'DOWNLOAD_OK', timeStamp: '7/25/2019 12:48AM', errorReason: 'Sample message 2'});

  });

});
