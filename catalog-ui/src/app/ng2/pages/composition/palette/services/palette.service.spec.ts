import {TestBed} from "@angular/core/testing";
import {CompositionPaletteService} from "./palette.service";
import {ISdcConfig, SdcConfigToken} from "../../../../config/sdc-config.config";
import {WorkspaceService} from "../../../../pages/workspace/workspace.service";
import { HttpClient } from "@angular/common/http";
describe('palette component', () => {

    let service: CompositionPaletteService;

    let httpServiceMock: Partial<HttpClient> = {
        get: jest.fn()
    }

    let sdcConfigToken: Partial<ISdcConfig> = {
        "api": {
            "root": ''
        }
    }

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [],
            providers: [CompositionPaletteService,
                {provide: HttpClient, useValue: httpServiceMock},
                {provide: SdcConfigToken, useValue: sdcConfigToken},
				{provide: WorkspaceService, useValue{}}
            ]
        });

        service = TestBed.get(CompositionPaletteService);
    });

    it('should create an instance', () => {
        expect(service).toBeDefined();
    });

    // it('should create an instance2', async () => {
    //     expect(await service.subscribeToLeftPaletteElements("resources")).toEqual([]);
    // });
});

