import {async, ComponentFixture} from "@angular/core/testing";
import {EnvParamsComponent} from "./env-params.component";
import {SdcUiServices, SdcUiCommon} from "onap-ui-angular";
import {ConfigureFn, configureTests} from "../../../../../jest/test-config.helper";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {ArtifactModel} from "../../../../models/artifacts";
import { CacheService } from '../../../services/cache.service';

describe('environment parameters component', () => {

    let fixture: ComponentFixture<EnvParamsComponent>;
    let popoverServiceMock: Partial<SdcUiServices.PopoverService>;
    let regexPatterns: any;

    let artifactModel = new ArtifactModel();

    let mockHeatParameters = [
        {currentValue: "1", defaultValue: null, description: "Description 1", empty:false, name: "Param1", ownerId: null, type: "string", uniqueId: null, envDisplayName:null, version: null, filterTerm:null},
        {currentValue: "2", defaultValue: null, description: "Description 2", empty:false, name: "Param2", ownerId: null, type: "string", uniqueId: null, envDisplayName:null, version: null, filterTerm:null},
        {currentValue: "3", defaultValue: null, description: "Description 3", empty:false, name: "Param3", ownerId: null, type: "string", uniqueId: null, envDisplayName:null, version: null, filterTerm:null}
        ];

    let keyboardEvent = new KeyboardEvent("keyup");

    beforeEach(
        async(() => {

            popoverServiceMock = {
                createPopOver : jest.fn()
            }

            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [EnvParamsComponent],
                    imports: [NgxDatatableModule],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        { provide: SdcUiServices.PopoverService, useValue: popoverServiceMock },
                        { provide: CacheService, useValue: { get: jest.fn() } }
                    ],
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(EnvParamsComponent);
            });
        })
    );

    it('should match current snapshot of env-params component', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('should clear CurrentValue for a given name in the heat parameter', () => {
         fixture.componentInstance.artifact = artifactModel;
         fixture.componentInstance.artifact.heatParameters = mockHeatParameters;
         expect(fixture.componentInstance.artifact.heatParameters.length).toBe(3);
         expect(fixture.componentInstance.artifact.heatParameters[0].currentValue).toBe("1");
         fixture.componentInstance.clearCurrentValue("Param1");
         expect(fixture.componentInstance.artifact.heatParameters[0].currentValue).toBe("");
    });

    it("should update filter heatParameters so there won''t be any value to be displayed", () => {

        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.artifact.heatParameters = mockHeatParameters;
        fixture.componentInstance.ngOnInit();

        let event = {
             target : {
                value: 'paramNotExist'
             }
        }

        expect(fixture.componentInstance.artifact.heatParameters.length).toBe(3);
        fixture.componentInstance.updateFilter(event);
        expect(fixture.componentInstance.artifact.heatParameters.length).toBe(0);
    });

    it("should update filter heatParameters so there will be only 1 value to be displayed", () => {

        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.artifact.heatParameters = mockHeatParameters;
        fixture.componentInstance.ngOnInit();

        let event = {
            target : {
                value: 'param1'
            }
        }

        expect(fixture.componentInstance.artifact.heatParameters.length).toBe(3);
        fixture.componentInstance.updateFilter(event);
        expect(fixture.componentInstance.artifact.heatParameters.length).toBe(1);
    });

});