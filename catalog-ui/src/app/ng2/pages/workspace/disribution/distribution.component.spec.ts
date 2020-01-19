import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture } from '@angular/core/testing';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiServices } from 'onap-ui-angular';
import { ConfigureFn, configureTests } from '../../../../../jest/test-config.helper';
import { ComponentMetadata } from '../../../../models/component-metadata';
import { AuthenticationService } from '../../../services/authentication.service';
import { WorkspaceService } from '../workspace.service';
import { DistributionComponent } from './distribution.component';
import { DistributionService } from './distribution.service';
import {EventListenerService} from "../../../../services/event-listener-service";

describe('DistributionComponent', () => {
  let fixture: ComponentFixture<DistributionComponent>;
  let distibutionServiceMock: Partial<DistributionService>;
  let workspaceServiceMock: Partial<WorkspaceService>;
  let loaderServiceMock: Partial<SdcUiServices.LoaderService>;
  let authenticationServiceMock: Partial <AuthenticationService>;
  let eventListenerService: Partial <EventListenerService>;

  const mockDistributionListFromService = [
    {
      deployementStatus: 'Distributed',
      distributionID: '1',
      timestamp: '2019-07-21 08:37:02.834 UTC',
      userId: 'Aretha Franklin(op0001)'
    }, {
      deployementStatus: 'Distributed',
      distributionID: '2',
      timestamp: '2019-07-21 09:37:02.834 UTC',
      userId: 'Aretha Franklin(op0001)'
    }];

  beforeEach(() => {

    distibutionServiceMock = {
      initDistributionsList: jest.fn(),
      getDistributionList: jest.fn().mockReturnValue(mockDistributionListFromService),
      initDistributionsStatusForDistributionID: jest.fn()
    };

    const componentMetadata = new ComponentMetadata();
    componentMetadata.uuid = '111';

    workspaceServiceMock = {
      metadata : componentMetadata
    };

    authenticationServiceMock = {
      getLoggedinUser: jest.fn().mockReturnValue({role: 'designer'})
    };

    eventListenerService = {
      registerObserverCallback: jest.fn(),
      unRegisterObserver: jest.fn()
    }

    loaderServiceMock = {
      activate: jest.fn(),
      deactivate: jest.fn()
    };

    const configure: ConfigureFn = (testBed) => {
      testBed.configureTestingModule({
        declarations: [DistributionComponent],
        imports: [NgxDatatableModule],
        schemas: [NO_ERRORS_SCHEMA],
        providers: [
          {provide: DistributionService, useValue: distibutionServiceMock},
          {provide: WorkspaceService, useValue: workspaceServiceMock},
          {provide: SdcUiServices.LoaderService, useValue: loaderServiceMock},
          {provide: AuthenticationService, useValue: authenticationServiceMock},
          {provide: EventListenerService, useValue: eventListenerService}
        ],
      });
    };

    configureTests(configure).then((testBed) => {
      fixture = testBed.createComponent(DistributionComponent);
    });

  });

  it('Once the Distribution Component is created - distributionsResponseFromServer save all the distributions from the Service', async () => {
    fixture.componentInstance.componentUuid = 'componentUid';
    fixture.componentInstance.rowDistributionID = null;
    fixture.componentInstance.isModal = false;

    await fixture.componentInstance.ngOnInit();
    expect(fixture.componentInstance.distributions.length).toBe(2);
  });
});
