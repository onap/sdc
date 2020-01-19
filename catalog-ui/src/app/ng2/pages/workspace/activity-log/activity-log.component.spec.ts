import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture } from '@angular/core/testing';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiServices } from 'onap-ui-angular';
import 'rxjs/add/observable/of';
import { Observable } from 'rxjs/Observable';
import { ConfigureFn, configureTests } from '../../../../../jest/test-config.helper';
import { ComponentMetadata } from '../../../../models/component-metadata';
import { ActivityLogService } from '../../../services/activity-log.service';
import { WorkspaceService } from '../workspace.service';
import { ActivityLogComponent } from './activity-log.component';

describe('activity log component', () => {

    let fixture: ComponentFixture<ActivityLogComponent>;
    let activityLogServiceMock: Partial<ActivityLogService>;
    let workspaceServiceMock: Partial<WorkspaceService>;
    let loaderServiceMock: Partial<SdcUiServices.LoaderService>;
    let componentMetadataMock: ComponentMetadata;

    const mockLogs = '[' +
        '{"MODIFIER":"Carlos Santana(m08740)","COMMENT":"comment","STATUS":"200","ACTION":"Checkout","TIMESTAMP":"2018-11-19 13:00:02.388 UTC"},' +
        '{"MODIFIER":"John Doe(m08741)","COMMENT":"comment","STATUS":"200","ACTION":"Checkin","TIMESTAMP":"2018-11-20 13:00:02.388 UTC"},' +
        '{"MODIFIER":"Jane Doe(m08742)","COMMENT":"comment","STATUS":"200","ACTION":"Checkout","TIMESTAMP":"2018-11-21 13:00:02.388 UTC"}' +
        ']';

    beforeEach(
        async(() => {

            componentMetadataMock =  new ComponentMetadata();
            componentMetadataMock.uniqueId = 'fake';
            componentMetadataMock.componentType = 'SERVICE';

            activityLogServiceMock = {
                getActivityLog : jest.fn().mockImplementation((type, id) => Observable.of(JSON.parse(mockLogs)) )
            };

            workspaceServiceMock = {
                metadata : componentMetadataMock
            };

            loaderServiceMock = {
                activate : jest.fn(),
                deactivate: jest.fn()
            };

            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [ActivityLogComponent],
                    imports: [NgxDatatableModule],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        { provide: WorkspaceService, useValue: workspaceServiceMock },
                        { provide: ActivityLogService, useValue: activityLogServiceMock },
                        { provide: SdcUiServices.LoaderService, useValue: loaderServiceMock }
                        ],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(ActivityLogComponent);
            });
        })
    );

    it('should see exactly 3 activity logs', () => {
        fixture.componentInstance.ngOnInit();
        expect(fixture.componentInstance.activities.length).toBe(3);
    });

    it('should filter out 1 element when searching', () => {
        fixture.componentInstance.ngOnInit();

        const event = {
            target : {
                value : 'Checkin'
            }
        };

        expect(fixture.componentInstance.activities.length).toBe(3);
        fixture.componentInstance.updateFilter(event);
        expect(fixture.componentInstance.activities.length).toBe(1);
    });
});
