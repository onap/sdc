import {IAppMenu} from "app/models";

export function getSdcMenu() : IAppMenu{
    const sdcMenu:IAppMenu = require('./../../../../configurations/menu.js');
    return sdcMenu;
}
