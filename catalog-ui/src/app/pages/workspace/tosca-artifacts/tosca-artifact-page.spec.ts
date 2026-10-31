import {of, Observable} from 'rxjs';
import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {ToscaArtifactPageComponent} from "./tosca-artifact-page.component";
import {ConfigureFn, configureTests} from "../../../../jest/test-config.helper";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {WorkspaceService} from "../workspace.service";
import {SdcUiServices} from "onap-ui-angular";
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {ComponentMetadata} from "../../../models/component-metadata";

import {NgxsModule, Store} from "@ngxs/store";
import {ArtifactsState} from "../../../store/states/artifacts.state";
import {toscaArtifactMock} from "../../../../jest/mocks/artifacts-mock";
import { ComponentService } from "app/services/component-services/component.service";

describe('tosca artifacts page', () => {

    let fixture: ComponentFixture<ToscaArtifactPageComponent>;
    let topologyTemplateServiceMock: Partial<TopologyTemplateService>;
    let workspaceServiceMock: Partial<WorkspaceService>;
    let store: Store;


    beforeEach(
        async(() => {

            topologyTemplateServiceMock = {
                getArtifactsByType: jest.fn().mockImplementation((componentType, id, artifactType) => of(toscaArtifactMock))
            };
            workspaceServiceMock = {metadata: <ComponentMetadata>{uniqueId: 'service_unique_id', componentType: 'SERVICE'}}

            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [ToscaArtifactPageComponent],
                    imports: [NgxDatatableModule, NgxsModule.forRoot([ArtifactsState])],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: WorkspaceService, useValue: workspaceServiceMock},
                        {provide: TopologyTemplateService, useValue: topologyTemplateServiceMock},
                        {provide: ComponentService, useValue: {}},
                        {provide: SdcUiServices.NotificationsService, useValue: {push: jest.fn()}}
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

    it('resolves the table after view initialization and expands clicked artifact rows', () => {
        fixture.detectChanges();
        const table = fixture.componentInstance.table;
        expect(table).toBeDefined();
        const toggleExpandRow = jest.spyOn(table.rowDetail, 'toggleExpandRow');
        const row = toscaArtifactMock[0];
        fixture.componentInstance.onActivate({type: 'click', row});
        expect(toggleExpandRow).toHaveBeenCalledWith(row);
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
describe('tosca artifacts page template', () => {
    // tslint:disable-next-line:no-var-requires
    const template: string = require('./tosca-artifact-page.component.html');
    const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

    it('offers Upload only while the workspace is editable', () => {
        const upload = doc.querySelector('upload-artifact');
        expect(upload.closest('ngx-datatable-column').getAttribute('*ngif')).toBe('isService() && !(isViewOnly$ | async)');
    });
});
