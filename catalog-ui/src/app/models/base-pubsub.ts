declare const window: Window;

export class BasePubSub {

    subscribers: Map<string, ISubscriber>;
    eventsCallbacks: Array<Function>;
    clientId: string;

    constructor(pluginId: string) {
        this.subscribers = new Map<string, ISubscriber>();
        this.eventsCallbacks = new Array<Function>();
        this.clientId = pluginId;
        this.onMessage = this.onMessage.bind(this);

        window.addEventListener("message", this.onMessage);
    }

    public register(subscriberId: string, subscriberWindow: Window, subscriberUrl: string) {
        const subscriber = {
            window: subscriberWindow,
            locationUrl: subscriberUrl || subscriberWindow.location.href
        } as ISubscriber;

        this.subscribers.set(subscriberId, subscriber);
    }

    public unregister(subscriberId: string) {
        this.subscribers.delete(subscriberId);
    }

    public on(callback: Function) {
        this.eventsCallbacks.push(callback);
    }

    public off(callback: Function) {
        let index = this.eventsCallbacks.indexOf(callback);
        this.eventsCallbacks.splice(index, 1)
    }

    public notify(eventType:string, eventData:any) {
        let eventObj = {
            type: eventType,
            data: eventData,
            originId: this.clientId
        } as IPubSubEvent;

        this.subscribers.forEach( (subscriber: ISubscriber, id: string) => {
            subscriber.window.postMessage(eventObj, subscriber.locationUrl)
        });
    }

    protected onMessage(event: any) {
        if (this.subscribers.has(event.data.originId)) {
            this.eventsCallbacks.forEach((callback: Function) => {
                callback(event.data, event);
            })
        }
    }
}

export class PluginPubSub extends BasePubSub {

    constructor(pluginId: string, subscriberUrl: string) {
        super(pluginId);
        this.register('sdc-hub', window.parent, subscriberUrl);
        this.subscribe();
    }

    public subscribe() {
        const registerData = {
            pluginId: this.clientId
        };

        this.notify('pluginRegister', registerData);
    }

    public unsubscribe() {
        const unregisterData = {
            pluginId: this.clientId
        };

        this.notify('pluginUnregister', unregisterData);
    }
}

export interface IPubSubEvent {
    type: string;
    originId: string;
    data: any;
}

export interface ISubscriber {
    window: Window;
    locationUrl: string;
}
