import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture } from '@angular/core/testing';
import {PluginsConfiguration} from 'app/models/plugins-config';
import { Observable } from 'rxjs';
import { Mock } from 'ts-mockery';
import { ConfigureFn, configureTests } from '../../../../jest/test-config.helper';
import { MenuItem, MenuItemGroup } from '../../../utils/menu-handler';
import { SdcConfigToken } from '../../../config/sdc-config.config';
import { AuthenticationService } from '../../../services/authentication.service';
import { TranslateModule } from '../../../shared/translator/translate.module';
import { TranslateService } from '../../../shared/translator/translate.service';
import { TopNavComponent } from './top-nav.component';
import { NavigationService } from '../../../services/navigation.service';

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
                        {provide: NavigationService, useValue: {navigate: mockStateService.go, getParams: () => mockStateService.params, getCurrentStateName: () => mockStateService.current.name, includes: mockStateService.includes}},
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

    it('renders the top-level items as links to their route', () => {
        fixture.componentInstance.topLvlSelectedIndex = 0;
        fixture.detectChanges();
        const links = Array.from(fixture.nativeElement.querySelectorAll('ul.top-menu > li > a'));
        expect(links.map((a: HTMLAnchorElement) => a.getAttribute('href')))
            .toEqual(['#!/dashboard', '#!/catalog', '#!/onboardVendor', '#!/plugins/DCAE-DS_stateURL']);
        expect(links.map((a: HTMLAnchorElement) => a.getAttribute('data-tests-id')))
            .toEqual(['main-menu-button-home', 'main-menu-button-catalog', 'main-menu-button-onboard', 'main-menu-button-dcae-ds']);
    });

    it('has no href for items that run a callback', () => {
        const item = new MenuItem('VF: x', () => Promise.resolve(true), null, null, []);
        expect(fixture.componentInstance.menuItemHref(item)).toBeNull();
        expect(fixture.componentInstance.menuItemHref(undefined)).toBeNull();
    });

    it('routes a plain link click through menuItemClick and leaves modified clicks to the browser', () => {
        const comp = fixture.componentInstance;
        const group = new MenuItemGroup(0, [new MenuItem('HOME', null, 'dashboard', 'goToState')]);
        comp.menuItemClick = jest.fn();
        const plain = {button: 0, preventDefault: jest.fn()} as any;
        comp.onMenuLinkClick(plain, group, group.menuItems[0]);
        expect(plain.preventDefault).toHaveBeenCalled();
        expect(comp.menuItemClick).toHaveBeenCalledWith(group, group.menuItems[0]);

        const withCtrl = {button: 0, ctrlKey: true, preventDefault: jest.fn()} as any;
        comp.onMenuLinkClick(withCtrl, group, group.menuItems[0]);
        expect(withCtrl.preventDefault).not.toHaveBeenCalled();
        expect(comp.menuItemClick).toHaveBeenCalledTimes(1);
    });

    it('renders breadcrumbs without a route and the sub-menu items as buttons', () => {
        const comp = fixture.componentInstance;
        const crumbs = new MenuItemGroup(0, [new MenuItem('VF: x', () => Promise.resolve(true), null, null, []),
            new MenuItem('VF: y', () => Promise.resolve(true), null, null, [])]);
        crumbs.menuItems[1].isDisabled = true;
        comp.menuModel = [crumbs];
        comp.ngOnChanges({menuModel: {}});
        fixture.detectChanges();
        const el: HTMLElement = fixture.nativeElement;
        expect(el.querySelector('[data-tests-id="breadcrumbs-button-0"]').tagName).toBe('A');
        expect(el.querySelector('[data-tests-id="breadcrumbs-button-1"]').tagName).toBe('BUTTON');
        const subItems = Array.from(el.querySelectorAll('ul.sub-menu [data-tests-id^="sub-menu-button-vf"]'));
        expect(subItems.map((b: HTMLButtonElement) => b.tagName + ':' + b.disabled)).toEqual(['BUTTON:false', 'BUTTON:true']);
    });

});
