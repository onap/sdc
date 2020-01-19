
import { SdcConfigToken, ISdcConfig } from "../../config/sdc-config.config";
import { SdcMenuToken, IAppMenu } from "../../config/sdc-menu.config";


import { async, ComponentFixture, TestBed } from "@angular/core/testing";
import { HomeComponent } from "./home.component";
import {ConfigureFn, configureTests} from "../../../../jest/test-config.helper";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import { TranslateService } from "../../shared/translator/translate.service";
import { HomeService, CacheService, AuthenticationService, ImportVSPService } from '../../../../app/services-ng2';
import { ModalsHandler } from "../../../../app/utils";
import { SdcUiServices } from "onap-ui-angular";
import {ComponentType, ResourceType} from "../../../utils/constants";
import { FoldersMenu, FoldersItemsMenu, FoldersItemsMenuGroup } from './folders';
import { HomeFilter } from "../../../../app/models/home-filter";
import {Component} from "../../../models/components/component";




describe('home component', () => {

    // const mockedEvent = <MouseEvent>{ target: {} }
    let fixture: ComponentFixture<HomeComponent>;
    // let eventServiceMock: Partial<EventListenerService>;

    let importVspService: Partial<ImportVSPService>;
    let mockStateService;
    let modalServiceMock :Partial<SdcUiServices.ModalService>;
    let translateServiceMock : Partial<TranslateService>;
    let foldersItemsMenuMock;
    let homeFilterMock :Partial<HomeFilter>;
    let foldersMock;
    let loaderServiceMock;


    beforeEach(
        async(() => {
            modalServiceMock = {
                openWarningModal: jest.fn()
            }

            mockStateService = {
                // go: jest.fn().mockReturnValue( new Promise.resolve((resolve, reject )=> resolve()))
                go: jest.fn()
            }

            translateServiceMock = {
                translate: jest.fn()
            }

            homeFilterMock = {
                search: jest.fn,
                toUrlParam: jest.fn()
            }

            foldersMock = {
                setSelected: jest.fn()
            }

            loaderServiceMock = {
                activate: jest.fn(),
                deactivate: jest.fn()
            }

            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [HomeComponent],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: SdcConfigToken, useValue: {"csarFileExtension":"csar", "toscaFileExtension":"yaml,yml"}},
                        {provide: SdcMenuToken, useValue: {}},
                        {provide: "$state", useValue: mockStateService},
                        {provide: HomeService, useValue: {}},
                        {provide: AuthenticationService, useValue: {}},
                        {provide: CacheService, useValue: {}},
                        {provide: TranslateService, useValue: translateServiceMock},
                        {provide: ModalsHandler, useValue: {}},
                        {provide: SdcUiServices.ModalService, useValue: modalServiceMock},
                        {provide: SdcUiServices.LoaderService, useValue: loaderServiceMock},
                        {provide: ImportVSPService, useValue: {}}
                    ],
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(HomeComponent);
            });
        })
    );


    it('should match current snapshot', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('should call on home component openCreateModal with null imported file', () => {
        const component = TestBed.createComponent(HomeComponent);
        let componentType:string = 'test';
        let importedFile:any = null;
        component.componentInstance.openCreateModal(componentType, importedFile);
        expect(mockStateService.go).toBeCalledWith('workspace.general', {type: componentType.toLowerCase()});
    });


    it('should call on home component openCreateModal with imported file', () => {
        const component = TestBed.createComponent(HomeComponent);
        component.componentInstance.initEntities = jest.fn();
        let componentType:string = 'test';
        let importedFile:any = 'importedFile';
        component.componentInstance.openCreateModal(componentType, importedFile);
        expect(component.componentInstance.initEntities).toBeCalledWith(true);
    });


    it ('should call on home component onImportVf without file without extension', () => {
        const component = TestBed.createComponent(HomeComponent);
        let file:any = {filename : 'test'};
        let expectedTitle:string = translateServiceMock.translate("NEW_SERVICE_RESOURCE_ERROR_VALID_CSAR_EXTENSIONS_TITLE");
        let expectedMessage:string = translateServiceMock.translate("NEW_SERVICE_RESOURCE_ERROR_VALID_CSAR_EXTENSIONS", {"csarFileExtension":"csar"});
        component.componentInstance.onImportVf(file);
        expect(modalServiceMock.openWarningModal).toBeCalledWith(expectedTitle, expectedMessage , 'error-invalid-csar-ext');
    });


    it ('should call on home component onImportVf with file without extension' , () => {
        const component = TestBed.createComponent(HomeComponent);
        let file:any = {filename : 'test.csar'};
        component.componentInstance.onImportVf(file);
        expect(mockStateService.go).toBeCalledWith('workspace.general', {
            type: ComponentType.RESOURCE.toLowerCase(),
                importedFile: file,
                resourceType: ResourceType.VF
        });
    });


    it ('should call on home component onImportVfc without file without extension', () => {
        const component = TestBed.createComponent(HomeComponent);
        let file:any = {filename : 'test'};
        let expectedTitle:string = translateServiceMock.translate("NEW_SERVICE_RESOURCE_ERROR_VALID_TOSCA_EXTENSIONS_TITLE");
        let expectedMessage:string = translateServiceMock.translate("NEW_SERVICE_RESOURCE_ERROR_VALID_TOSCA_EXTENSIONS", {"toscaFileExtension":"yaml,yml"});
        component.componentInstance.onImportVfc(file);
        expect(modalServiceMock.openWarningModal).toBeCalledWith(expectedTitle, expectedMessage , 'error-invalid-tosca-ext');
    });

    it ('should call on home component onImportVfc with file without extension' , () => {
        const component = TestBed.createComponent(HomeComponent);
        let file:any = {filename : 'test.yml'};
        component.componentInstance.onImportVfc(file);
        expect(mockStateService.go).toBeCalledWith('workspace.general', {
            type: ComponentType.RESOURCE.toLowerCase(),
            importedFile: file,
            resourceType: ResourceType.VFC
        });
    });

    it ('should call on home component createPNF' , () => {
        const component = TestBed.createComponent(HomeComponent);
        component.componentInstance.createPNF();
        expect(mockStateService.go).toBeCalledWith('workspace.general', {
            type: ComponentType.RESOURCE.toLowerCase(),
            resourceType: ResourceType.PNF
        });
    });

    it ('should call on home component createCR' , () => {
        const component = TestBed.createComponent(HomeComponent);
        component.componentInstance.createCR();
        expect(mockStateService.go).toBeCalledWith('workspace.general', {
            type: ComponentType.RESOURCE.toLowerCase(),
            resourceType: ResourceType.CR
        });
    });


    it ('should call on home component updateFilter' , () => {
        const component = TestBed.createComponent(HomeComponent);
        component.componentInstance.homeFilter = homeFilterMock;
        component.componentInstance.filterHomeItems = jest.fn();
        component.componentInstance.updateFilter();

        expect(mockStateService.go).toBeCalledWith('.', homeFilterMock.toUrlParam(), {location: 'replace', notify: false});
        // expect(spy).toHaveBeenCalledTimes(1);

        // let spy = spyOn(homeFilterMock, 'toUrlParam').and.returnValue({
        //     'filter.term': '',
        //     'filter.distributed': '',
        //     'filter.status':''
        // });
    });

    // it ('should call on home component setSelectedFolder' , () => {
    //     const component = TestBed.createComponent(HomeComponent);
    //     let folderItem:Partial<FoldersItemsMenu> = { text:'someThing'};
    //     let folderItem1:number;
    //     component.componentInstance.folders = foldersMock;
    //     expect(foldersMock.setSelected).toBeCalledWith(folderItem);
    // });

    // it ('should call on home component goToComponent' , () => {
    //     const component = TestBed.createComponent(HomeComponent);
    //     let componentParam:Partial<Component> = { uuid:'someThing', uniqueId:'uniqueID', componentType:'componentType'};
    //     component.componentInstance.goToComponent(componentParam);
    //     expect(loaderServiceMock.activate).toHaveBeenCalled();
    //     // expect(mockStateService.go).toBeCalledWith('workspace.general', {id: componentParam.uniqueId, type: componentParam.componentType.toLowerCase()}).then(function(){
    //     //     loaderServiceMock.deactivate();
    //     // });
    //     expect(mockStateService.go).toBeCalled();
    // });

    // it ('should call on home component raiseNumberOfElementToDisplay so numberOfItemToDisplay will be 0' , () => {
    //     const component = TestBed.createComponent(HomeComponent);
    //     component.componentInstance.raiseNumberOfElementToDisplay();
    //     expect(component.componentInstance.numberOfItemToDisplay).toEqual(0);
    // });
    //
    // it ('should call on home component raiseNumberOfElementToDisplay with min(2,70) so numberOfItemToDisplay will be 2' , () => {
    //     const component = TestBed.createComponent(HomeComponent);
    //     component.componentInstance.homeItems = ['item1', 'item2'];
    //     component.componentInstance.numberOfItemToDisplay = 70;
    //     component.componentInstance.raiseNumberOfElementToDisplay(true);
    //     expect(component.componentInstance.numberOfItemToDisplay).toEqual(2);
    // });
    //
    // it ('should call on home component raiseNumberOfElementToDisplay with min(3,35) so numberOfItemToDisplay will be 2 after fullPagesAmount is calculated' , () => {
    //     const component = TestBed.createComponent(HomeComponent);
    //     component.componentInstance.homeItems = ['item1', 'item2', 'item3'];
    //     component.componentInstance.numberOfItemToDisplay = 70;
    //     component.componentInstance.numberOfItemToDisplay = 0;
    //     component.componentInstance.raiseNumberOfElementToDisplay(false);
    //     expect(component.componentInstance.numberOfItemToDisplay).toEqual(3);
    // });
    //
    //
    // it ('should call on home component changeFilterTerm' , () => {
    //     const component = TestBed.createComponent(HomeComponent);
    //     component.componentInstance.changeFilterTerm("testStr");
    //     // expect ( "testStr" ).toEqual(homeFilterMock.search.)
    // });






    // it ('should call on home component entitiesCount' , () => {
    //     const component = TestBed.createComponent(HomeComponent);
    //     component.componentInstance.entitiesCount("aaa");
    //     expect(mockStateService.go).toBeCalledWith('workspace.general', {
    //         type: ComponentType.RESOURCE.toLowerCase(),
    //         resourceType: ResourceType.CR
    //     });
    // });


    // it('should call on home component notificationIconCallback', () => {
    //     const component = TestBed.createComponent(HomeComponent);
    //     component.componentInstance.initEntities = jest.fn();
    //     component.componentInstance.notificationIconCallback();
    //     expect(mockStateService.go).toBeCalledWith('workspace.general', {});
    // });





});