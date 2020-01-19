import {Injectable} from '@angular/core';
import {BasePubSub, IPubSubEvent} from "sdc-pubsub";

@Injectable()
export class EventBusService extends BasePubSub {

    NoWindowOutEvents: Array<string>;

    constructor() {
        super("sdc-hub");
        this.NoWindowOutEvents = ["CHECK_IN", "SUBMIT_FOR_TESTING", "UNDO_CHECK_OUT"];
    }

    public unregister(pluginId: string) {
        const unregisterData = {
            pluginId: pluginId
        };

        this.notify('PLUGIN_CLOSE', unregisterData).subscribe(() => {
            super.unregister(pluginId);
        });
    }

    public disableNavigation(isDisable: boolean) {
        let iframes = document.getElementsByClassName("plugin-iframe");

        if (isDisable) {
            _.forEach(iframes, (iframeElement: HTMLElement) => {
                iframeElement.style.zIndex = '1300';
            });
            let disableDiv = document.createElement('div');
            disableDiv.style.cssText = "position: fixed;\n" +
                "z-index: 1029;\n" +
                "background: rgba(0,0,0,0.5);\n" +
                "width: 100%;\n" +
                "height: 100%;\n" +
                "top: 0;\n" +
                "left: 0;";
            disableDiv.setAttribute("class", "disable-navigation-div");
            document.body.appendChild(disableDiv);
        } else {
            document.getElementsByClassName("disable-navigation-div")[0].remove();

            _.forEach(iframes, (iframeElement: HTMLElement) => {
                iframeElement.style.zIndex = '';
            });
        }
    }

    public notify(eventType: string, eventData?: any, disableOnWaiting: boolean = true) {
        let doDisable = false;

        if (disableOnWaiting) {
            doDisable = this.isWaitingForEvent(eventType);

            if (doDisable) {
                this.disableNavigation(true);
            }
        }

        const origSubscribe = super.notify(eventType, eventData).subscribe;

        return {
            subscribe: function (callbackFn) {
                origSubscribe(() => {
                    if (doDisable) {
                        this.disableNavigation(false);
                    }

                    callbackFn();
                });
            }.bind(this)
        };
    }

    protected handlePluginRegistration(eventData: IPubSubEvent, event: any) {
        if (eventData.type === 'PLUGIN_REGISTER') {
            this.register(eventData.data.pluginId, event.source, event.origin);

            let newEventsList = [];

            if (this.eventsToWait.has(eventData.data.pluginId)) {
                newEventsList = _.union(this.eventsToWait.get(eventData.data.pluginId), eventData.data.eventsToWait);
            } else {
                newEventsList = eventData.data.eventsToWait;
            }

            this.eventsToWait.set(eventData.data.pluginId, newEventsList);

        } else if (eventData.type === 'PLUGIN_UNREGISTER') {
            this.unregister(eventData.data.pluginId);
        }
    }

    protected onMessage(event: any) {
        if (event.data.type === 'PLUGIN_REGISTER') {
            this.handlePluginRegistration(event.data, event);
        }

        super.onMessage(event);

        if (event.data.type === 'PLUGIN_UNREGISTER') {
            this.handlePluginRegistration(event.data, event);
        }
    }
}
