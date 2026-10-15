import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture } from '@angular/core/testing';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiServices } from 'onap-ui-angular';
import { ConfigureFn, configureTests } from '../../../../../jest/test-config.helper';
import { DistributionService } from '../distribution.service';
import { DistributionComponentTableComponent } from './distribution-component-table.component';

describe('DistributionComponentTableComponent', () => {
  let fixture: ComponentFixture<DistributionComponentTableComponent>;
  let distibutionServiceMock: Partial<DistributionService>;

  const mockComponentsForDistribution = ['Consumer1', 'Consumer2'];

  beforeEach(() => {

    distibutionServiceMock = {
      getComponentsByDistributionID: jest.fn().mockReturnValue(mockComponentsForDistribution),
      getArtifactstByDistributionIDAndComponentsName: jest.fn(),
      getArtifactsForDistributionIDAndComponentByStatus: jest.fn()
    };

    const configure: ConfigureFn = (testBed) => {
      testBed.configureTestingModule({
        declarations: [DistributionComponentTableComponent],
        imports: [NgxDatatableModule],
        schemas: [NO_ERRORS_SCHEMA],
        providers: [
          {provide: DistributionService, useValue: distibutionServiceMock},
          {provide: SdcUiServices.ModalService, useValue: {}}
        ],
      });
    };

    configureTests(configure).then((testBed) => {
      fixture = testBed.createComponent(DistributionComponentTableComponent);
    });

  });

  it('Once the Distribution Component Table Component is created - components will keep the relevant components for a specific distributionID', async () => {
    await fixture.componentInstance.ngOnInit();
    expect(fixture.componentInstance.components.length).toBe(2);
    expect(fixture.componentInstance.components[0]).toBe('Consumer1');
    expect(fixture.componentInstance.components[1]).toBe('Consumer2');
  });
});

describe('DistributionComponentTableComponent template', () => {
  // tslint:disable-next-line:no-var-requires
  const template: string = require('./distribution-component-table.component.html');
  const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

  it('opens each status count from a native button', () => {
    const counts = Array.from(doc.querySelectorAll('.distributionSummary [\\(click\\)]'));
    expect(counts.map((el) => el.getAttribute('data-tests-id'))).toEqual([
      'totalDistributionNotifiedArtifactsLabel', 'totalDistributionDownloadedArtifactsLabel',
      'totalDistributionDeployedArtifactsLabel', 'totalDistributionNotNotifiedArtifactsLabel',
      'totalDistributionDeployErrorArtifactsLabel', 'totalDistributionDownloadErrorArtifactsLabel']);
    counts.forEach((el) => {
      expect(el.tagName).toBe('BUTTON');
      expect(el.getAttribute('type')).toBe('button');
    });
  });
});
