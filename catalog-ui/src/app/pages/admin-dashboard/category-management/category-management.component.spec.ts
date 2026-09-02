import 'rxjs/add/observable/of';
import {Observable} from 'rxjs/Observable';
import {IMainCategory, ISubCategory} from 'app/models/category';
import {CategoryManagementComponent} from './category-management.component';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeCategory(name: string, uniqueId: string = name): IMainCategory {
    return {name, uniqueId, displayName: name, normalizedName: name, models: [], icons: [],
        metadataKeys: [], filterTerms: '', isDisabled: false, filteredGroup: [],
        subcategories: [], useServiceSubstitutionForNestedServices: false,
        notApplicableMetadataKeys: []} as IMainCategory;
}

function makeSub(name: string, uniqueId: string = name): ISubCategory {
    return {name, uniqueId, displayName: name, normalizedName: name, models: [], icons: [],
        metadataKeys: [], filterTerms: '', isDisabled: false, filteredGroup: [],
        groupings: []} as ISubCategory;
}

// ---------------------------------------------------------------------------
// Factory
// ---------------------------------------------------------------------------

function createComp() {
    const serviceCategories: IMainCategory[] = [makeCategory('Services A'), makeCategory('Services B')];
    const resourceCategories: IMainCategory[] = [makeCategory('Resource X')];

    const cacheService: any = {
        get: jest.fn((key: string) => {
            if (key === 'serviceCategories') { return serviceCategories; }
            if (key === 'resourceCategories') { return resourceCategories; }
            return undefined;
        })
    };

    const validationUtils: any = {
        getValidationPattern: jest.fn((type: string) => /^[a-z]+$/i)
    };

    // The modal instance that createCustomModal returns
    const dynamicContentInstance = {
        save: jest.fn(),
        isValid: jest.fn(() => true)
    };
    const modalInstance: any = {
        instance: {
            dynamicContent: {instance: dynamicContentInstance},
            open: jest.fn()
        }
    };
    const modalService: any = {
        createCustomModal: jest.fn(() => modalInstance),
        addDynamicContentToModal: jest.fn(),
        closeCurrentModal: jest.fn()
    };

    const translateService: any = {
        translate: jest.fn((key: string) => key)
    };

    const cdr: any = {detectChanges: jest.fn(), destroyed: false};

    const comp = new CategoryManagementComponent(
        cacheService, modalService, validationUtils, translateService, cdr);

    return {comp, cacheService, validationUtils, modalService, translateService, cdr,
        serviceCategories, resourceCategories, modalInstance, dynamicContentInstance};
}

// ---------------------------------------------------------------------------
// Specs
// ---------------------------------------------------------------------------

describe('CategoryManagementComponent', () => {

    // --- ngOnInit ---

    it('ngOnInit defaults type to "service" and populates categoriesToShow with serviceCategories', () => {
        const {comp, serviceCategories} = createComp();
        comp.ngOnInit();
        expect(comp.type).toBe('service');
        expect(comp.categoriesToShow).toBe(serviceCategories);
    });

    it('ngOnInit reads serviceCategories and resourceCategories from cache', () => {
        const {comp, cacheService} = createComp();
        comp.ngOnInit();
        expect(cacheService.get).toHaveBeenCalledWith('serviceCategories');
        expect(cacheService.get).toHaveBeenCalledWith('resourceCategories');
    });

    it('ngOnInit calls getValidationPattern("category") on validationUtils', () => {
        const {comp, validationUtils} = createComp();
        comp.ngOnInit();
        expect(validationUtils.getValidationPattern).toHaveBeenCalledWith('category');
    });

    it('ngOnInit calls detectChanges via cdr', () => {
        const {comp, cdr} = createComp();
        comp.ngOnInit();
        expect(cdr.detectChanges).toHaveBeenCalled();
    });

    // --- selectType ---

    it('selectType("resource") swaps categoriesToShow to resourceCategories', () => {
        const {comp, resourceCategories} = createComp();
        comp.ngOnInit();
        comp.selectType('resource');
        expect(comp.type).toBe('resource');
        expect(comp.categoriesToShow).toBe(resourceCategories);
    });

    it('selectType to different type clears selectedCategory and selectedSubCategory', () => {
        const {comp, serviceCategories} = createComp();
        comp.ngOnInit();
        comp.selectedCategory = serviceCategories[0];
        comp.selectedSubCategory = makeSub('sub') as any;
        comp.selectType('resource');
        expect(comp.selectedCategory).toBeNull();
        expect(comp.selectedSubCategory).toBeNull();
    });

    it('selectType to same type does NOT clear selectedCategory', () => {
        const {comp, serviceCategories} = createComp();
        comp.ngOnInit();
        comp.selectedCategory = serviceCategories[0];
        comp.selectType('service');  // same type
        expect(comp.selectedCategory).toBe(serviceCategories[0]);
    });

    // --- selectCategory ---

    it('selectCategory sets selectedCategory', () => {
        const {comp, serviceCategories} = createComp();
        comp.ngOnInit();
        comp.selectCategory(serviceCategories[0]);
        expect(comp.selectedCategory).toBe(serviceCategories[0]);
    });

    it('selectCategory clears selectedSubCategory when category changes', () => {
        const {comp, serviceCategories} = createComp();
        comp.ngOnInit();
        comp.selectCategory(serviceCategories[0]);
        comp.selectedSubCategory = makeSub('sub') as any;
        comp.selectCategory(serviceCategories[1]);
        expect(comp.selectedSubCategory).toBeNull();
    });

    it('selectCategory does NOT clear selectedSubCategory when same category clicked again', () => {
        const {comp, serviceCategories} = createComp();
        comp.ngOnInit();
        comp.selectCategory(serviceCategories[0]);
        const sub = makeSub('sub') as any;
        comp.selectedSubCategory = sub;
        comp.selectCategory(serviceCategories[0]);  // same
        expect(comp.selectedSubCategory).toBe(sub);
    });

    // --- selectSubCategory ---

    it('selectSubCategory sets selectedSubCategory', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        const sub = makeSub('sub') as any;
        comp.selectSubCategory(sub);
        expect(comp.selectedSubCategory).toBe(sub);
    });

    // --- createCategoryModal guard ---

    it('createCategoryModal with parent and type==="service" returns early — no modal opened', () => {
        const {comp, modalService, serviceCategories} = createComp();
        comp.ngOnInit();
        // type defaults to 'service'
        comp.createCategoryModal(serviceCategories[0]);
        expect(modalService.createCustomModal).not.toHaveBeenCalled();
    });

    it('createCategoryModal with parent and type==="resource" opens the modal', () => {
        const {comp, modalService, resourceCategories} = createComp();
        comp.ngOnInit();
        comp.selectType('resource');
        const created = makeCategory('New Sub') as any;
        const {dynamicContentInstance, modalInstance} = createComp();  // get fresh mock
        // reuse the already-created comp's modalService which has been configured
        modalService.createCustomModal.mockReturnValue({
            instance: {dynamicContent: {instance: {save: jest.fn(() => Observable.of(created)), isValid: jest.fn(() => true)}}, open: jest.fn()}
        });
        comp.createCategoryModal(resourceCategories[0]);
        expect(modalService.createCustomModal).toHaveBeenCalled();
    });

    it('createCategoryModal(null) calls createCustomModal and opens modal', () => {
        const {comp, modalService, modalInstance} = createComp();
        comp.ngOnInit();

        // Make dynamicContent.instance.save return Observable
        const created = makeCategory('New Cat');
        modalInstance.instance.dynamicContent.instance.save.mockReturnValue(Observable.of(created));

        comp.createCategoryModal(null);

        expect(modalService.createCustomModal).toHaveBeenCalled();
        expect(modalInstance.instance.open).toHaveBeenCalled();
        expect(modalService.addDynamicContentToModal).toHaveBeenCalled();
    });

    // --- OK button callback: top-level category creation ---

    it('OK callback pushes created category into categoriesToShow and closes modal', () => {
        const {comp, modalService, modalInstance, serviceCategories} = createComp();
        comp.ngOnInit();

        const created = makeCategory('New Cat');
        modalInstance.instance.dynamicContent.instance.save.mockReturnValue(Observable.of(created));

        comp.createCategoryModal(null);

        // Invoke the OK button callback (first button)
        const okBtn = (modalService.createCustomModal.mock.calls[0][0] as any).buttons[0];
        okBtn.callback();

        expect(comp.categoriesToShow).toContain(created);
        expect(modalService.closeCurrentModal).toHaveBeenCalled();
    });

    it('OK callback calls detectChangesSafe after pushing category', () => {
        const {comp, modalService, modalInstance, cdr} = createComp();
        comp.ngOnInit();
        cdr.detectChanges.mockClear();

        const created = makeCategory('New Cat');
        modalInstance.instance.dynamicContent.instance.save.mockReturnValue(Observable.of(created));

        comp.createCategoryModal(null);
        const okBtn = (modalService.createCustomModal.mock.calls[0][0] as any).buttons[0];
        okBtn.callback();

        expect(cdr.detectChanges).toHaveBeenCalled();
    });

    // --- OK button callback: sub-category creation ---

    it('OK callback for sub-category pushes into parent.subcategories', () => {
        const {comp, modalService, resourceCategories} = createComp();
        comp.ngOnInit();
        comp.selectType('resource');

        const parent = resourceCategories[0];
        parent.subcategories = [];

        const created = makeSub('New Sub') as any;
        const subModalInstance: any = {
            instance: {
                dynamicContent: {instance: {save: jest.fn(() => Observable.of(created)), isValid: jest.fn(() => true)}},
                open: jest.fn()
            }
        };
        modalService.createCustomModal.mockReturnValue(subModalInstance);

        comp.createCategoryModal(parent);

        const okBtn = (modalService.createCustomModal.mock.calls[0][0] as any).buttons[0];
        okBtn.callback();

        expect(parent.subcategories).toContain(created);
        expect(modalService.closeCurrentModal).toHaveBeenCalled();
    });

    it('OK callback for sub-category initialises parent.subcategories array if undefined', () => {
        const {comp, modalService, resourceCategories} = createComp();
        comp.ngOnInit();
        comp.selectType('resource');

        const parent = resourceCategories[0];
        (parent as any).subcategories = undefined;

        const created = makeSub('New Sub') as any;
        const subModalInstance: any = {
            instance: {
                dynamicContent: {instance: {save: jest.fn(() => Observable.of(created)), isValid: jest.fn(() => true)}},
                open: jest.fn()
            }
        };
        modalService.createCustomModal.mockReturnValue(subModalInstance);

        comp.createCategoryModal(parent);

        const okBtn = (modalService.createCustomModal.mock.calls[0][0] as any).buttons[0];
        okBtn.callback();

        expect(parent.subcategories).toBeDefined();
        expect(parent.subcategories).toContain(created);
    });

    // --- getDisabled (4th ButtonModel arg) ---

    it('OK button getDisabled returns true when dynamicContent.instance.isValid() is false', () => {
        const {comp, modalService, modalInstance} = createComp();
        comp.ngOnInit();
        modalInstance.instance.dynamicContent.instance.save.mockReturnValue(Observable.of(makeCategory('x')));
        modalInstance.instance.dynamicContent.instance.isValid.mockReturnValue(false);

        comp.createCategoryModal(null);

        const okBtn = (modalService.createCustomModal.mock.calls[0][0] as any).buttons[0];
        expect(okBtn.getDisabled()).toBe(true);
    });

    it('OK button getDisabled returns false when dynamicContent.instance.isValid() is true', () => {
        const {comp, modalService, modalInstance} = createComp();
        comp.ngOnInit();
        modalInstance.instance.dynamicContent.instance.save.mockReturnValue(Observable.of(makeCategory('x')));
        modalInstance.instance.dynamicContent.instance.isValid.mockReturnValue(true);

        comp.createCategoryModal(null);

        const okBtn = (modalService.createCustomModal.mock.calls[0][0] as any).buttons[0];
        expect(okBtn.getDisabled()).toBe(false);
    });

    // --- Cancel button ---

    it('Cancel button callback calls closeCurrentModal', () => {
        const {comp, modalService, modalInstance} = createComp();
        comp.ngOnInit();
        modalInstance.instance.dynamicContent.instance.save.mockReturnValue(Observable.of(makeCategory('x')));

        comp.createCategoryModal(null);

        const cancelBtn = (modalService.createCustomModal.mock.calls[0][0] as any).buttons[1];
        cancelBtn.callback();
        expect(modalService.closeCurrentModal).toHaveBeenCalled();
    });

    // --- Constants on component (used in template) ---

    it('exposes SERVICE and RESOURCE constants', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.SERVICE).toBe('service');
        expect(comp.RESOURCE).toBe('resource');
    });
});
