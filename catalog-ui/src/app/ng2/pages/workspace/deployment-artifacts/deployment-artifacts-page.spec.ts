// import ' rxjs/add/observable/of';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture } from '@angular/core/testing';
import { NgxsModule, Store } from '@ngxs/store';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiServices } from 'onap-ui-angular';
import { Observable } from 'rxjs/Observable';
import { deploymentArtifactMock } from '../../../../../jest/mocks/artifacts-mock';
import { ConfigureFn, configureTests } from '../../../../../jest/test-config.helper';
import { ComponentMetadata } from '../../../../models/component-metadata';
import { ArtifactsService } from '../../../components/forms/artifacts-form/artifacts.service';
import { CacheService } from '../../../services/cache.service';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';
import { TranslateModule } from '../../../shared/translator/translate.module';
import { TranslateService } from '../../../shared/translator/translate.service';
import { ArtifactsState } from '../../../store/states/artifacts.state';
import { WorkspaceService } from '../workspace.service';
import { DeploymentArtifactsPageComponent } from './deployment-artifacts-page.component';
import {ModalService} from "../../../services/modal.service";

describe('deployment artifacts page', () => {

    let fixture: ComponentFixture<DeploymentArtifactsPageComponent>;
    let topologyTemplateServiceMock: Partial<TopologyTemplateService>;
    let workspaceServiceMock: Partial<WorkspaceService>;
    let loaderServiceMock: Partial<SdcUiServices.LoaderService>;
    let store: Store;

    beforeEach(
        async(() => {

            topologyTemplateServiceMock = {
                getArtifactsByType: jest.fn().mockImplementation((componentType, id, artifactType) => Observable.of(deploymentArtifactMock))
            };
            workspaceServiceMock = {
                metadata: <ComponentMetadata>{
                    uniqueId: 'service_unique_id',
                    componentType: 'SERVICE'
                }
            }

            loaderServiceMock = {
                activate: jest.fn(),
                deactivate: jest.fn()
            }
            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [DeploymentArtifactsPageComponent],
                    imports: [NgxDatatableModule, TranslateModule, NgxsModule.forRoot([ArtifactsState])],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: WorkspaceService, useValue: workspaceServiceMock},
                        {provide: TopologyTemplateService, useValue: topologyTemplateServiceMock},
                        {provide: SdcUiServices.LoaderService, useValue: loaderServiceMock},
                        {provide: ArtifactsService, useValue: {}},
                        {provide: SdcUiServices.PopoverService, useValue: {}},
                        {provide: CacheService, useValue: {}},
                        {provide: SdcUiServices.ModalService, useValue: {}},
                        {provide: ModalService, useValue: {}},
                        {provide: TranslateService, useValue: {}}
                    ],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(DeploymentArtifactsPageComponent);
                store = testBed.get(Store);
            });
        })
    );

    it('should match current snapshot of informational artifact pages component', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('should see exactly 2 tosca artifacts', () => {
        fixture.componentInstance.ngOnInit();
        fixture.componentInstance.deploymentArtifacts$.subscribe((artifacts) => {
            expect(artifacts.length).toEqual(8);
        })
        store.selectOnce((state) => state.artifacts.deploymentArtifacts).subscribe((artifacts) => {
            expect(artifacts.length).toEqual(8);
        });
    });

});
