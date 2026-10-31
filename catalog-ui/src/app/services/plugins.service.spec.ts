import * as _ from 'lodash';
(window as any)._ = _;

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {HTTP_INTERCEPTORS, HttpResponse} from '@angular/common/http';
import {SdcUiServices} from 'onap-ui-angular';
import {PluginsService} from './plugins.service';
import {SdcConfigToken} from '../config/sdc-config.config';
import {mockSdcConfig} from '../../jest/mocks/sdc-config.mock';
import {Plugin, PluginsConfiguration} from 'app/models/plugins-config';
import {HeadersInterceptor} from '../interceptors/headers-interceptor';
import {CookieService} from './cookie.service';
import {HttpHelperService} from './http-helper.service';
import {TranslateService} from '../shared/translator/translate.service';

describe('PluginsService', () => {
    let service: PluginsService;
    let http: HttpTestingController;
    let modals: {openErrorDetailModal: jest.Mock, openWarningModal: jest.Mock};

    beforeEach(() => {
        modals = {openErrorDetailModal: jest.fn(), openWarningModal: jest.fn()};
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                PluginsService,
                {provide: HTTP_INTERCEPTORS, useClass: HeadersInterceptor, multi: true},
                {provide: CookieService, useValue: {getUserIdSuffix: () => 'USER_ID', getUserId: () => 'cs0008'}},
                {provide: HttpHelperService, useValue: {getUuidValue: () => ''}},
                {provide: SdcUiServices.ModalService, useValue: modals},
                {provide: TranslateService, useValue: {translate: jest.fn()}},
                {provide: SdcConfigToken, useValue: mockSdcConfig}
            ]
        });

        service = TestBed.get(PluginsService);
        http = TestBed.get(HttpTestingController);
    });

    afterEach(() => http.verify());

    it('requests the plugin status and returns the parsed boolean body', () => {
        const result = jest.fn();
        const status = service.isPluginOnline('workflow');
        http.expectNone(mockSdcConfig.api.no_proxy_root + mockSdcConfig.api.GET_plugin_online_state.replace(':pluginId', 'workflow'));
        const subscription = status.subscribe(result);
        const request = http.expectOne(mockSdcConfig.api.no_proxy_root + mockSdcConfig.api.GET_plugin_online_state.replace(':pluginId', 'workflow'));
        expect(request.request.method).toBe('GET');
        request.event(new HttpResponse({body: true}));
        expect(result).toHaveBeenCalledWith(true);
        subscription.unsubscribe();
    });

    it('reports an unavailable plugin when the status request fails', () => {
        const result = jest.fn();
        service.isPluginOnline('workflow').subscribe(result);
        http.expectOne(mockSdcConfig.api.no_proxy_root + mockSdcConfig.api.GET_plugin_online_state.replace(':pluginId', 'workflow'))
            .flush('unavailable', {status: 503, statusText: 'Unavailable'});
        expect(result).toHaveBeenCalledWith(false);
    });

    it('does not show a global error modal for an unavailable plugin', () => {
        service.isPluginOnline('workflow').subscribe();
        http.expectOne(mockSdcConfig.api.no_proxy_root + mockSdcConfig.api.GET_plugin_online_state.replace(':pluginId', 'workflow'))
            .flush('unavailable', {status: 503, statusText: 'Unavailable'});
        expect(modals.openErrorDetailModal).not.toHaveBeenCalled();
        expect(modals.openWarningModal).not.toHaveBeenCalled();
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
