import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture } from '@angular/core/testing';
import { PluginsConfiguration } from 'app/models';
import { Observable } from 'rxjs';
import { Mock } from 'ts-mockery';
import { ConfigureFn, configureTests } from '../../../../../jest/test-config.helper';
import { MenuItem, MenuItemGroup } from '../../../../utils/menu-handler';
import { SdcConfigToken } from '../../../config/sdc-config.config';
import { AuthenticationService } from '../../../services/authentication.service';
import { TranslateModule } from '../../../shared/translator/translate.module';
import { TranslateService } from '../../../shared/translator/translate.service';
import { TopNavComponent } from './top-nav.component';

describe('artifact form component', () => {

    let fixture: ComponentFixture<TopNavComponent>;
    let translateServiceMock: Partial<TranslateService>;
    let mockStateService;
    let authServiceMock;

    const designerUser = {
        email: 'designer@sdc.com',
        firstName: 'Carlos',
        fullName: 'Carlos Santana',
        lastLoginTime: 1555587266566,
        lastName: 'Santana',
        role: 'DESIGNER',
        status: 'ACTIVE',
        userId: 'cs0008'
    };

    const pluginDisplayOptions = {
        displayName : '',
        displayContext : new Array<string>(),
        displayRoles :  new Array<string>()
    };

    let roleToReturn = designerUser;

    const map1 =  new Map();
    map1.otherValue =  pluginDisplayOptions;

    const map2 = new Map();
    pluginDisplayOptions.displayRoles = ['DESIGNER'];
    pluginDisplayOptions.displayName = 'DCAE-DS';
    map2.tab =  pluginDisplayOptions;

    PluginsConfiguration.plugins =
        [
            {pluginId: 'DCAED', pluginDiscoveryUrl: 'DCAED_discoveryURL', pluginSourceUrl: 'DCAED_sourceURL', pluginStateUrl: 'DCAED_stateURL', pluginDisplayOptions:  map1, isOnline: true},
            {pluginId: 'DCAE-DS', pluginDiscoveryUrl: 'DCAE-DS_discoveryURL', pluginSourceUrl: 'DCAE-DS_sourceURL', pluginStateUrl: 'DCAE-DS_stateURL', pluginDisplayOptions: map2, isOnline: true}
        ];

    beforeEach(
        async(() => {
            authServiceMock = {
                getLoggedinUser: jest.fn().mockImplementation(() => {
                    return roleToReturn;
                })
            };

            mockStateService = {
                go: jest.fn(),
                includes: jest.fn(() => true),
                current : {
                    name : 'plugins'
                },
                params : {}
            };

            translateServiceMock = {
                languageChangedObservable: Mock.of<Observable<string>>( {
                    subscribe : jest.fn().mockImplementation((cb) => {
                        cb();
                    })
                }),
                translate: jest.fn((str: string) => {
                    if (str === 'TOP_MENU_HOME_BUTTON') {
                        return 'HOME';
                    } else if (str === 'TOP_MENU_CATALOG_BUTTON') {
                        return 'CATALOG';
                    } else if (str === 'TOP_MENU_ON_BOARD_BUTTON') {
                        return 'ONBOARD';
                    } else {
                        return 'TBD...';
                    }
                })
            };

            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [TopNavComponent],
                    imports: [TranslateModule],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: TranslateService, useValue: translateServiceMock},
                        {provide: '$state', useValue: mockStateService},
                        {provide: AuthenticationService, useValue: authServiceMock},
                        {provide: SdcConfigToken, useValue: {csarFileExtension: 'csar', toscaFileExtension: 'yaml,yml'}},
                    ],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(TopNavComponent);
            });
        })
    );

    it('should match current snapshot of top-nav component', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('Once a Designer logged in, Menu Items will contain HOME, CATALOG, ONBOARD & DCAE-DS; HOME will be selected', () => {

        // topLvlSelectedIndex = 1 => Ignore the inner call to  _getTopLvlSelectedIndexByState.
        fixture.componentInstance.topLvlSelectedIndex = 0;
        fixture.componentInstance.ngOnInit();

        expect(fixture.componentInstance.topLvlMenu.itemClick).toBe(true);

        expect(fixture.componentInstance.topLvlMenu.menuItems.length).toBe(4);

        expect(fixture.componentInstance.topLvlMenu.menuItems[0]).toEqual({
            action: 'goToState',
            blockedForTypes: null,
            callback: null,
            params: null,
            state: 'dashboard',
            text: 'HOME'
        });
        expect(fixture.componentInstance.topLvlMenu.menuItems[1]).toEqual({
            action: 'goToState',
            blockedForTypes: null,
            callback: null,
            params: null,
            state: 'catalog',
            text: 'CATALOG'
        });
        expect(fixture.componentInstance.topLvlMenu.menuItems[2]).toEqual({
            action: 'goToState',
            blockedForTypes: null,
            callback: null,
            params: null,
            state: 'onboardVendor',
            text: 'ONBOARD'
        });
        expect(fixture.componentInstance.topLvlMenu.menuItems[3]).toEqual({
            action: 'goToState',
            blockedForTypes: null,
            callback: null,
            params:
                {path: 'DCAE-DS_stateURL'},
            state: 'plugins',
            text: 'DCAE-DS'
        });

        expect(fixture.componentInstance.topLvlMenu.selectedIndex).toBe(0);
    });

});
