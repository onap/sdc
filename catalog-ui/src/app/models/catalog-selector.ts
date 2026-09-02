/* added Michael */
// export interface ILeftSwitchItemModel {
export interface ICatalogSelector{    
    value: CatalogSelectorTypes;
    title: string;
    header: string;
    hidden?: number;
    disabled?: number;
}

export enum CatalogSelectorTypes {
    Active,
    Archive,
}