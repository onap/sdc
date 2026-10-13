import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { NgxsModule, Store } from '@ngxs/store';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiServices } from 'onap-ui-angular';
import 'rxjs/add/observable/of';
import { Observable } from 'rxjs/Observable';
import { informationalArtifactsMock } from '../../../../jest/mocks/artifacts-mock';
import { ConfigureFn, configureTests } from '../../../../jest/test-config.helper';
import { ArtifactsService } from '../../../components/forms/artifacts-form/artifacts.service';
import { ComponentMetadata } from '../../../models/component-metadata';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';
import { ArtifactsState } from '../../../store/states/artifacts.state';
import { WorkspaceService } from '../workspace.service';
import { InformationArtifactPageComponent } from './information-artifact-page.component';

describe('informational artifacts page', () => {

    let fixture: ComponentFixture<InformationArtifactPageComponent>;
    let topologyTemplateServiceMock: Partial<TopologyTemplateService>;
    let workspaceServiceMock: Partial<WorkspaceService>;
    let loaderServiceMock: Partial<SdcUiServices.LoaderService>;
    let store: Store;

    beforeEach(
        async(() => {

            topologyTemplateServiceMock = {
                getArtifactsByType: jest.fn().mockImplementation((componentType, id, artifactType) => Observable.of(informationalArtifactsMock))
            };
            workspaceServiceMock = {metadata: {uniqueId: 'service_unique_id', componentType: 'SERVICE'} as ComponentMetadata};

            loaderServiceMock = {
                activate : jest.fn(),
                deactivate: jest.fn()
            };
            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [InformationArtifactPageComponent],
                    imports: [NgxDatatableModule, NgxsModule.forRoot([ArtifactsState])],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: WorkspaceService, useValue: workspaceServiceMock},
                        {provide: TopologyTemplateService, useValue: topologyTemplateServiceMock},
                        {provide: SdcUiServices.LoaderService, useValue: loaderServiceMock },
                        {provide: ArtifactsService, useValue: {}},
                    ],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(InformationArtifactPageComponent);
                store = testBed.get(Store);
            });
        })
    );

    it('should match current snapshot of informational artifact pages component', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('should see exactly 3 informational artifacts and six buttons to add artifact by template', () => {
        fixture.componentInstance.ngOnInit();
        fixture.componentInstance.informationArtifacts$.subscribe((artifacts) => {
            expect(artifacts.length).toEqual(3);
        });
        fixture.componentInstance.informationArtifactsAsButtons$.subscribe((artifacts) => {
            expect(artifacts.length).toEqual(6);
        });

        store.selectOnce((state) => state.artifacts.artifacts).subscribe((artifacts) => {
            expect(artifacts.length).toEqual(9);
        });
    });

});
