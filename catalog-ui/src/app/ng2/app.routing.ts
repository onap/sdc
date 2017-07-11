/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import { RouterModule, Route } from '@angular/router';
import { ModuleWithProviders } from '@angular/core';
// import { Page1Component } from "./pages/page1/page1.component";
// import { Page2Component } from "./pages/page2/page2.component";
import { PageNotFoundComponent } from "./pages/page404/page404.component";

const routes: Route[] = [
  // { path: 'page1', component: Page1Component },
  // { path: 'page2', component: Page2Component },
  // { path: '', pathMatch: 'full', redirectTo: 'page1'},
  { path: '**', component: PageNotFoundComponent }
  /*{ loadChildren: './pages/dashboard/dashboard.module#DashboardModule', path: 'dashboard' }*/
];

export const routing: ModuleWithProviders = RouterModule.forRoot(
  routes,
  {
    useHash: true
  }
);
