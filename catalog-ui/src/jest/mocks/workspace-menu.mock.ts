export function createMockSdcMenu(): any {
    return {
        roles: {
            DESIGNER: {
                changeLifecycleStateButtons: {
                    NOT_CERTIFIED_CHECKOUT: {
                        RESOURCE: {checkin: {text: 'Check in', url: 'lifecycleState/CHECKIN', confirmationModal: 'checkin'}},
                        SERVICE: {checkin: {text: 'Check in', url: 'lifecycleState/CHECKIN', confirmationModal: 'checkin'}}
                    },
                    NOT_CERTIFIED_CHECKIN: {
                        RESOURCE: {
                            checkout: {text: 'Check Out', url: 'lifecycleState/CHECKOUT'},
                            certify: {text: 'Certify', url: 'lifecycleState/certify'}
                        },
                        SERVICE: {
                            checkout: {text: 'Check Out', url: 'lifecycleState/CHECKOUT'},
                            certify: {text: 'Certify', url: 'lifecycleState/certify'}
                        }
                    },
                    CERTIFIED: {
                        RESOURCE: {checkout: {text: 'Check Out', url: 'lifecycleState/CHECKOUT'}},
                        SERVICE: {checkout: {text: 'Check Out', url: 'lifecycleState/CHECKOUT'}}
                    },
                    DISTRIBUTION_NOT_APPROVED: {
                        SERVICE: {distribute: {text: 'Distribute', url: 'distribution/PROD/activate'}}
                    }
                }
            },
            TESTER: {
                changeLifecycleStateButtons: {}
            }
        },
        component_workspace_menu_option: {
            VF: [
                {text: 'General', action: 'onMenuItemPressed', state: 'workspace.general'},
                {text: 'Deployment Artifact', action: 'onMenuItemPressed', state: 'workspace.deployment_artifacts'},
                {text: 'Composition', action: 'onMenuItemPressed', state: 'workspace.composition'}
            ],
            VFC: [
                {text: 'General', action: 'onMenuItemPressed', state: 'workspace.general'},
                {text: 'Composition', action: 'onMenuItemPressed', state: 'workspace.composition'}
            ],
            PNF: [
                {text: 'General', action: 'onMenuItemPressed', state: 'workspace.general'},
                {text: 'Composition', action: 'onMenuItemPressed', state: 'workspace.composition'}
            ],
            SERVICE: [
                {text: 'General', action: 'onMenuItemPressed', state: 'workspace.general'},
                {text: 'Composition', action: 'onMenuItemPressed', state: 'workspace.composition'},
                {text: 'Distribution', action: 'onMenuItemPressed', state: 'workspace.distribution'}
            ]
        },
        alertMessages: {okButton: 'OK'},
        confirmationMessages: {
            checkin: {showComment: true, title: 'Check In', message: 'Check in %1?'},
            certify: {showComment: true, title: 'Certify', message: 'Certify %1?'}
        }
    };
}
