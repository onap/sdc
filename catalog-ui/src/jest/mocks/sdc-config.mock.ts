import {ISdcConfig} from 'app/ng2/config/sdc-config.config';

export const mockSdcConfig: ISdcConfig = {
    api: {
        root: '/sdc2/rest',
        no_proxy_root: '/sdc2/rest',
        component_api_root: '/v1/catalog/',
        GET_user_authorize: '/v1/user/authorize',
        GET_element: '/v1/followed',
        GET_uicache_catalog: '/v1/catalog',
        GET_plugin_online_state: '/v1/plugins/:pluginId/online',
        uicache_root: '/sdc-ui-cache/rest'
    },
    cookie: {
        userFirstName: 'HTTP_CSP_FIRSTNAME',
        userLastName: 'HTTP_CSP_LASTNAME',
        userEmail: 'HTTP_CSP_EMAIL',
        userIdSuffix: 'HTTP_CSP_ATTUID',
        junctionName: 'HTTP_IV_REMOTE_ADDRESS'
    }
};
