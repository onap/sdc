import * as _ from 'lodash';
(window as any)._ = _;

import {TestBed} from '@angular/core/testing';
import {HttpModule, Http, BaseRequestOptions} from '@angular/http';
import {MockBackend} from '@angular/http/testing';
import {PluginsService} from './plugins.service';
import {SdcConfigToken} from '../config/sdc-config.config';
import {mockSdcConfig} from '../../../jest/mocks/sdc-config.mock';
import {Plugin, PluginsConfiguration} from 'app/models';

describe('PluginsService', () => {
    let service: PluginsService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpModule],
            providers: [
                PluginsService,
                {provide: SdcConfigToken, useValue: mockSdcConfig},
                MockBackend,
                BaseRequestOptions,
                {
                    provide: Http,
                    useFactory: (backend, options) => new Http(backend, options),
                    deps: [MockBackend, BaseRequestOptions]
                }
            ]
        });

        service = TestBed.get(PluginsService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('isPluginDisplayedInContext', () => {
        it('should return true when role and context match', () => {
            const plugin = {
                pluginDisplayOptions: {
                    context: {
                        displayRoles: ['DESIGNER', 'ADMIN'],
                        displayContext: ['SERVICE', 'VF']
                    }
                }
            } as any;

            expect(service.isPluginDisplayedInContext(plugin, 'DESIGNER', 'SERVICE')).toBe(true);
        });

        it('should return false when role does not match', () => {
            const plugin = {
                pluginDisplayOptions: {
                    context: {
                        displayRoles: ['ADMIN'],
                        displayContext: ['SERVICE']
                    }
                }
            } as any;

            expect(service.isPluginDisplayedInContext(plugin, 'DESIGNER', 'SERVICE')).toBe(false);
        });

        it('should return false when context type does not match', () => {
            const plugin = {
                pluginDisplayOptions: {
                    context: {
                        displayRoles: ['DESIGNER'],
                        displayContext: ['SERVICE']
                    }
                }
            } as any;

            expect(service.isPluginDisplayedInContext(plugin, 'DESIGNER', 'VF')).toBe(false);
        });

        it('should return false when context display options are missing', () => {
            const plugin = {
                pluginDisplayOptions: {}
            } as any;

            expect(service.isPluginDisplayedInContext(plugin, 'DESIGNER', 'SERVICE')).toBeFalsy();
        });
    });

    describe('getPluginByStateUrl', () => {
        it('should return the plugin matching the state URL', () => {
            const testPlugin = {pluginStateUrl: 'test-state', pluginId: 'test'};
            PluginsConfiguration.plugins = {'test': testPlugin} as any;

            const result = service.getPluginByStateUrl('test-state');
            expect(result).toBe(testPlugin);
        });

        it('should return undefined for unknown state URL', () => {
            PluginsConfiguration.plugins = {} as any;
            const result = service.getPluginByStateUrl('nonexistent');
            expect(result).toBeUndefined();
        });
    });
});
