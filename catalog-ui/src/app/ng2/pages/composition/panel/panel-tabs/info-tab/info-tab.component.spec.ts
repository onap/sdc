import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Store } from '@ngxs/store';
import { CompositionPaletteService } from '../../../../../pages/composition/palette/services/palette.service';
import { IAppMenu, SdcMenuToken } from '../../../../../../../app/ng2/config/sdc-menu.config';
import { CompositionService } from '../../../../../pages/composition/composition.service';
import { ServiceServiceNg2 } from '../../../../../../../app/services-ng2';
import { WorkspaceService } from '../../../../../../../app/ng2/pages/workspace/workspace.service';
import { ComponentInstanceServiceNg2 } from '../../../../../../../app/ng2/services/component-instance-services/component-instance.service';
import { EventListenerService } from '../../../../../../../app/services';
import { InfoTabComponent } from './info-tab.component';
import { ConfigureFn, configureTests } from "../../../../../../../jest/test-config.helper";
import { Observable } from "rxjs";
import { leftPaletteElements } from "../../../../../../../jest/mocks/left-paeltte-elements.mock";
import { TranslatePipe } from "../../../../../shared/translator/translate.pipe";
import { HttpClientModule } from "@angular/common/http";
import { TranslateModule } from "../../../../../../../app/ng2/shared/translator/translate.module";
import _ from "lodash";
import { TranslateService } from "../../../../../shared/translator/translate.service";
import { SdcUiServices } from "onap-ui-angular";
import { Component as TopologyTemplate, FullComponentInstance, ComponentInstance } from '../../../../../../../app/models';


describe('InfoTabComponent', () => {
    // let comp: InfoTabComponent;
    let fixture: ComponentFixture<InfoTabComponent>;

    // let eventServiceMock: Partial<EventListenerService>;
    let storeStub:Partial<Store>;
    let compositionPaletteServiceStub:Partial<CompositionPaletteService>;
    let iAppMenuStub:Partial<IAppMenu>;
    let compositionServiceStub:Partial<CompositionService>;
    let serviceServiceNg2Stub:Partial<ServiceServiceNg2>;
    let workspaceServiceStub:Partial<WorkspaceService>;
    let componentInstanceServiceNg2Stub:Partial<ComponentInstanceServiceNg2>;
    let eventListenerServiceStub:Partial<EventListenerService>;

    beforeEach(
        async(() => {
            storeStub = {};
            iAppMenuStub = {};
            eventListenerServiceStub = {
                notifyObservers: jest.fn()
            }
            compositionPaletteServiceStub = {
                getLeftPaletteElements:  jest.fn().mockImplementation(()=>  Observable.of(leftPaletteElements))
            }
            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    imports: [ ],
                    declarations: [ InfoTabComponent, TranslatePipe ],
                    schemas: [ NO_ERRORS_SCHEMA ],
                    providers: [
                        { provide: Store, useValue: {} },
                        { provide: CompositionPaletteService, useValue: compositionPaletteServiceStub },
                        { provide: SdcMenuToken, useValue: {} },
                        { provide: CompositionService, useValue: {} },
                        { provide: SdcUiServices.ModalService, useValue: {}},
                        { provide: ServiceServiceNg2, useValue: {} },
                        { provide: WorkspaceService, useValue: {} },
                        { provide: ComponentInstanceServiceNg2, useValue: {} },
                        { provide: EventListenerService, useValue: eventListenerServiceStub },
                        { provide: TranslateService, useValue: {}}
                    ]
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(InfoTabComponent);
                let comp = fixture.componentInstance;

            });
        })
    );


    it('can load instance', () => {
        expect(fixture).toMatchSnapshot();
    });

    describe('Version dropdown', () => {
        it('is undefined for topologyTemplate', () => {
            fixture.componentInstance.component = <TopologyTemplate>{};
            fixture.componentInstance.initEditResourceVersion(fixture.componentInstance.component, fixture.componentInstance.flatLeftPaletteElementsFromService(leftPaletteElements));
            expect(fixture.componentInstance.versions).toBe(undefined);
        });
        it('does not contain the highest minor version if it is checked out', () => {
            fixture.componentInstance.component = new ComponentInstance();
            fixture.componentInstance.component.allVersions =  
            {'1.0': "9c829122-af05-4bc9-b537-5d84f4c8ae25", '1.1': "930d56cb-868d-4e35-bd0f-e737d2fdb171"};
            fixture.componentInstance.component.version = "1.0";
            fixture.componentInstance.component.uuid = "a8cf015e-e4e5-4d4b-a01e-8624e8d36095";
            fixture.componentInstance.initEditResourceVersion(fixture.componentInstance.component, fixture.componentInstance.flatLeftPaletteElementsFromService(leftPaletteElements));
            expect(fixture.componentInstance.versions).toHaveLength(1);
        });
    });

});
