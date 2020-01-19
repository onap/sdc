import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import { NO_ERRORS_SCHEMA} from "@angular/core";
import {ConfigureFn, configureTests} from "../../../../../jest/test-config.helper";

import {Observable} from "rxjs/Observable";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {SdcUiServices, SdcUiCommon} from "onap-ui-angular";
import 'rxjs/add/observable/of';
import {OnboardingService} from "../../../services/onboarding.service";
import {TranslateService} from "../../../shared/translator/translate.service";
import {CacheService} from "../../../services/cache.service";
import {FileUtilsService} from "../../../services/file-utils.service";
import {onboardingModalVSPMock, onboardingModalUniqueVSPMock, vspFromServerMock} from "../../../../../jest/mocks/onboarding-vsp.mock";
import {OnboardingModalComponent} from "./onboarding-modal.component";
import {TranslatePipe} from "../../../shared/translator/translate.pipe";

describe('onboarding modal component', () => {

    let fixture: ComponentFixture<OnboardingModalComponent>;
    let onboardingServiceMock: Partial<OnboardingService>;
    let translateServiceMock: Partial<TranslateService>;
    let cacheServiceMock: Partial<CacheService>;
    let fileUtilsServiceMock: Partial<FileUtilsService>;
    let popoverServiceMock: Partial<SdcUiServices.PopoverService>;
    let loaderServiceMock: Partial<SdcUiServices.LoaderService>;

    beforeEach(
        async(() => {

            onboardingServiceMock = {
                getOnboardingComponents: jest.fn().mockImplementation(()=>  Observable.of(onboardingModalUniqueVSPMock)),
                getComponentFromCsarUuid: jest.fn().mockImplementation(()=>  Observable.of(vspFromServerMock))
            };

            cacheServiceMock = {
                set: jest.fn()
            };

            loaderServiceMock = {
                activate: jest.fn(),
                deactivate: jest.fn()
            }


            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [OnboardingModalComponent, TranslatePipe],
                    imports: [NgxDatatableModule],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        { provide: OnboardingService, useValue: onboardingServiceMock },
                        { provide: TranslateService, useValue: translateServiceMock },
                        { provide: CacheService, useValue: cacheServiceMock },
                        { provide: FileUtilsService, useValue: fileUtilsServiceMock },
                        { provide: SdcUiServices.PopoverService, useValue: popoverServiceMock },
                        { provide: SdcUiServices.LoaderService, useValue: loaderServiceMock }
                    ],
                });
            };
            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(OnboardingModalComponent);
            });
        })
    );

    /*it('should match current snapshot of onboarding modal component', () => {
        expect(fixture).toMatchSnapshot();
    });*/

    it('should see exactly 2 vsp in onboarding modal and call initOnboardingComponentsList', () => {
        fixture.componentInstance.initOnboardingComponentsList();
        expect(fixture.componentInstance.componentsMetadataList.length).toBe(2);
    });

    it('should see exactly 1 vsp in onboarding modal and call initOnboardingComponentsList', () => {
        fixture.componentInstance.currentCsarUUID = "6348841e79a64871ba064ce340a968a4";
        fixture.componentInstance.initOnboardingComponentsList();
        expect(fixture.componentInstance.componentsMetadataList.length).toBe(1);
    });

    it('when get a list of vsp initMaxVersionOfItemsInList will return a list with unique items with the latest versions for each packageId', () => {
        onboardingServiceMock.getOnboardingComponents = jest.fn().mockImplementation(() => Observable.of(onboardingModalVSPMock));
        fixture.componentInstance.initOnboardingComponentsList();
        expect(fixture.componentInstance.componentsMetadataList.length).toBe(2);
    });

    it('should filter out 1 vsp when searching and call updateFilter function', () => {
        fixture.componentInstance.initOnboardingComponentsList();
        let event = {
            target : {
                value : 'test new vsp'
            }
        }

        expect(fixture.componentInstance.componentsMetadataList.length).toBe(2);
        fixture.componentInstance.updateFilter(event);
        expect(fixture.componentInstance.componentsMetadataList.length).toBe(1);
    });

    it('When select the selected vsp the row details closed and call onSelectComponent function', () => {
        fixture.componentInstance.initOnboardingComponentsList();
        fixture.componentInstance.onSelectComponent({selected: []});
        expect(fixture.componentInstance.selectedComponent).toEqual(undefined);
        expect(fixture.componentInstance.componentFromServer).toEqual(undefined);
    });

    it('When select vsp a row with its details will be opened and call onSelectComponent function', () => {
        fixture.componentInstance.initOnboardingComponentsList();
        fixture.componentInstance.onSelectComponent({selected: onboardingModalVSPMock});
        expect(fixture.componentInstance.selectedComponent).not.toEqual(null);
        expect(fixture.componentInstance.componentFromServer).not.toEqual(undefined);
        expect(fixture.componentInstance.isCsarComponentExists).toEqual(true);
    });
    it('When select new vsp a row with import and download buttons will be opened and call onSelectComponent function', () => {
        fixture.componentInstance.initOnboardingComponentsList();
        onboardingServiceMock.getComponentFromCsarUuid.mockImplementation(() => Observable.of(undefined));
        fixture.componentInstance.onSelectComponent({selected: onboardingModalVSPMock});
        expect(fixture.componentInstance.selectedComponent).not.toEqual(null);
        expect(fixture.componentInstance.componentFromServer).toEqual(undefined);
        expect(fixture.componentInstance.isCsarComponentExists).toEqual(false);
    });
});
