import {BasePubSub} from "./base-pubsub";

declare const window: Window;

export class PluginPubSub extends BasePubSub {

    constructor(pluginId: string, parentUrl: string, eventsToWait?: Array<string>) {
        super(pluginId);
        this.register('sdc-hub', window.parent, parentUrl);
        this.subscribe(eventsToWait);
    }

    public subscribe(eventsToWait?: Array<string>) {
        const registerData = {
            pluginId: this.clientId,
            eventsToWait: eventsToWait || []
        };

        this.notify('PLUGIN_REGISTER', registerData);
    }

    public unsubscribe() {
        const unregisterData = {
            pluginId: this.clientId
        };

        this.notify('PLUGIN_UNREGISTER', unregisterData);
    }
}
