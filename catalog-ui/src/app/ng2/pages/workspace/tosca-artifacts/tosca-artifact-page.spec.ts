import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {ToscaArtifactPageComponent} from "./tosca-artifact-page.component";
import {ConfigureFn, configureTests} from "../../../../../jest/test-config.helper";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {WorkspaceService} from "../workspace.service";
import {SdcUiServices} from "onap-ui-angular";
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {Observable} from "rxjs/Observable";
import {ComponentMetadata} from "../../../../models/component-metadata";
import 'rxjs/add/observable/of';
import {NgxsModule, Store} from "@ngxs/store";
import {ArtifactsState} from "../../../store/states/artifacts.state";
import {toscaArtifactMock} from "../../../../../jest/mocks/artifacts-mock";

describe('tosca artifacts page', () => {

    let fixture: ComponentFixture<ToscaArtifactPageComponent>;
    let topologyTemplateServiceMock: Partial<TopologyTemplateService>;
    let workspaceServiceMock: Partial<WorkspaceService>;
    let loaderServiceMock: Partial<SdcUiServices.LoaderService>;
    let store: Store;


    beforeEach(
        async(() => {

            topologyTemplateServiceMock = {
                getArtifactsByType: jest.fn().mockImplementation((componentType, id, artifactType) => Observable.of(toscaArtifactMock))
            };
            workspaceServiceMock = {metadata: <ComponentMetadata>{uniqueId: 'service_unique_id', componentType: 'SERVICE'}}

            loaderServiceMock = {
                activate : jest.fn(),
                deactivate: jest.fn()
            }
            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [ToscaArtifactPageComponent],
                    imports: [NgxDatatableModule, NgxsModule.forRoot([ArtifactsState])],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: WorkspaceService, useValue: workspaceServiceMock},
                        {provide: TopologyTemplateService, useValue: topologyTemplateServiceMock},
                        {provide: SdcUiServices.LoaderService, useValue: loaderServiceMock }
                    ],
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(ToscaArtifactPageComponent);
                store = testBed.get(Store);
            });
        })
    );

    it('should match current snapshot of tosca artifact pages component', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('should see exactly 2 tosca artifacts', () => {
        fixture.componentInstance.ngOnInit();
        fixture.componentInstance.toscaArtifacts$.subscribe((artifacts)=> {
            expect(artifacts.length).toEqual(2);
        })
        store.selectOnce(state => state.artifacts.toscaArtifacts).subscribe(artifacts => {
            expect(artifacts.length).toEqual(9);
        });
    })

});