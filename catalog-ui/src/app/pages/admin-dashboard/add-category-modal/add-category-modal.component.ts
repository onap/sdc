/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2024 Deutsche Telekom AG. All rights reserved.
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

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Observable} from 'rxjs/Observable';
import {IMainCategory, ISubCategory} from 'app/models/category';
import {CategoryManagementService} from '../services/category-management.service';

@Component({
    selector: 'add-category-modal',
    templateUrl: './add-category-modal.component.html',
    styleUrls: ['./add-category-modal.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AddCategoryModalComponent implements OnInit {

    /** Set by ModalService.addDynamicContentToModal() before first change detection. */
    public input: {
        type: string;
        parentCategory?: IMainCategory;
        namePattern: RegExp;
        modelType: string;
    };

    public form: FormGroup;

    constructor(private fb: FormBuilder,
                private categoryService: CategoryManagementService) {}

    public ngOnInit(): void {
        this.form = this.fb.group({
            name: ['', [
                Validators.required,
                Validators.minLength(3),
                Validators.pattern(this.input.namePattern)
            ]]
        });
    }

    public isValid(): boolean {
        return this.form.valid;
    }

    public save(): Observable<IMainCategory | ISubCategory> {
        const name = this.form.get('name').value;
        return this.input.parentCategory
            ? this.categoryService.createSubCategory(this.input.type, this.input.parentCategory.uniqueId, {name})
            : this.categoryService.createCategory(this.input.type, {name});
    }
}
