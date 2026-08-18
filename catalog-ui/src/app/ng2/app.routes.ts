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

import {Routes} from '@angular/router';
import {AdminDashboardComponent} from './pages/admin-dashboard/admin-dashboard.component';
import {AttributesOutputsComponent} from './pages/attributes-outputs/attributes-outputs.page.component';
import {CatalogComponent} from './pages/catalog/catalog.component';
import {CompositionPageComponent} from './pages/composition/composition-page.component';
import {Error403PageComponent} from './pages/error-403/error-403.component';
import {HomeComponent} from './pages/home/home.component';
import {InterfaceDefinitionComponent} from './pages/interface-definition/interface-definition.page.component';
import {InterfaceOperationComponent} from './pages/interface-operation/interface-operation.page.component';
import {OnboardVendorPageComponent} from './pages/onboard-vendor/onboard-vendor.component';
import {PluginContextViewPageComponent} from './pages/plugins/plugin-context-view/plugin-context-view.page.component';
import {PluginTabViewPageComponent} from './pages/plugins/plugin-tab-view/plugin-tab-view.page.component';
import {PropertiesAssignmentComponent} from './pages/properties-assignment/properties-assignment.page.component';
import {TypeWorkspaceComponent} from './pages/type-workspace/type-workspace.component';
import {ActivityLogComponent} from './pages/workspace/activity-log/activity-log.component';
import {DeploymentArtifactsPageComponent} from './pages/workspace/deployment-artifacts/deployment-artifacts-page.component';
import {DeploymentPageComponent} from './pages/workspace/deployment/deployment-page.component';
import {DistributionComponent} from './pages/workspace/disribution/distribution.component';
import {ManagementWorkflowTabComponent} from './pages/workspace/flow-editor/management-workflow-tab.component';
import {NetworkCallFlowTabComponent} from './pages/workspace/flow-editor/network-call-flow-tab.component';
import {GeneralTabComponent} from './pages/workspace/general-tab/general-tab.component';
import {InformationArtifactPageComponent} from './pages/workspace/information-artifact/information-artifact-page.component';
import {WorkspacePropertiesTabComponent} from './pages/workspace/properties-tab/properties-tab.component';
import {ReqAndCapabilitiesComponent} from './pages/workspace/req-and-capabilities/req-and-capabilities.component';
import {ToscaArtifactPageComponent} from './pages/workspace/tosca-artifacts/tosca-artifact-page.component';
import {WorkspaceComponentResolver} from './pages/workspace/workspace-component.resolver';
import {WorkspaceContainerComponent} from './pages/workspace/workspace-container/workspace-container.component';
import {AttributesComponent} from '../view-models/workspace/tabs/attributes/attributes.component';
import {AuthGuard} from './guards/auth.guard';
import {UnsavedChangesFlagGuard} from './guards/unsaved-changes-flag.guard';
import {UnsavedChangesGuard} from './guards/unsaved-changes.guard';

/**
 * The 19 workspace tabs. Under ui-router these were child states of 'workspace' and each declared
 * its own copy of the `injectComponent` resolve; here the resolve lives on the shared parent, so a
 * tab switch inside one component reuses the already-fetched component instead of re-fetching it.
 *
 * ui-router declared three of these WITH a trailing slash ('composition/', 'activity_log/',
 * 'deployment/'), and those URLs are hard-coded in the Cypress and Playwright suites — but the routes
 * below deliberately DROP it, and must not have it added back. `Location.path()` normalises through
 * `Location.stripTrailingSlash()`, and every URL the router reads from the browser goes through it:
 * both `initialNavigation()` (a deep link or reload) and the `hashchange` subscription. So the router
 * only ever sees '…/composition', and a route declared as 'composition/' — which is a real empty
 * segment to the path matcher — can never match it, falling through to '**' -> /dashboard instead.
 * In-app `navigateByUrl('…/composition/')` DOES match, because that string is parsed directly and
 * `Location.go()` does not normalise, which is why this is invisible to a Jest router harness.
 * Dropping the slash makes both forms work: the browser's stripped URL matches exactly, and the
 * suites' trailing-slash URLs match after normalisation.
 *
 * `composition` and `composition/:panelTab` are deliberately two route configs on one component,
 * as are req_and_capabilities and req_and_capabilities_editable: shouldReuseRoute compares
 * `future.routeConfig === curr.routeConfig`, so distinct configs give the component-recreating
 * behaviour that the old distinct states had, while a change of :panelTab alone reuses it.
 */
const workspaceChildren: Routes = [
    {path: '', pathMatch: 'full', redirectTo: 'general'},
    {
        path: 'general',
        component: GeneralTabComponent,
        // NOT UnsavedChangesGuard: the General tab implements neither half of that guard's
        // `UnsavedChangesAware` contract, so it would find hasChangedData === undefined and allow
        // every navigation. It raises a boolean flag instead — see UnsavedChangesFlagGuard.
        canDeactivate: [UnsavedChangesFlagGuard],
        data: {bodyClass: 'general'}
    },
    {path: 'information_artifacts', component: InformationArtifactPageComponent},
    {path: 'tosca_artifacts', component: ToscaArtifactPageComponent},
    {path: 'deployment_artifacts', component: DeploymentArtifactsPageComponent},
    {path: 'properties', component: WorkspacePropertiesTabComponent, data: {bodyClass: 'properties'}},
    {
        path: 'properties_assignment',
        component: PropertiesAssignmentComponent,
        canDeactivate: [UnsavedChangesGuard],
        data: {bodyClass: 'properties-assignment'}
    },
    {path: 'attributes', component: AttributesComponent},
    {
        path: 'attributes_outputs',
        component: AttributesOutputsComponent,
        canDeactivate: [UnsavedChangesGuard],
        data: {bodyClass: 'attributes-outputs'}
    },
    {path: 'req_and_capabilities', component: ReqAndCapabilitiesComponent},
    {path: 'req_and_capabilities_editable', component: ReqAndCapabilitiesComponent},
    {path: 'management_workflow', component: ManagementWorkflowTabComponent, data: {bodyClass: 'management_workflow'}},
    {path: 'network_call_flow', component: NetworkCallFlowTabComponent, data: {bodyClass: 'network_call_flow'}},
    {path: 'composition', component: CompositionPageComponent, data: {bodyClass: 'composition'}},
    {path: 'composition/:panelTab', component: CompositionPageComponent, data: {bodyClass: 'composition'}},
    {path: 'activity_log', component: ActivityLogComponent},
    {path: 'distribution', component: DistributionComponent},
    {path: 'deployment', component: DeploymentPageComponent},
    {path: 'interface_operation', component: InterfaceOperationComponent, data: {bodyClass: 'interface_operation'}},
    {path: 'interfaceDefinition', component: InterfaceDefinitionComponent, data: {bodyClass: 'interfaceDefinition'}},
    {path: 'plugins/:path', component: PluginContextViewPageComponent}
];

/**
 * `data.title` reproduces app.ts:731-738's WCAG 2.4.2 page-title map, which keyed off the state's
 * BASE name — so every workspace tab shared one title and everything outside the three entries
 * fell back to 'SDC'. Only these three carry a title for that reason; RouteMetadataService applies
 * the fallback.
 */
export const routes: Routes = [
    {
        path: 'dashboard', component: HomeComponent, canActivate: [AuthGuard],
        data: {permissions: ['DESIGNER'], title: 'SDC - Dashboard'}
    },
    {path: 'catalog', component: CatalogComponent, canActivate: [AuthGuard], data: {title: 'SDC - Catalog'}},
    {path: 'adminDashboard', component: AdminDashboardComponent, canActivate: [AuthGuard], data: {permissions: ['ADMIN']}},
    {path: 'onboardVendor', component: OnboardVendorPageComponent, canActivate: [AuthGuard]},
    {path: 'plugins/:path', component: PluginTabViewPageComponent, canActivate: [AuthGuard]},
    {path: 'error-403', component: Error403PageComponent},

    {path: ':previousState/type-workspace/:type/:id/:subPage', component: TypeWorkspaceComponent, canActivate: [AuthGuard]},

    // CREATE mode. ui-router matched the id-less form as '/dashboard/workspace//service/general' —
    // a DOUBLE slash. DefaultUrlSerializer.parse() drops an empty segment AND everything after it,
    // so that URL is unrepresentable here; the single-slash form is the replacement. Declared BEFORE
    // the id-bearing route: the two consume a different segment count so order alone disambiguates,
    // and with :id absent WorkspaceComponentResolver takes its create-an-empty-component branch.
    //
    // Both entries are spelled out in full rather than sharing a `workspaceShell` const via
    // Object.assign(). Under AOT the metadata collector statically evaluates the array passed to
    // RouterModule.forRoot(); it cannot evaluate a function call, so it DROPS such an element
    // silently — no warning, no build error. That deleted both routes and all 20 tab children from
    // the compiled ROUTES provider, sending every workspace URL to the '**' → dashboard fallback.
    // Only object literals are safe here.
    //
    // 'paramsChange' below is the router default, stated for the record, and NOT 'always'. Measured
    // on router 5.2.11: `equalParamsAndUrlSegments` recurses into parents, so 'paramsChange' already
    // re-runs the resolve when only :id changes — a version switch or the post-create redirect
    // refetches, which is the behaviour that matters. 'always' would additionally refetch the
    // component on every TAB switch, a server round-trip per click that ui-router never made: it
    // declared `injectComponent` once on the parent state (app.ts:201), so tab switches reused it.
    {
        path: ':previousState/workspace/:type',
        component: WorkspaceContainerComponent,
        resolve: {component: WorkspaceComponentResolver},
        runGuardsAndResolvers: 'paramsChange' as 'paramsChange',
        data: {bodyClass: 'workspace', title: 'SDC - Workspace'},
        children: workspaceChildren
    },
    {
        path: ':previousState/workspace/:id/:type',
        component: WorkspaceContainerComponent,
        resolve: {component: WorkspaceComponentResolver},
        runGuardsAndResolvers: 'paramsChange' as 'paramsChange',
        data: {bodyClass: 'workspace', title: 'SDC - Workspace'},
        children: workspaceChildren
    },

    // Replaces the 'workspace-old' state and its oldWorkspaceController (app.ts:110-122), which
    // rewrote bare /workspace/* URLs to /catalog/workspace/*. Declared AFTER the two shell routes
    // so it can only catch URLs with no :previousState segment.
    //
    // The path deliberately has NO wildcard tail even though the old url did
    // ('/workspace/:id/:type/*workspaceInnerPath'). Measured on router 5.2.11: this form already
    // carries the whole remaining tail across the redirect (…/composition/details survives intact),
    // whereas adding '/**' makes every such URL fall through to '**' and land on /dashboard.
    {path: 'workspace/:id/:type', redirectTo: 'catalog/workspace/:id/:type'},

    {path: '', pathMatch: 'full', redirectTo: 'dashboard'},
    // Mirrors $urlRouterProvider.otherwise('dashboard') (app.ts).
    {path: '**', redirectTo: 'dashboard'}
];
