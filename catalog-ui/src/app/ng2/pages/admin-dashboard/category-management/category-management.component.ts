/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {ButtonModel, ModalModel} from 'app/models';
import {IMainCategory, ISubCategory} from 'app/models/category';
import {CacheService} from 'app/services-ng2';
import {ModalService} from 'app/ng2/services/modal.service';
import {TranslateService} from 'app/ng2/shared/translator/translate.service';
import {ValidationUtils} from 'app/utils';
import {AddCategoryModalComponent} from '../add-category-modal/add-category-modal.component';

@Component({
    selector: 'category-management',
    templateUrl: './category-management.component.html',
    styleUrls: ['./category-management.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CategoryManagementComponent implements OnInit {

    readonly SERVICE: string = 'service';
    readonly RESOURCE: string = 'resource';

    type: string;
    categoriesToShow: Array<IMainCategory> = [];
    selectedCategory: IMainCategory = null;
    selectedSubCategory: ISubCategory = null;
    namePattern: RegExp;
    isLoading: boolean = false;

    private serviceCategories: Array<IMainCategory> = [];
    private resourceCategories: Array<IMainCategory> = [];
    private modalInstance: any;

    constructor(private cacheService: CacheService,
                private modalService: ModalService,
                private validationUtils: ValidationUtils,
                private translateService: TranslateService,
                private cdr: ChangeDetectorRef) {}

    ngOnInit(): void {
        this.serviceCategories = this.cacheService.get('serviceCategories');
        this.resourceCategories = this.cacheService.get('resourceCategories');
        this.namePattern = this.validationUtils.getValidationPattern('category');
        this.selectType(this.SERVICE);
        this.detectChangesSafe();
    }

    selectType(type: string): void {
        if (this.type !== type) {
            this.selectedCategory = null;
            this.selectedSubCategory = null;
        }
        this.type = type;
        this.categoriesToShow = (type === this.SERVICE) ? this.serviceCategories : this.resourceCategories;
    }

    selectCategory(category: IMainCategory): void {
        if (this.selectedCategory !== category) {
            this.selectedSubCategory = null;
        }
        this.selectedCategory = category;
    }

    selectSubCategory(subcategory: ISubCategory): void {
        this.selectedSubCategory = subcategory;
    }

    createCategoryModal(parent?: IMainCategory): void {
        // cannot create a sub-category for a service type
        if (parent && this.type === this.SERVICE) {
            return;
        }

        const modelType: string = parent ? 'sub category' : 'category';
        const title: string = this.translateService.translate('CREATE_CATEGORY_MODAL_HEADER', {modelType});

        const okBtn = new ButtonModel('OK', 'blue', () => {
            this.modalInstance.instance.dynamicContent.instance.save().subscribe((created: IMainCategory | ISubCategory) => {
                if (!parent) {
                    this.categoriesToShow.push(created as IMainCategory);
                } else {
                    parent.subcategories = parent.subcategories || [];
                    parent.subcategories.push(created as ISubCategory);
                }
                this.modalService.closeCurrentModal();
                this.detectChangesSafe();
            });
        }, () => !this.modalInstance.instance.dynamicContent.instance.isValid());

        const cancelBtn = new ButtonModel('Cancel', 'grey', () => this.modalService.closeCurrentModal());

        this.modalInstance = this.modalService.createCustomModal(
            new ModalModel('sm', title, null, [okBtn, cancelBtn], 'standard')
        );

        this.modalService.addDynamicContentToModal(
            this.modalInstance,
            AddCategoryModalComponent,
            {type: this.type, parentCategory: parent, namePattern: this.namePattern, modelType}
        );

        this.modalInstance.instance.open();
    }

    private detectChangesSafe(): void {
        if (!(this.cdr as any).destroyed) {
            this.cdr.detectChanges();
        }
    }
}
