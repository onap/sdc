
import { async, ComponentFixture, TestBed } from "@angular/core/testing";
import {ConfigureFn, configureTests} from "../../../../jest/test-config.helper";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import { CacheService} from "../../../../app/services-ng2";
import {CatalogComponent} from "./catalog.component";
import {  SdcUiServices } from "onap-ui-angular";
import { SdcConfigToken } from "../../config/sdc-config.config";
import { SdcMenuToken} from "../../config/sdc-menu.config";
import { ResourceNamePipe } from "../../pipes/resource-name.pipe";
import { CatalogService } from "../../services/catalog.service";
import {TranslatePipe} from "../../shared/translator/translate.pipe";
import {TranslateService} from "../../shared/translator/translate.service";
import {Observable} from "rxjs";
import {LoaderService} from "onap-ui-angular/dist/loader/loader.service";
import {categoriesElements} from "../../../../jest/mocks/categories.mock";
import {sdcMenu} from "../../../../jest/mocks/sdc-menu.mock";
import {IEntityFilterObject} from "../../pipes/entity-filter.pipe";





describe('catalog component', () => {

    let fixture: ComponentFixture<CatalogComponent>;

    //Data variables
    let catalogSelectorItemsMock;
    let checkListModelMock;
    let filterParamsMock;
    let checkboxesFilterMock;
    let checkboxesFilterKeysMock;


    //Service variables
    let stateServiceMock;
    let cacheServiceMock: Partial<CacheService>;
    let loaderServiceMock: Partial<LoaderService>;
    let catalogServiceMock: Partial<CatalogService>;


    beforeEach(

        async(() => {
            console.info = jest.fn();
            catalogSelectorItemsMock = [
                {
                    value: 0,
                    title: 'Active Items',
                    header: 'Active'
                },
                {
                    value: 1,
                    title: 'Archive',
                    header: 'Archived'
                }
            ];
            checkListModelMock = {
                checkboxes: [
                    {label: "VF", disabled: false, isChecked: false, testId: "checkbox-vf", value: "Resource.VF"},
                    {label: "VFC", disabled: false, isChecked: false, testId: "checkbox-vfc", value: "Resource.VFC",
                        subLevelChecklist: {checkboxes:[{label: "VFD", disabled: false, isChecked: false, testId: "checkbox-vfd", value: "Resource.VFD"}],
                        selectedValues: ["Resource.VFD"]}
                        },
                    {label: "CR", disabled: false, isChecked: false, testId: "checkbox-cr", value: "Resource.CR",
                        subLevelChecklist: { checkboxes:[{label: "VF", disabled: false, isChecked: false, testId: "checkbox-vf", value: "Resource.VF"}],
                        selectedValues: []}
                    }],
                selectedValues: ["Resource.VF"]
            }
            filterParamsMock = {
                active: true,
                categories: ["resourceNewCategory.allotted resource.allotted resource", "resourceNewCategory.allotted resource.contrail route", "resourceNewCategory.application l4+.application server"],
                components: ["Resource.VF", "Resource.VFC"],
                order:  ["lastUpdateDate", true],
                statuses: ["inDesign"],
                models: ["test"],
                term: "Vf"
            }
            checkboxesFilterMock = {
                selectedCategoriesModel: ["serviceNewCategory.network l4+", "resourceNewCategory.allotted resource.allotted resource"],
                selectedComponentTypes: ["Resource.VF", "Resource.VFC"],
                selectedResourceSubTypes: ["VF", "VFC"],
                selectedStatuses: ["NOT_CERTIFIED_CHECKOUT", "NOT_CERTIFIED_CHECKIN"],
                selectedModels: ["test"]
            };
            checkboxesFilterKeysMock = {
                categories:{_main: ["serviceNewCategory.network l4+"]},
                componentTypes: { Resource: ["Resource.VF", "Resource.VFC"], _main: ["Resource.VFC"]},
                statuses: {_main: ["inDesign"]},
                models: {_main: ["test"]}
            }

            stateServiceMock = {
                go: jest.fn(),
                current: jest.fn()
            };
            cacheServiceMock = {
                get: jest.fn().mockImplementation(()=> categoriesElements),
                set: jest.fn(),
                contains: jest.fn().mockImplementation(()=> true)
            };
            loaderServiceMock = {
                activate: jest.fn(),
                deactivate: jest.fn()
            };
            catalogServiceMock = {
                //TODO create mock function of archive
                getCatalog: jest.fn().mockImplementation(()=>  Observable.of(categoriesElements)),
                getArchiveCatalog: jest.fn().mockImplementation(()=>  Observable.of(categoriesElements))
            };
            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [CatalogComponent, TranslatePipe],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: SdcConfigToken, useValue: {}},
                        {provide: SdcMenuToken, useValue: sdcMenu},
                        {provide: "$state", useValue: stateServiceMock },
                        {provide: CacheService, useValue: cacheServiceMock },
                        {provide: CatalogService, useValue: catalogServiceMock },
                        {provide: ResourceNamePipe, useValue: {}},
                        {provide: SdcUiServices.LoaderService, useValue: loaderServiceMock },
                        {provide: TranslateService, useValue: {}}
                    ],
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(CatalogComponent);
            });
        })
    );


    it('should match current snapshot of catalog component', () => {
        expect(fixture).toMatchSnapshot();
    });

    it ('should call on catalog component onInit' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.initGui = jest.fn();
        component.componentInstance.initLeftSwitch = jest.fn();
        component.componentInstance.initScopeMembers = jest.fn();
        component.componentInstance.loadFilterParams = jest.fn();
        component.componentInstance.initCatalogData = jest.fn();
        component.componentInstance.ngOnInit();
        expect(component.componentInstance.initGui).toHaveBeenCalled();
        expect(component.componentInstance.initLeftSwitch).toHaveBeenCalled();
        expect(component.componentInstance.initScopeMembers).toHaveBeenCalled();
        expect(component.componentInstance.loadFilterParams).toHaveBeenCalled();
        expect(component.componentInstance.initCatalogData).toHaveBeenCalled();
    });

    it ('should call on catalog component initLeftSwitch' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.initLeftSwitch();
        expect(component.componentInstance.showCatalogSelector).toEqual(false);
        expect(component.componentInstance.catalogSelectorItems).toEqual(catalogSelectorItemsMock);
        expect(component.componentInstance.selectedCatalogItem).toEqual(catalogSelectorItemsMock[0]);
    });

    it ('should call on catalog component initCatalogData and selectedCatalogItem is archive ' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.getArchiveCatalogItems = jest.fn();
        component.componentInstance.selectedCatalogItem = catalogSelectorItemsMock[1];
        component.componentInstance.initCatalogData();
        expect(component.componentInstance.getArchiveCatalogItems).toHaveBeenCalled();
    });

    it ('should call on catalog component initCatalogData and selectedCatalogItem is active ' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.getActiveCatalogItems = jest.fn();
        component.componentInstance.selectedCatalogItem = catalogSelectorItemsMock[0];
        component.componentInstance.initCatalogData();
        expect(component.componentInstance.getActiveCatalogItems).toHaveBeenCalled();
    });

    it ('should call on catalog component initScopeMembers' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.makeSortedCategories = jest.fn().mockImplementation(()=> categoriesElements);
        component.componentInstance.initCategoriesMap = jest.fn();
        component.componentInstance.initCheckboxesFilter = jest.fn();
        component.componentInstance.initCheckboxesFilterKeys = jest.fn();
        component.componentInstance.buildCheckboxLists = jest.fn();
        component.componentInstance.initScopeMembers();
        expect(component.componentInstance.numberOfItemToDisplay).toEqual(0);
        expect(component.componentInstance.categories).toEqual(categoriesElements);
        expect(component.componentInstance.confStatus).toEqual(component.componentInstance.sdcMenu.statuses);
        expect(component.componentInstance.expandedSection).toEqual( ["type", "category", "status", "model"]);
        expect(component.componentInstance.catalogItems).toEqual([]);
        expect(component.componentInstance.search).toEqual({FilterTerm: ""});
        expect(component.componentInstance.initCategoriesMap).toHaveBeenCalled();
        expect(component.componentInstance.initCheckboxesFilter).toHaveBeenCalled();
        expect(component.componentInstance.initCheckboxesFilterKeys).toHaveBeenCalled();
        expect(component.componentInstance.buildCheckboxLists).toHaveBeenCalled();
        expect(component.componentInstance.version).toEqual(categoriesElements);
        expect(component.componentInstance.sortBy).toEqual('lastUpdateDate');
        expect(component.componentInstance.reverse).toEqual(true);
    });

    it ('should call on catalog component buildCheckboxLists ' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.buildChecklistModelForTypes = jest.fn();
        component.componentInstance.buildChecklistModelForCategories = jest.fn();
        component.componentInstance.buildChecklistModelForStatuses = jest.fn();
		component.componentInstance.buildChecklistModelForModels = jest.fn();
        component.componentInstance.buildCheckboxLists();
        expect(component.componentInstance.buildChecklistModelForTypes).toHaveBeenCalled();
        expect(component.componentInstance.buildChecklistModelForCategories).toHaveBeenCalled();
        expect(component.componentInstance.buildChecklistModelForStatuses).toHaveBeenCalled();
        expect(component.componentInstance.buildChecklistModelForModels).toHaveBeenCalled();
    });

    it ('should call on catalog component getTestIdForCheckboxByText ' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        let testId = component.componentInstance.getTestIdForCheckboxByText("catalog filter");
        expect(testId).toEqual("checkbox-catalogfilter");
    });

    it ('should call on catalog component selectLeftSwitchItem with active catalog' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.selectedCatalogItem = catalogSelectorItemsMock[1];
        component.componentInstance.getActiveCatalogItems = jest.fn();
        component.componentInstance.changeFilterParams = jest.fn();
        component.componentInstance.selectLeftSwitchItem(catalogSelectorItemsMock[0]);
        expect(component.componentInstance.getActiveCatalogItems).toBeCalledWith(true);
        expect(component.componentInstance.changeFilterParams).toBeCalledWith({"active": true});
    });

    it ('should call on catalog component selectLeftSwitchItem with archive catalog' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.selectedCatalogItem = catalogSelectorItemsMock[0];
        component.componentInstance.getArchiveCatalogItems = jest.fn();
        component.componentInstance.changeFilterParams = jest.fn();
        component.componentInstance.selectLeftSwitchItem(catalogSelectorItemsMock[1]);
        expect(component.componentInstance.getArchiveCatalogItems).toBeCalledWith(true);
        expect(component.componentInstance.changeFilterParams).toBeCalledWith({"active": false});
    });

    it ('should call on catalog component buildChecklistModelForTypes' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.checkboxesFilterKeys = checkboxesFilterKeysMock;
        component.componentInstance.buildChecklistModelForTypes();
        expect(component.componentInstance.componentTypes).toEqual({ Resource: ['VF', 'VFC', 'CR', 'PNF', 'CP', 'VL'],
            Service: null, TOSCA_Type: ["Data Type"]})
        expect(component.componentInstance.typesChecklistModel.checkboxes.length).toEqual(3);
    });

    it ('should call on catalog component buildChecklistModelForCategories' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.checkboxesFilterKeys = checkboxesFilterKeysMock;
        component.componentInstance.categories = categoriesElements;
        component.componentInstance.buildChecklistModelForCategories();
        expect(component.componentInstance.categoriesChecklistModel.checkboxes).not.toEqual(null);
    });

    it ('should call on catalog component buildChecklistModelForStatuses' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.checkboxesFilterKeys = checkboxesFilterKeysMock;
        component.componentInstance.categories = categoriesElements;
        component.componentInstance.confStatus = sdcMenu.statuses;
        component.componentInstance.buildChecklistModelForStatuses();
        expect(component.componentInstance.statusChecklistModel.checkboxes.length).toEqual(3);
    });

    it ('should call on catalog component initCheckboxesFilter' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.initCheckboxesFilter();
        expect(component.componentInstance.checkboxesFilter.selectedComponentTypes).toEqual([]);
        expect(component.componentInstance.checkboxesFilter.selectedResourceSubTypes).toEqual([]);
        expect(component.componentInstance.checkboxesFilter.selectedModels).toEqual([]);
        expect(component.componentInstance.checkboxesFilter.selectedStatuses).toEqual([]);
    });

    it ('should call on catalog component initCheckboxesFilterKeys' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.initCheckboxesFilterKeys();
        expect(component.componentInstance.checkboxesFilterKeys.componentTypes).toEqual({ _main: [] });
        expect(component.componentInstance.checkboxesFilterKeys.categories).toEqual({ _main: [] });
        expect(component.componentInstance.checkboxesFilterKeys.statuses).toEqual({ _main: [] });
        expect(component.componentInstance.checkboxesFilterKeys.models).toEqual({ _main: [] });
    });

    it ('should call on catalog component initCategoriesMap' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        const categoriesMap = component.componentInstance.initCategoriesMap(categoriesElements);
        expect(categoriesMap["resourceNewCategory.allotted resource.allotted resource"].parent.name).toEqual("Allotted Resource");
        expect(categoriesMap["resourceNewCategory.generic"].category.uniqueId).toEqual("resourceNewCategory.generic");
        expect(categoriesMap["serviceNewCategory.voip call control"].category.name).toEqual("VoIP Call Control");

    });


    it ('should call on catalog component selectLeftSwitchItem with active and selectedCatalogItem equal to archived' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.getActiveCatalogItems = jest.fn();
        component.componentInstance.changeFilterParams = jest.fn();
        component.componentInstance.selectedCatalogItem = catalogSelectorItemsMock[1]
        component.componentInstance.selectLeftSwitchItem(catalogSelectorItemsMock[0]);
        expect(component.componentInstance.getActiveCatalogItems).toHaveBeenCalledWith(true);
        expect(component.componentInstance.changeFilterParams).toHaveBeenCalledWith({active: true})
    });

    it ('should call on catalog component selectLeftSwitchItem with archived and selectedCatalogItem equal to active' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.getArchiveCatalogItems = jest.fn();
        component.componentInstance.changeFilterParams = jest.fn();
        component.componentInstance.selectedCatalogItem = catalogSelectorItemsMock[0]
        component.componentInstance.selectLeftSwitchItem(catalogSelectorItemsMock[1]);
        expect(component.componentInstance.getArchiveCatalogItems).toBeCalledWith(true);
        expect(component.componentInstance.changeFilterParams).toHaveBeenCalledWith({active: false})
    });

    it ('should call on catalog component sectionClick with section contains in expandedSection' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.expandedSection =  ["type", "category", "status"];
        component.componentInstance.sectionClick("type");
        expect(component.componentInstance.expandedSection).toEqual(["category", "status"])
    });

    it ('should call on catalog component sectionClick with section not contains in expandedSection' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.expandedSection =  ["type", "category", "status"];
        component.componentInstance.sectionClick("newItem");
        expect(component.componentInstance.expandedSection).toEqual(["type", "category", "status", "newItem"])
    });

    it ('should call on catalog component makeFilterParamsFromCheckboxes with selected values' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        expect(component.componentInstance.makeFilterParamsFromCheckboxes(checkListModelMock)).toEqual(["Resource.VF", "Resource.VFD"])
    });

    it ('should call on catalog component order with resourceName value' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.changeFilterParams = jest.fn();
        component.componentInstance.filterParams = filterParamsMock
        component.componentInstance.order("resourceName");
        expect(component.componentInstance.changeFilterParams).toHaveBeenCalledWith( {"order": ["resourceName", false]})
    });

    it ('should call on catalog component goToComponent' , () => {
        const component = TestBed.createComponent(CatalogComponent);
       const componentMock = { uniqueId: "d3e80fed-12f6-4f29-aeb1-771050e5db72", componentType: "RESOURCE"}
        component.componentInstance.goToComponent(componentMock);
       expect(stateServiceMock.go).toHaveBeenCalledWith('workspace.general', {id: componentMock.uniqueId, type: componentMock.componentType.toLowerCase()})

    });

    it ('should call on catalog component getNumOfElements for active catalog' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.selectedCatalogItem = catalogSelectorItemsMock[0]
        expect(component.componentInstance.getNumOfElements(3)).toEqual("3 <b>Active</b> Elements found")

    });

    it ('should call on catalog component raiseNumberOfElementToDisplay with empty catalogFilteredItems' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.catalogFilteredItems = []
        component.componentInstance.raiseNumberOfElementToDisplay(true);
        expect(component.componentInstance.numberOfItemToDisplay).toEqual(NaN);
        expect(component.componentInstance.catalogFilteredSlicedItems).toEqual([]);
    });

    it ('should call on catalog component raiseNumberOfElementToDisplay with full catalogFilteredItems' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.catalogFilteredItems = [1 , 2 , 3, 4, 5, 6]
        component.componentInstance.numberOfItemToDisplay = 2;
        component.componentInstance.raiseNumberOfElementToDisplay(false);
         expect(component.componentInstance.numberOfItemToDisplay).toEqual(6);
         expect(component.componentInstance.catalogFilteredSlicedItems).toEqual([1 , 2 , 3, 4, 5, 6]);
    });

    it ('should call on catalog component componentShouldReload return false' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.isDefaultFilter = jest.fn().mockImplementation(() => false);
        cacheServiceMock.get.mockImplementation(()=> "mockConstructor");
        let componentShouldReload = component.componentInstance.componentShouldReload();
        expect(component.componentInstance.cacheService.get()).toEqual(component.componentInstance.$state.current.name);
        expect(component.componentInstance.cacheService.contains()).toEqual(true);
        expect(component.componentInstance.isDefaultFilter).toHaveBeenCalled();
        expect(componentShouldReload).toEqual(false);
    });

    it ('should call on catalog component componentShouldReload return true' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.isDefaultFilter = jest.fn();
        let componentShouldReload = component.componentInstance.componentShouldReload();
        expect(component.componentInstance.cacheService.get()).not.toEqual(component.componentInstance.$state.current.name);
        expect(component.componentInstance.cacheService.contains()).toEqual(true);
        expect(componentShouldReload).toEqual(true);
    });

    it ('should call on catalog component getActiveCatalogItems with true' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        let resp = component.componentInstance.cacheService.get();
        component.componentInstance.updateCatalogItems = jest.fn().mockImplementation((resp) => {});
        component.componentInstance.getActiveCatalogItems(true);
        expect(component.componentInstance.loaderService.activate).toHaveBeenCalled();
        expect(component.componentInstance.updateCatalogItems).toHaveBeenCalledWith(resp);
        expect(component.componentInstance.loaderService.deactivate).toHaveBeenCalled();
        expect(component.componentInstance.cacheService.set).toHaveBeenCalledWith('breadcrumbsComponentsState', "mockConstructor");
        expect(component.componentInstance.cacheService.set).toHaveBeenCalledWith('breadcrumbsComponents', categoriesElements);
        expect(component.componentInstance.catalogService.getCatalog).toHaveBeenCalled();
    });

    it ('should call on catalog component getActiveCatalogItems with false' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.componentShouldReload = jest.fn();
        component.componentInstance.updateCatalogItems = jest.fn().mockImplementation((resp) => {});
        component.componentInstance.getActiveCatalogItems(false);
        expect(component.componentInstance.componentShouldReload).toHaveBeenCalled();
        let resp = component.componentInstance.cacheService.get();
        expect(component.componentInstance.updateCatalogItems).toHaveBeenCalledWith(resp);
    });

    it ('should call on catalog component getActiveCatalogItems with true observable return error' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        catalogServiceMock.getCatalog.mockImplementation(()=>  Observable.throwError('error'));
        component.componentInstance.getActiveCatalogItems(true);
        expect(component.componentInstance.loaderService.activate).toHaveBeenCalled();
        expect(console.info).toHaveBeenCalledWith('Failed to load catalog CatalogViewModel::getActiveCatalogItems');
        expect(component.componentInstance.loaderService.deactivate).toHaveBeenCalled();
        expect(component.componentInstance.catalogService.getCatalog).toHaveBeenCalled();
    });

    it ('should call on catalog component getArchiveCatalogItems with true' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        const resp = component.componentInstance.cacheService.get();
        component.componentInstance.updateCatalogItems = jest.fn().mockImplementation((resp) => {});
        component.componentInstance.getArchiveCatalogItems(true);
        expect(component.componentInstance.loaderService.activate).toHaveBeenCalled();
        expect(component.componentInstance.catalogService.getArchiveCatalog).toHaveBeenCalled();
        expect(component.componentInstance.cacheService.set).toHaveBeenCalledWith('archiveComponents', categoriesElements);
        expect(component.componentInstance.loaderService.deactivate).toHaveBeenCalled();
        expect(component.componentInstance.updateCatalogItems).toHaveBeenCalledWith(resp)
    });

    it ('should call on catalog component getArchiveCatalogItems with false' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.updateCatalogItems = jest.fn().mockImplementation((resp) => Observable.of());
        component.componentInstance.getArchiveCatalogItems(false);
        expect(component.componentInstance.cacheService.contains).toHaveBeenCalled();
        expect(component.componentInstance.cacheService.get).toHaveBeenCalled();
        let resp = component.componentInstance.cacheService.get();
        expect(component.componentInstance.updateCatalogItems).toHaveBeenCalledWith(resp);
    });

    it ('should call on catalog component getArchiveCatalogItems with true observable return error' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        catalogServiceMock.getArchiveCatalog.mockImplementation(()=>  Observable.throwError('error'));
        component.componentInstance.getArchiveCatalogItems(true);
        expect(component.componentInstance.loaderService.activate).toHaveBeenCalled();
        expect(component.componentInstance.catalogService.getArchiveCatalog).toHaveBeenCalled();
        expect(component.componentInstance.loaderService.deactivate).toHaveBeenCalled();
        expect(console.info).toHaveBeenCalledWith('Failed to load catalog CatalogViewModel::getArchiveCatalogItems');
    });

    it ('should call on catalog component updateCatalogItems' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.filterCatalogItems = jest.fn();
        component.componentInstance.addFilterTermToComponent = jest.fn();
        component.componentInstance.updateCatalogItems([1, 2, 3]);
        expect(component.componentInstance.catalogItems).toEqual([1, 2, 3]);
        expect(component.componentInstance.addFilterTermToComponent).toHaveBeenCalled();
        expect(component.componentInstance.filterCatalogItems).toHaveBeenCalled();
    });

    it ('should call on catalog component applyFilterParamsToView' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.initCheckboxesFilter = jest.fn();
        component.componentInstance.filterCatalogCategories = jest.fn();
        component.componentInstance.applyFilterParamsComponents = jest.fn();
        component.componentInstance.applyFilterParamsCategories = jest.fn();
        component.componentInstance.applyFilterParamsStatuses = jest.fn();
        component.componentInstance.applyFilterParamsModels = jest.fn();
        component.componentInstance.applyFilterParamsOrder = jest.fn();
        component.componentInstance.applyFilterParamsTerm = jest.fn();
        component.componentInstance.filterCatalogItems = jest.fn();
        component.componentInstance.applyFilterParamsToView(filterParamsMock);
        expect(component.componentInstance.initCheckboxesFilter).toHaveBeenCalled();
        expect(component.componentInstance.filterCatalogCategories).toHaveBeenCalled();
        expect(component.componentInstance.applyFilterParamsComponents).toHaveBeenCalledWith(filterParamsMock);
        expect(component.componentInstance.applyFilterParamsCategories).toHaveBeenCalledWith(filterParamsMock);
        expect(component.componentInstance.applyFilterParamsStatuses).toHaveBeenCalledWith(filterParamsMock);
        expect(component.componentInstance.applyFilterParamsOrder).toHaveBeenCalledWith(filterParamsMock);
        expect(component.componentInstance.applyFilterParamsTerm).toHaveBeenCalledWith(filterParamsMock);
        expect(component.componentInstance.filterCatalogItems).toHaveBeenCalled();
    });

    it ('should call on catalog component filterCatalogCategories' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.makeFilteredCategories = jest.fn();
        component.componentInstance.buildChecklistModelForCategories = jest.fn();
        component.componentInstance.categories = categoriesElements;
        component.componentInstance.checkboxesFilter = {selectedComponentTypes: ["firstType", "secondType"]};
        component.componentInstance.filterCatalogCategories();
        expect(component.componentInstance.makeFilteredCategories).toHaveBeenCalledWith(categoriesElements, ["firstType", "secondType"]);
        expect(component.componentInstance.buildChecklistModelForCategories).toHaveBeenCalled();
    });

    it ('should call on catalog component filterCatalogItems' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.makeFilteredItems = jest.fn().mockImplementation(() => [1,2,3]);
        component.componentInstance.raiseNumberOfElementToDisplay = jest.fn();
        component.componentInstance.catalogItems = ["firstComponent", "secondComponent"];
        component.componentInstance.checkboxesFilter = {};
        component.componentInstance.search = {};
        component.componentInstance.sortBy = "";
        component.componentInstance.reverse = true;
        component.componentInstance.numberOfItemToDisplay = 2;
       // component.componentInstance.catalogFilteredItems = component.componentInstance.makeFilteredItems();
        component.componentInstance.filterCatalogItems();
        expect(component.componentInstance.makeFilteredItems).toHaveBeenCalledWith(["firstComponent", "secondComponent"], {}, {}, "",true);
        expect(component.componentInstance.raiseNumberOfElementToDisplay).toHaveBeenCalledWith(true);
        expect(component.componentInstance.catalogFilteredSlicedItems).toEqual([1,2]);
    });

    it ('should call on catalog component applyFilterParamsToCheckboxes' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.applyFilterParamsToCheckboxes(checkListModelMock, ["Resource.CR", "Resource.VFD", "Resource.VF"]);
        expect(checkListModelMock.selectedValues).toEqual(["Resource.VF","Resource.CR"]);
        expect(checkListModelMock.checkboxes[1].subLevelChecklist.selectedValues).toEqual(["Resource.VFD"]);
        expect(checkListModelMock.checkboxes[2].subLevelChecklist.selectedValues).toEqual(["Resource.VF"])
    });

    it ('should call on catalog component applyFilterParamsComponents and filterParams.active equal true' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.applyFilterParamsToCheckboxes = jest.fn();
        component.componentInstance.checkboxesFilterKeys = checkboxesFilterKeysMock;
        component.componentInstance.checkboxesFilter = checkboxesFilterMock;
        component.componentInstance.catalogSelectorItems = catalogSelectorItemsMock;
        component.componentInstance.typesChecklistModel = checkListModelMock;
        component.componentInstance.applyFilterParamsComponents(filterParamsMock);
        expect(component.componentInstance.applyFilterParamsToCheckboxes).toHaveBeenCalledWith(checkListModelMock, filterParamsMock.components);
        expect(component.componentInstance.checkboxesFilter.selectedComponentTypes).toEqual(["Resource.VFC"]);
        expect(component.componentInstance.checkboxesFilter.selectedResourceSubTypes).toEqual(["VF", "VFC"]);
        expect(component.componentInstance.selectedCatalogItem).toEqual(catalogSelectorItemsMock[0]);
    });

    it ('should call on catalog component applyFilterParamsComponents and filterParams.active equal false' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.applyFilterParamsToCheckboxes = jest.fn();
        filterParamsMock.active = false;
        component.componentInstance.checkboxesFilterKeys = checkboxesFilterKeysMock;
        component.componentInstance.checkboxesFilter = checkboxesFilterMock;
        component.componentInstance.catalogSelectorItems = catalogSelectorItemsMock;
        component.componentInstance.typesChecklistModel = checkListModelMock;
        component.componentInstance.applyFilterParamsComponents(filterParamsMock);
        expect(component.componentInstance.applyFilterParamsToCheckboxes).toHaveBeenCalledWith(checkListModelMock, filterParamsMock.components);
        expect(component.componentInstance.checkboxesFilter.selectedComponentTypes).toEqual(["Resource.VFC"]);
        expect(component.componentInstance.checkboxesFilter.selectedResourceSubTypes).toEqual(["VF", "VFC"]);
        expect(component.componentInstance.selectedCatalogItem).toEqual(catalogSelectorItemsMock[1]);
    });

    it ('should call on catalog component applyFilterParamsCategories' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.applyFilterParamsToCheckboxes = jest.fn();
        component.componentInstance.categoriesChecklistModel = checkListModelMock;
        component.componentInstance.checkboxesFilterKeys = checkboxesFilterKeysMock;
        component.componentInstance.checkboxesFilter = checkboxesFilterMock;
        component.componentInstance.applyFilterParamsCategories(filterParamsMock);
        expect(component.componentInstance.applyFilterParamsToCheckboxes).toHaveBeenCalledWith(checkListModelMock, filterParamsMock.categories);
        expect(component.componentInstance.checkboxesFilter.selectedCategoriesModel).toEqual(["serviceNewCategory.network l4+"]);
    });

    it ('should call on catalog component applyFilterParamsStatuses' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.applyFilterParamsToCheckboxes = jest.fn();

        component.componentInstance.statusChecklistModel = checkListModelMock;
        component.componentInstance.checkboxesFilterKeys = checkboxesFilterKeysMock;
        component.componentInstance.checkboxesFilter = checkboxesFilterMock;
        component.componentInstance.confStatus = sdcMenu.statuses;
        component.componentInstance.applyFilterParamsStatuses(filterParamsMock);
        expect(component.componentInstance.applyFilterParamsToCheckboxes).toHaveBeenCalledWith(checkListModelMock, filterParamsMock.statuses);
        expect(component.componentInstance.checkboxesFilter.selectedStatuses).toEqual(["NOT_CERTIFIED_CHECKOUT", "NOT_CERTIFIED_CHECKIN"]);
    });

    it ('should call on catalog component applyFilterParamsOrder' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.applyFilterParamsOrder(filterParamsMock);
        expect(component.componentInstance.sortBy).toEqual("lastUpdateDate");
        expect(component.componentInstance.reverse).toEqual( true);
    });

    it ('should call on catalog component applyFilterParamsTerm' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.applyFilterParamsTerm(filterParamsMock);
        expect(component.componentInstance.search.filterTerm).toEqual("Vf");
    });

    // it ('should call on catalog component loadFilterParams' , () => {
    //     const component = TestBed.createComponent(CatalogComponent);
    //     component.componentInstance.$state = {params: {}};
    //     component.componentInstance.loadFilterParams();
    // });

    it ('should call on catalog component changeFilterParams' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.applyFilterParamsToView = jest.fn();
        component.componentInstance.filterParams =  { active: true, categories: [], components: [], order:  ["lastUpdateDate", true], statuses: [], term: ""};
        component.componentInstance.$state.go = jest.fn().mockImplementation(() => Promise.resolve({ json: () => [] }));
        const newParams = {"filter.active": true, "filter.categories": "resourceNewCategory.allotted resource.allotted resource,resourceNewCategory.allotted resource.contrail route,resourceNewCategory.application l4+.application server", "filter.components": "Resource.VF,Resource.VFC", "filter.models": "test", "filter.order": "-lastUpdateDate", "filter.statuses": "inDesign", "filter.term": "Vf"}
        component.componentInstance.changeFilterParams(filterParamsMock);
        expect(component.componentInstance.filterParams).toEqual(filterParamsMock);
        expect(component.componentInstance.$state.go).toHaveBeenCalledWith('.',newParams, {location: 'replace', notify: false});
        expect(component.componentInstance.applyFilterParamsToView).toHaveBeenCalledWith(filterParamsMock);
    });

    it ('should call on catalog component changeFilterParams and rebuild equal true' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        component.componentInstance.applyFilterParamsToView = jest.fn();
        component.componentInstance.makeFilterParamsFromCheckboxes = jest.fn();
        component.componentInstance.buildCheckboxLists = jest.fn();
        component.componentInstance.filterParams =  { active: true, categories: [], components: [], order:  ["lastUpdateDate", true], statuses: [], term: ""};
        component.componentInstance.$state.go = jest.fn().mockImplementation(() => Promise.resolve({ json: () => [] }));
        const newParams = {"filter.active": true, "filter.categories": "resourceNewCategory.allotted resource.allotted resource,resourceNewCategory.allotted resource.contrail route,resourceNewCategory.application l4+.application server", "filter.components": "Resource.VF,Resource.VFC", "filter.models": "test", "filter.order": "-lastUpdateDate", "filter.statuses": "inDesign", "filter.term": "Vf"}
        component.componentInstance.typesChecklistModel = checkListModelMock;
        component.componentInstance.categoriesChecklistModel = checkListModelMock;
        component.componentInstance.statusChecklistModel = checkListModelMock;
        component.componentInstance.changeFilterParams(filterParamsMock, true);
        expect(component.componentInstance.filterParams).toEqual(filterParamsMock);
        expect(component.componentInstance.$state.go).toHaveBeenCalledWith('.',newParams, {location: 'replace', notify: false});
        //expect(component.componentInstance.makeFilterParamsFromCheckboxes).toHaveBeenCalledWith(component.componentInstance.typesChecklistModel);
        //expect(component.componentInstance.buildCheckboxLists).toHaveBeenCalled();
        expect(component.componentInstance.applyFilterParamsToView).toHaveBeenCalledWith(filterParamsMock);
    });

    it ('should call on catalog component makeFilteredCategories' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        const categoryMock = [{"name":"Network L1-3","normalizedName":"network l1-3","uniqueId":"serviceNewCategory.network l1-3","icons":["network_l_1-3"],"subcategories":null,"version":null,"ownerId":null,"empty":false,"type":null}];
        cacheServiceMock.get.mockImplementation(()=> categoryMock);
        const resp = component.componentInstance.makeFilteredCategories(categoriesElements, checkboxesFilterMock.selectedComponentTypes);
        expect(component.componentInstance.cacheService.get).toHaveBeenCalledWith("resourceCategories");
        expect(resp).toEqual(categoryMock);
    });

    it ('should call on catalog component makeFilteredCategories return unique elements' , () => {
        const component = TestBed.createComponent(CatalogComponent);
        const categoryMock = [{"name":"Network L1-3","normalizedName":"network l1-3","uniqueId":"serviceNewCategory.network l1-3","icons":["network_l_1-3"],"subcategories":null,"version":null,"ownerId":null,"empty":false,"type":null},
            {"name":"Network L1-3","normalizedName":"network l1-3","uniqueId":"serviceNewCategory.network l1-3","icons":["network_l_1-3"],"subcategories":null,"version":null,"ownerId":null,"empty":false,"type":null},
            {"name":"Network Service","normalizedName":"network service","uniqueId":"serviceNewCategory.network service","icons":["network_l_1-3"],"subcategories":null,"version":null,"ownerId":null,"empty":false,"type":null}];
        const categoryUniqueMock = [{"name":"Network L1-3","normalizedName":"network l1-3","uniqueId":"serviceNewCategory.network l1-3","icons":["network_l_1-3"],"subcategories":null,"version":null,"ownerId":null,"empty":false,"type":null},
            {"name":"Network Service","normalizedName":"network service","uniqueId":"serviceNewCategory.network service","icons":["network_l_1-3"],"subcategories":null,"version":null,"ownerId":null,"empty":false,"type":null}];
        cacheServiceMock.get.mockImplementation(()=> categoryMock);
        checkboxesFilterMock.selectedComponentTypes = ["SERVICE", "Resource.VF"];
        const resp = component.componentInstance.makeFilteredCategories(categoriesElements, checkboxesFilterMock.selectedComponentTypes);
        expect(component.componentInstance.cacheService.get).toHaveBeenCalledWith("resourceCategories");
        expect(resp).toEqual(categoryUniqueMock);
    });


});
