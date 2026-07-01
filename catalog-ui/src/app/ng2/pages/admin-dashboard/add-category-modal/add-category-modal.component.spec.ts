import 'rxjs/add/operator/map';
import {of} from 'rxjs/observable/of';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {AddCategoryModalComponent} from './add-category-modal.component';
import {CategoryManagementService} from '../services/category-management.service';
import {IMainCategory, ISubCategory} from 'app/models/category';
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';
import {TranslateServiceConfigToken} from 'app/ng2/shared/translator/translate.service.config';

const NAME_PATTERN = /^[\s\w&\-\+]+$/;

const mockTranslateConfig = {
    filePrefix: '/assets/i18n/',
    fileSuffix: '.json',
    allowedLanguages: ['en_US'],
    defaultLanguage: 'en_US'
};

const mockCategoryService = {
    createCategory: jasmine.createSpy('createCategory').and.returnValue(of({name: 'test', uniqueId: 'c1'} as IMainCategory)),
    createSubCategory: jasmine.createSpy('createSubCategory').and.returnValue(of({name: 'test', uniqueId: 'sc1'} as ISubCategory))
};

function createComponent(parentCategory?: IMainCategory): ComponentFixture<AddCategoryModalComponent> {
    const fixture = TestBed.createComponent(AddCategoryModalComponent);
    const component = fixture.componentInstance;
    component.input = {
        type: 'service',
        namePattern: NAME_PATTERN,
        modelType: 'category',
        parentCategory
    };
    fixture.detectChanges(); // triggers ngOnInit
    return fixture;
}

describe('AddCategoryModalComponent', () => {

    beforeEach(() => {
        mockCategoryService.createCategory.calls.reset();
        mockCategoryService.createSubCategory.calls.reset();

        TestBed.configureTestingModule({
            imports: [ReactiveFormsModule, HttpClientTestingModule, TranslateModule],
            declarations: [AddCategoryModalComponent],
            providers: [
                {provide: CategoryManagementService, useValue: mockCategoryService},
                {provide: TranslateServiceConfigToken, useValue: mockTranslateConfig}
            ]
        }).compileComponents();
    });

    describe('form validation', () => {
        it('is invalid when name is empty', () => {
            const fixture = createComponent();
            expect(fixture.componentInstance.isValid()).toBe(false);
        });

        it('is invalid when name has length 2 (below minlength 3)', () => {
            const fixture = createComponent();
            fixture.componentInstance.form.get('name').setValue('ab');
            expect(fixture.componentInstance.isValid()).toBe(false);
        });

        it('is valid when name has length 3', () => {
            const fixture = createComponent();
            fixture.componentInstance.form.get('name').setValue('abc');
            expect(fixture.componentInstance.isValid()).toBe(true);
        });

        it('is invalid when name violates the namePattern', () => {
            const fixture = createComponent();
            // Exclamation mark is not in /^[\s\w&\-\+]+$/
            fixture.componentInstance.form.get('name').setValue('abc!');
            expect(fixture.componentInstance.isValid()).toBe(false);
        });

        it('is valid when name matches the namePattern', () => {
            const fixture = createComponent();
            fixture.componentInstance.form.get('name').setValue('My Service ABC');
            expect(fixture.componentInstance.isValid()).toBe(true);
        });
    });

    describe('save()', () => {
        it('calls createCategory when no parentCategory is given', () => {
            const fixture = createComponent();
            fixture.componentInstance.form.get('name').setValue('TestName');
            fixture.componentInstance.save().subscribe();
            expect(mockCategoryService.createCategory).toHaveBeenCalledWith('service', {name: 'TestName'});
            expect(mockCategoryService.createSubCategory).not.toHaveBeenCalled();
        });

        it('calls createSubCategory when parentCategory is given', () => {
            const parent = {uniqueId: 'PARENT_ID'} as IMainCategory;
            const fixture = createComponent(parent);
            fixture.componentInstance.form.get('name').setValue('SubName');
            fixture.componentInstance.save().subscribe();
            expect(mockCategoryService.createSubCategory).toHaveBeenCalledWith('service', 'PARENT_ID', {name: 'SubName'});
            expect(mockCategoryService.createCategory).not.toHaveBeenCalled();
        });
    });
});
