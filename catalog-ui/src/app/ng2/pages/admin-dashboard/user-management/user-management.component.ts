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
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Subscription} from 'rxjs/Subscription';
import {IUserProperties} from 'app/models/user';
import {UserService} from 'app/ng2/services/user.service';
import {TranslateService} from 'app/ng2/shared/translator/translate.service';

interface ITableHeader {
    title: string;
    property: string;
}

interface IUserRow extends IUserProperties {
    index?: number;
}

/**
 * Admin Dashboard "User Management" tab. Read-only table of all SDC users.
 * Migrated from the AngularJS UserManagementViewModel to an OnPush Angular component (Phase 7).
 *
 * No add/edit/delete — display only.  Filtering is done in TypeScript via a pre-computed
 * filterTerm per user so the OnPush component never derives a new array in the template.
 */
@Component({
    selector: 'user-management',
    templateUrl: './user-management.component.html',
    styleUrls: ['./user-management.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserManagementComponent implements OnInit, OnDestroy {

    isLoading: boolean = false;

    usersList: IUserRow[] = [];
    filteredUsers: IUserRow[] = [];

    tableHeadersList: ITableHeader[] = [];

    sortBy: string = 'lastLoginTime';
    reverse: boolean = false;
    searchTerm: string = '';

    private datePipe: DatePipe = new DatePipe('en-US');
    private languageChangedSubscription: Subscription;

    constructor(private userService: UserService,
                private translateService: TranslateService,
                private cdr: ChangeDetectorRef) {
    }

    ngOnInit(): void {
        this.buildTableHeaders();
        // The 'User ID' header title is resolved once via translateService.translate(). On a cold
        // load the async language JSON has not resolved yet, so it comes back empty and (OnPush,
        // propagateDigest:false) never re-renders. Rebuild the headers + re-run CD when the
        // language loads. languageChangedObservable is publishReplay(1) — fires immediately if
        // already loaded, or on load otherwise.
        this.languageChangedSubscription = this.translateService.languageChangedObservable.subscribe(() => {
            this.buildTableHeaders();
            this.detectChangesSafe();
        });

        this.isLoading = true;

        const onSuccess = (response: IUserRow[]) => {
            this.usersList = response;
            this.usersList.forEach((user: IUserRow, i: number) => {
                user.index = i;
                user.filterTerm = [
                    user.firstName,
                    user.lastName,
                    user.userId,
                    user.email,
                    user.role,
                    this.datePipe.transform(Number(user.lastLoginTime), 'MM/dd/yyyy')
                ].join(' ');
            });
            this.isLoading = false;
            this.detectChangesSafe();
            this.refresh();
        };

        const onError = (response: any) => {
            this.isLoading = false;
            this.detectChangesSafe();
            console.error('UserManagementComponent: failed to load users', response);
        };

        this.userService.getAllUsers().subscribe(onSuccess, onError);
    }

    ngOnDestroy(): void {
        if (this.languageChangedSubscription) {
            this.languageChangedSubscription.unsubscribe();
        }
    }

    private buildTableHeaders(): void {
        this.tableHeadersList = [
            {title: 'First Name', property: 'firstName'},
            {title: 'Last Name', property: 'lastName'},
            {title: this.translateService.translate('USER_MANAGEMENT_TABLE_HEADER_USER_ID'), property: 'userId'},
            {title: 'Email', property: 'email'},
            {title: 'Role', property: 'role'},
            {title: 'Last Active', property: 'lastLoginTime'}
        ];
    }

    sort(by: string): void {
        this.reverse = (this.sortBy === by) ? !this.reverse : false;
        this.sortBy = by;
        this.refresh();
    }

    onSearchChange(term: string): void {
        this.searchTerm = term;
        this.refresh();
    }

    getTitle(role: string): string {
        return role.toLowerCase().replace('governor', 'governance_Rep').replace('_', ' ');
    }

    trackByUserId(i: number, user: IUserRow): any {
        return user.userId || i;
    }

    private refresh(): void {
        const term = (this.searchTerm || '').toLowerCase();
        let rows: IUserRow[] = this.usersList.slice();
        if (term) {
            rows = rows.filter((u: IUserRow) =>
                (u.filterTerm || '').toLowerCase().indexOf(term) !== -1
            );
        }
        const sortBy = this.sortBy;
        const reverse = this.reverse;
        rows.sort((a: IUserRow, b: IUserRow) => {
            const av: any = a[sortBy];
            const bv: any = b[sortBy];
            let cmp: number;
            if (sortBy === 'lastLoginTime') {
                cmp = Number(av || 0) - Number(bv || 0);
            } else {
                const as = av === undefined || av === null ? '' : String(av);
                const bs = bv === undefined || bv === null ? '' : String(bv);
                cmp = as.localeCompare(bs);
            }
            return reverse ? -cmp : cmp;
        });
        this.filteredUsers = rows;
        this.detectChangesSafe();
    }

    private detectChangesSafe(): void {
        if (!(this.cdr as any).destroyed) {
            this.cdr.detectChanges();
        }
    }
}
