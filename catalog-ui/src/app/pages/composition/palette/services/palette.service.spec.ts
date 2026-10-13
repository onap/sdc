import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ISdcConfig, SdcConfigToken } from '../../../../config/sdc-config.config';
import { WorkspaceService } from '../../../workspace/workspace.service';
import { CompositionPaletteService } from './palette.service';
describe('palette component', () => {

    let service: CompositionPaletteService;

    const httpServiceMock: Partial<HttpClient> = {
        get: jest.fn()
    };

    const sdcConfigToken: Partial<ISdcConfig> = {
        api: {
            root: ''
        }
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [],
            providers: [CompositionPaletteService,
                {provide: HttpClient, useValue: httpServiceMock},
                {provide: SdcConfigToken, useValue: sdcConfigToken},
                {provide: WorkspaceService, useValue: {}}
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
