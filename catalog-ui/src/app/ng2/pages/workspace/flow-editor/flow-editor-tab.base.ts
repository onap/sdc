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
import {AfterViewInit, ChangeDetectorRef, ElementRef, OnDestroy, OnInit} from '@angular/core';
import {WorkspaceMode} from 'app/utils/constants';
import {CacheService} from 'app/services-ng2';
import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';
import {WorkspaceService} from '../workspace.service';
import {VendorModel} from './vendor-model';
// Side-effect import: defines the global window.PunchOutRegistry used below. The old AngularJS
// <punch-out> directive carried the only require() of this bundle; it was removed with the
// directive layer, so this consumer must load it itself (the IIFE self-guards, so importing it
// from more than one site is idempotent).
import '../../../../../third-party/PunchOutRegistry.js';

declare const window: any;

/** Marker class on the host <div> the React punch-out is mounted into (see the shared template). */
const HOST_SELECTOR = '.flow-editor-punch-out-host';

/**
 * Shared base for the two SDC-internal "flow editor" workspace tabs (management-workflow and
 * network-call-flow). Both render the same internal React component (FlowsListEditor, the
 * "sequence-diagram" punch-out) — they differ only in diagram type, which artifacts they fetch,
 * and how their participant lane-list is built.
 *
 * Migration pattern (Phase 5, mirrors GeneralTabComponent / Phase 3 "Mechanism B"):
 *  - read the working component from the shared WorkspaceService in ngOnInit (NOT via @Input — the
 *    downgraded component is rendered directly by a ui-router state with propagateDigest:false,
 *    which never delivers inputs);
 *  - mount the React punch-out by calling the global window.PunchOutRegistry DIRECTLY (the old
 *    AngularJS <punch-out> directive was only a thin wrapper over the same registry, and that
 *    directive is pure AngularJS — it cannot be compiled inside an Angular template);
 *  - OnPush change detection + detectChangesSafe() after the async fetch/mount so the loader clears;
 *  - unmount the React tree in ngOnDestroy (or its Redux store leaks).
 *
 * The host element is resolved by querySelector on the component's own ElementRef rather than
 * @ViewChild: query-decorator inheritance from a selectorless abstract base is unreliable under
 * Angular 5 AOT, whereas constructor DI (ElementRef) inherits cleanly via super(...). The actual
 * render is deferred to ngAfterViewInit (or later) because PunchOutRegistry.loadOnBoarding invokes
 * its callback SYNCHRONOUSLY once the onboarding bundle is cached (a return visit), which would
 * otherwise run mount() inside ngOnInit before the template's host <div> exists.
 */
export abstract class FlowEditorTabBase implements OnInit, AfterViewInit, OnDestroy {

    isLoading: boolean = true;
    protected component: any;

    private destroyed: boolean = false;
    private viewReady: boolean = false;
    private mounted: boolean = false;
    private pendingModel: VendorModel;

    constructor(protected workspaceService: WorkspaceService,
                protected cacheService: CacheService,
                protected componentService: ComponentServiceNg2,
                protected cdr: ChangeDetectorRef,
                protected el: ElementRef,
                protected $injector: any,
                protected sdcConfig: any) {
    }

    ngOnInit(): void {
        this.component = this.workspaceService.component;
        if (!this.component) {
            this.isLoading = false;
            return;
        }
        // Lazy-load the onboarding punch-out bundle (registers the "sequence-diagram" factory), then
        // fetch the tab's data and build the model. Mirrors the old view-models' constructor.
        window['PunchOutRegistry'].loadOnBoarding(() => {
            if (this.destroyed) { return; }
            this.loadAndMount();
        });
    }

    ngAfterViewInit(): void {
        this.viewReady = true;
        // If the data was already fetched synchronously (cached bundle + cached artifacts), render now
        // that the host <div> exists.
        this.tryRender();
    }

    ngOnDestroy(): void {
        this.destroyed = true;
        const host = this.getHost();
        if (host && this.mounted) {
            // The registry logs an error if unmount is called before a render resolved — guard so a
            // half-initialised tab does not throw on teardown.
            try {
                window['PunchOutRegistry'].unmount(host);
            } catch (e) {
                // no-op: nothing was mounted yet
            }
        }
    }

    /** Subclass: fetch the artifacts (+ instances for network-call-flow) it needs, then call
     *  render(this.buildVendorModel()). Guarded so a present artifacts cache skips the fetch. */
    protected abstract loadAndMount(): void;

    /** Subclass: build the VendorModel with its diagram type + participants. */
    protected abstract buildVendorModel(): VendorModel;

    /** Hand the built model to the punch-out; the actual ReactDOM.render is deferred until the view
     *  is ready (see tryRender). */
    protected render(model: VendorModel): void {
        this.pendingModel = model;
        this.isLoading = false;
        this.tryRender();
        this.detectChangesSafe();
    }

    private tryRender(): void {
        if (this.destroyed || this.mounted || !this.viewReady || !this.pendingModel) { return; }
        const host = this.getHost();
        if (!host) { return; }
        const props = {
            name: 'sequence-diagram',
            options: {
                data: this.pendingModel,
                apiRoot: this.sdcConfig.api.root,
                apiHeaders: this.buildApiHeaders()
            },
            // The old templates bound on-event="onVendorEvent", but neither flow view-model ever
            // defined onVendorEvent (only onboard-vendor does) — the binding resolved to undefined.
            // The host consumes no events from the flows editor, so this is intentionally a no-op.
            onEvent: () => { /* no-op */ }
        };
        window['PunchOutRegistry'].render(props, host);
        this.mounted = true;
    }

    private getHost(): any {
        const root = this.el && this.el.nativeElement;
        return root ? root.querySelector(HOST_SELECTOR) : null;
    }

    /** Build the same user-cookie apiHeaders the old AngularJS punch-out directive built, sourcing the
     *  user from CacheService (the workspace shim never set $scope.user, so the old binding was blank). */
    private buildApiHeaders(): any {
        const user = this.cacheService.get('user') || {};
        const cookie = this.sdcConfig.cookie;
        return {
            userId: {name: cookie.userIdSuffix, value: user.userId},
            userFirstName: {name: cookie.userFirstName, value: user.firstName},
            userLastName: {name: cookie.userLastName, value: user.lastName},
            userEmail: {name: cookie.userEmail, value: user.email}
        };
    }

    /** VIEW (or CREATE) → the flows editor is read-only; only an active EDIT checkout is editable.
     *  Mirrors the old $scope.isViewMode() that fed VendorModel.readonly. */
    protected get readonly(): boolean {
        return this.workspaceService.getComponentMode(this.component) !== WorkspaceMode.EDIT;
    }

    protected get userId(): string {
        const user = this.cacheService.get('user') || {};
        return user.userId;
    }

    protected generateRequestId(): string {
        return this.$injector.get('uuid4').generate();
    }

    protected detectChangesSafe(): void {
        if (!(this.cdr as any).destroyed) {
            this.cdr.detectChanges();
        }
    }
}
