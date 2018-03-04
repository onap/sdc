export class ServicePathMapItem {
    constructor(public data: MapItemData, public id: string) {}
}

export class MapItemData {
    constructor(public name:string, public id: string,  public ownerId?: string, public options?: Array<ServicePathMapItem>) {}
}
