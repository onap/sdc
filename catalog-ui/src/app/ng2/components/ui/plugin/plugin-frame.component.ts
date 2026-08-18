import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {URLSearchParams} from '@angular/http';
import {Plugin} from "app/models";
import {EventBusService} from "../../../services/event-bus.service";
import {NavigationService} from "../../../services/navigation.service";
import {PluginsService} from "../../../services/plugins.service";

@Component({
    selector: 'plugin-frame',
    templateUrl: './plugin-frame.component.html',
    styleUrls: ['plugin-frame.component.less']
})

export class PluginFrameComponent implements OnInit, OnDestroy {

    @Input() plugin: Plugin;
    @Input() queryParams: Object;
    @Output() onLoadingDone: EventEmitter<void> = new EventEmitter<void>();
    pluginUrl: string;
    private urlSearchParams: URLSearchParams;
    private isClosed: boolean;
    private isReady: boolean;
    isPluginCheckDone: boolean;
    private stateChangeStartUnregister: Function;

    constructor(private eventBusService: EventBusService,
                private pluginsService: PluginsService,
                private navigationService: NavigationService) {
        this.urlSearchParams = new URLSearchParams();
        this.isPluginCheckDone = false;
    }

    ngOnDestroy(): void {
        // The navigation listener now lives on the root scope (NavigationService), not on this
        // component's own $scope, so it no longer dies with the component — deregister it here or
        // a stale handler keeps hijacking every later transition.
        if (this.stateChangeStartUnregister) {
            this.stateChangeStartUnregister();
            this.stateChangeStartUnregister = undefined;
        }
    }

    ngOnInit(): void {
        this.pluginsService.isPluginOnline(this.plugin.pluginId).subscribe(isPluginOnline => {
            this.plugin.isOnline = isPluginOnline;
            this.isPluginCheckDone = true;

            if (this.plugin.isOnline) {
                this.initPlugin();
            } else {
                this.onLoadingDone.emit();
            }
        });
    }

    private initPlugin() {
        this.pluginUrl = this.plugin.pluginSourceUrl;
        this.isClosed = false;
        this.isReady = false;

        if (this.queryParams && !_.isEmpty(this.queryParams)) {
            _.forOwn(this.queryParams, (value, key) => {
                this.urlSearchParams.set(key, value);
            });

            this.pluginUrl += '?';
            this.pluginUrl += this.urlSearchParams.toString();
        }

        let readyEvent = (eventData) => {
            if (eventData.originId === this.plugin.pluginId) {
                if (eventData.type == "READY") {
                    this.isReady = true;
                    this.onLoadingDone.emit();
                    this.eventBusService.off(readyEvent)
                }
            }
        };

        this.eventBusService.on(readyEvent);

        // Listening to the stateChangeStart event in order to notify the plugin about it being closed
        // before moving to a new state
        if (this.stateChangeStartUnregister) { this.stateChangeStartUnregister(); }
        this.stateChangeStartUnregister = this.navigationService.onNavigationStart((event) => {
            if ((event.fromState !== event.toState) || (event.fromState === event.toState) && (event.toParams.path !== event.fromParams.path)) {
                if (!this.isReady) {
                    this.onLoadingDone.emit();
                    this.eventBusService.off(readyEvent)
                }
                if (this.eventBusService.NoWindowOutEvents.indexOf(this.eventBusService.lastEventNotified) == -1) {
                    if (!this.isClosed) {
                        event.preventDefault();
                        this.eventBusService.notify("WINDOW_OUT").subscribe(() => {
                            this.isClosed = true;
                            this.eventBusService.unregister(this.plugin.pluginId);

                            this.navigationService.navigate(event.toState, event.toParams);
                        });
                    }
                } else {
                    this.eventBusService.unregister(this.plugin.pluginId);
                }
            }
        });
    }
}
