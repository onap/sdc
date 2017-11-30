declare const __ENV__:string;

export interface ISdcConfig {
    [index:string]: any
}

export function getSdcConfig() : ISdcConfig {
    let sdcConfig:ISdcConfig = {};

    if (__ENV__==='prod') {
        sdcConfig = require('./../../../../configurations/prod.js');
    } else {
        sdcConfig = require('./../../../../configurations/dev.js');
    }

    return sdcConfig;
}
