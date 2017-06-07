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
