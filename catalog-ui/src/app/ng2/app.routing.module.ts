/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2025 Deutsche Telekom AG. All rights reserved.
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

import {NgModule} from '@angular/core';
import {RouterModule, Routes, UrlHandlingStrategy, UrlTree} from '@angular/router';
import {HomeComponent} from './pages/home/home.component';
import {CatalogComponent} from './pages/catalog/catalog.component';
import {TypeWorkspaceComponent} from './pages/type-workspace/type-workspace.component';
import {AuthGuard} from './guards/auth.guard';

const routes: Routes = [
    {path: 'dashboard', component: HomeComponent, canActivate: [AuthGuard], data: {permissions: ['DESIGNER']}},
    {path: 'catalog', component: CatalogComponent, canActivate: [AuthGuard]},
    {path: ':previousState/type-workspace/:type/:id/:subPage', component: TypeWorkspaceComponent},
    {path: '', redirectTo: 'dashboard', pathMatch: 'full'}
];

export class SdcUrlHandlingStrategy implements UrlHandlingStrategy {

    shouldProcessUrl(url: UrlTree): boolean {
        const path = url.toString();
        if (path === '/' || path.startsWith('/dashboard')) {
            return true;
        }
        if (path.startsWith('/catalog') && !path.includes('/workspace/')) {
            return true;
        }
        if (/^\/[^/]+\/type-workspace\//.test(path)) {
            return true;
        }
        return false;
    }

    extract(url: UrlTree): UrlTree {
        return url;
    }

    merge(newUrlPart: UrlTree, rawUrl: UrlTree): UrlTree {
        return newUrlPart;
    }
}

@NgModule({
    imports: [
        RouterModule.forRoot(routes, {useHash: true})
    ],
    exports: [RouterModule],
    providers: [
        {provide: UrlHandlingStrategy, useClass: SdcUrlHandlingStrategy}
    ]
})
export class AppRoutingModule {
}
