import {Component, EventEmitter, Inject, Input, OnInit, Output} from "@angular/core";
import {URLSearchParams} from '@angular/http';
import {Plugin} from "app/models";
import {EventBusService} from "../../../services/event-bus.service";
import {PluginsService} from "../../../services/plugins.service";

@Component({
    selector: 'plugin-frame',
    templateUrl: './plugin-frame.component.html',
    styleUrls: ['plugin-frame.component.less']
})

export class PluginFrameComponent implements OnInit {

    @Input() plugin: Plugin;
    @Input() queryParams: Object;
    @Output() onLoadingDone: EventEmitter<void> = new EventEmitter<void>();
    pluginUrl: string;
    private urlSearchParams: URLSearchParams;
    private isClosed: boolean;
    private isPluginCheckDone: boolean;

    constructor(private eventBusService: EventBusService,
                private pluginsService: PluginsService,
                @Inject('$scope') private $scope: ng.IScope,
                @Inject('$state') private $state: ng.ui.IStateService) {
        this.urlSearchParams = new URLSearchParams();
        this.isPluginCheckDone = false;
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
                    this.onLoadingDone.emit();
                    this.eventBusService.off(readyEvent)
                }
            }
        };

        this.eventBusService.on(readyEvent);

        // Listening to the stateChangeStart event in order to notify the plugin about it being closed
        // before moving to a new state
        this.$scope.$on('$stateChangeStart', (event, toState, toParams, fromState, fromParams) => {
            if ((fromState.name !== toState.name) || (fromState.name === toState.name) && (toParams.path !== fromParams.path)) {
                if (this.eventBusService.NoWindowOutEvents.indexOf(this.eventBusService.lastEventNotified) == -1) {
                    if (!this.isClosed) {
                        event.preventDefault();

                        this.eventBusService.notify("WINDOW_OUT").subscribe(() => {
                            this.isClosed = true;
                            this.eventBusService.unregister(this.plugin.pluginId);

                            this.$state.go(toState.name, toParams);
                        });
                    }
                } else {
                    this.eventBusService.unregister(this.plugin.pluginId);
                }
            }
        });
    }
}
