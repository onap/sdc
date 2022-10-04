const SDC_MENU_CONFIG = {
    "roles": {
        "ADMIN": {
            "title": "Admin's Workspace",
        },
        "DESIGNER": {
            "title": "Designer's Workspace",
            "dashboard": {
                "showCreateNew": true
            },
            "changeLifecycleStateButtons": {
                "NOT_CERTIFIED_CHECKOUT": {
                    "RESOURCE": {
                        "certify": {
                            "text": "Certify",
                            "url": "lifecycleState/certify",
                            "confirmationModal": "lifecycleState/certify"
                        },
                        "checkIn": {
                            "text": "Check in",
                            "url": "lifecycleState/CHECKIN",
                            "confirmationModal": "lifecycleState/CHECKIN"
                        },
                        "deleteVersion": {
                            "text": "Delete Version",
                            "url": "lifecycleState/UNDOCHECKOUT",
                            "alertModal": "lifecycleState/UNDOCHECKOUT"
                        }
                    },
                    "SERVICE": {
                        "certify": {
                            "text": "Certify",
                            "url": "lifecycleState/certify",
                            "confirmationModal": "lifecycleState/certify"
                        },
                        "checkIn": {
                            "text": "Check in",
                            "url": "lifecycleState/CHECKIN",
                            "confirmationModal": "lifecycleState/CHECKIN"
                        },
                        "deleteVersion": {
                            "text": "Delete Version",
                            "url": "lifecycleState/UNDOCHECKOUT",
                            "alertModal": "lifecycleState/UNDOCHECKOUT"
                        }
                    }

                },
                "CERTIFIED": {
                    "RESOURCE": {
                        "checkOut": {"text": "Check Out", "url": "lifecycleState/CHECKOUT"}
                    },
                    "SERVICE": {
                        "checkOut": {"text": "Check Out", "url": "lifecycleState/CHECKOUT"}
                    }
                },
                "NOT_CERTIFIED_CHECKIN": {
                    "RESOURCE": {
                        "certify": {
                            "text": "Certify",
                            "url": "lifecycleState/certify",
                            "confirmationModal": "lifecycleState/certify"
                        },
                        "checkOut": {"text": "Check Out", "url": "lifecycleState/CHECKOUT"}
                    },
                    "SERVICE": {
                        "certify": {
                            "text": "Certify",
                            "url": "lifecycleState/certify",
                            "confirmationModal": "lifecycleState/certify"
                        },
                        "checkOut": {"text": "Check Out", "url": "lifecycleState/CHECKOUT"}
                    }
                },
                "DISTRIBUTION_NOT_APPROVED": {
                    "RESOURCE":{},
                    "SERVICE": {
                        "distribute": {
                            "text": "Distribute",
                            "url": "distribution/PROD/activate",
                            "conformanceLevelModal": {
                                "url": "distribution-state/reject",
                                "confirmationModal": "distribution-state/reject"
                            }
                        },
                        "checkOut": {"text": "Check Out", "url": "lifecycleState/CHECKOUT"}
                    }
                },
                "DISTRIBUTED": {
                    "RESOURCE":{},
                    "SERVICE": {
                        "redistribute": {
                            "text": "Redistribute",
                            "url": "distribution/PROD/activate",
                            "conformanceLevelModal": {
                                "url": "distribution-state/reject",
                                "confirmationModal": "distribution-state/reject"
                            }
                        },
                        "checkOut": {"text": "Check Out", "url": "lifecycleState/CHECKOUT"}
                    }
                }
            },
            "folder": [
                {"text": "Active Projects", "groupname": "IN_PROGRESS"},
                {"text": "Check Out", "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKOUT"},
                {"text": "Check In", "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKIN"},
                {"text": "Followed Projects", "groupname": "FOLLOWING"},
                {"text": "Certified", "group": "FOLLOWING", "state": "CERTIFIED"},
                {"text": "Distributed", "group": "FOLLOWING", "state": "DISTRIBUTED"}
            ]

        }
    },
    "confirmationMessages": {
        "lifecycleState/CHECKIN": {
            "showComment": true,
            "title": "Check in confirmation",
            "message": "Please add comment and confirm the check in."
        },
        "lifecycleState/CHECKOUT": {
            "showComment": true,
            "title": "Check out confirmation",
            "message": "Please add comment and confirm the check out."
        },
        "lifecycleState/certify": {
            "showComment": true,
            "title": "Certification confirmation",
            "message": "Please add comment and confirm test results."
        },
        "updateTemplate": {
            "showComment": false,
            "title": "Update Template Confirmation",
            "message": "Modifying the Template might cause losing of previous information"
        }
    },
    "alertMessages": {
        "lifecycleState/UNDOCHECKOUT": {
            "title": "Delete Version Confirmation",
            "message": "Are you sure you want to delete this version?"
        },
        "exitWithoutSaving": {
            "title": "Exit Without Saving Confirmation",
            "message": "All unsaved changes will be lost. Are you sure you want to exit this page?"
        },
        "upgradeInstance": {
            "title": "Switch Versions",
            "message": "Switching versions will erase service paths: %1. Are you sure you want to proceed?"
        },
        "deleteInstance": {"title": "Delete Confirmation", "message": "Are you sure you would like to delete %1?"},
        "deleteInput": {"title": "Delete Confirmation", "message": "Are you sure you would like to delete %1?"},
        "okButton": "OK"
    },
    "statuses": {
        "inDesign": {
            "name": "In Design",
            "values": [
                "NOT_CERTIFIED_CHECKOUT",
                "NOT_CERTIFIED_CHECKIN"
            ]
        },
        "certified": {
            "name": "Certified",
            "values": ["CERTIFIED"]
        },
        "distributed": {
            "name": "Distributed",
            "values": ["DISTRIBUTED"]
        }
    },
    "categoriesDictionary": {
        "Mobility": "Application Layer 4+",
        "Network L1-3": "Network Layer 2-3",
        "Network L4": "Network Layer 4+",
        "VoIP Call Control": "Application Layer 4+"
    },
    "catalogMenuItem": {
        "DESIGNER": {
            "states": {
                "NOT_CERTIFIED_CHECKOUT": {
                    "ANY": []
                },
                "NOT_CERTIFIED_CHECKIN": {
                    "ANY": []
                },
                "CERTIFIED": {
                    "ANY": []
                }
            }
        },
        "OTHER": {
            "states": {
                "ANY": {
                    "ANY": []
                }
            }
        }
    },
    "LifeCycleStatuses": {
        "NOT_CERTIFIED_CHECKOUT": {"text": "In Design Check Out", "icon": "checkout-editable-status-icon"},
        "NOT_CERTIFIED_CHECKIN": {"text": "In Design Check In", "icon": "checkin-status-icon "},
        "CERTIFIED": {"text": "Certified", "icon": "checkin-status-icon "}
    },
    "DistributionStatuses": {
        "DISTRIBUTION_NOT_APPROVED": {"text": "Waiting For Distribution"},
        "DISTRIBUTED": {"text": "Distributed"}
    },
    "canvas_buttons": {
        "checkIn": {
            "text": "Check in",
            "action": "changeLifecycleState",
            "url": "lifecycleState/CHECKIN",
            "confirmationModal": "lifecycleState/CHECKIN"
        },
        "deleteVersion": {
            "text": "Delete Version",
            "action": "changeLifecycleState",
            "url": "lifecycleState/UNDOCHECKOUT",
            "alertModal": "lifecycleState/UNDOCHECKOUT"
        }
    },

    "component_workspace_menu_option": {
        "VFC": [
            {"text": "General", "action": "onMenuItemPressed", "state": "workspace.general"},
            {"text": "Deployment Artifact", "action": "onMenuItemPressed", "state": "workspace.deployment_artifacts"},
            {"text": "Information Artifact", "action": "onMenuItemPressed", "state": "workspace.information_artifacts"},
            {"text": "TOSCA Artifacts", "action": "onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text": "Properties", "action": "onMenuItemPressed", "state": "workspace.properties"},
            {"text": "Attributes", "action": "onMenuItemPressed", "state": "workspace.attributes"},
            {"text": "Interfaces", "action": "onMenuItemPressed", "state": "workspace.interface-definition"},
            {"text": "Req. & Capabilities", "action": "onMenuItemPressed", "state": "workspace.reqAndCap"},
            {"text": "Activity Log", "action": "onMenuItemPressed", "state": "workspace.activity_log"}
        ],
        "VL": [
            {"text": "General", "action": "onMenuItemPressed", "state": "workspace.general"},
            {"text": "Deployment Artifact", "action": "onMenuItemPressed", "state": "workspace.deployment_artifacts"},
            {"text": "Information Artifact", "action": "onMenuItemPressed", "state": "workspace.information_artifacts"},
            {"text": "TOSCA Artifacts", "action": "onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text": "Properties", "action": "onMenuItemPressed", "state": "workspace.properties"},
            {"text": "Attributes", "action": "onMenuItemPressed", "state": "workspace.attributes"},
            {"text": "Req. & Capabilities", "action": "onMenuItemPressed", "state": "workspace.reqAndCap"},
            {"text": "Activity Log", "action": "onMenuItemPressed", "state": "workspace.activity_log"}
        ],
        "CP": [
            {"text": "General", "action": "onMenuItemPressed", "state": "workspace.general"},
            {"text": "Deployment Artifact", "action": "onMenuItemPressed", "state": "workspace.deployment_artifacts"},
            {"text": "Information Artifact", "action": "onMenuItemPressed", "state": "workspace.information_artifacts"},
            {"text": "TOSCA Artifacts", "action": "onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text": "Properties", "action": "onMenuItemPressed", "state": "workspace.properties"},
            {"text": "Attributes", "action": "onMenuItemPressed", "state": "workspace.attributes"},
            {"text": "Req. & Capabilities", "action": "onMenuItemPressed", "state": "workspace.reqAndCap"},
            {"text": "Activity Log", "action": "onMenuItemPressed", "state": "workspace.activity_log"}
        ],
        "VF": [
            {"text": "General", "action": "onMenuItemPressed", "state": "workspace.general"},
            {"text": "Deployment Artifact", "action": "onMenuItemPressed", "state": "workspace.deployment_artifacts"},
            {"text": "Information Artifact", "action": "onMenuItemPressed", "state": "workspace.information_artifacts"},
            {"text": "TOSCA Artifacts", "action": "onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text": "Composition", "action": "onMenuItemPressed", "state": "workspace.composition.details"},
            {"text": "Operation", "action":"onMenuItemPressed", "state": "workspace.interface_operation"},
            {"text": "Activity Log", "action": "onMenuItemPressed", "state": "workspace.activity_log"},
            {"text": "Deployment", "action": "onMenuItemPressed", "state": "workspace.deployment"},
            {"text": "Properties Assignment", "action": "onMenuItemPressed", "state": "workspace.properties_assignment"},
            {"text": "Attributes & Outputs", "action": "onMenuItemPressed", "state": "workspace.attributes_outputs"},
            {"text": "Req. & Capabilities", "action": "onMenuItemPressed", "state": "workspace.reqAndCapEditable"}
        ],
        "PNF": [
            {"text": "General", "action": "onMenuItemPressed", "state": "workspace.general"},
            {"text": "Deployment Artifact", "action": "onMenuItemPressed", "state": "workspace.deployment_artifacts"},
            {"text": "Information Artifact", "action": "onMenuItemPressed", "state": "workspace.information_artifacts"},
            {"text": "TOSCA Artifacts", "action": "onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text": "Composition", "action": "onMenuItemPressed", "state": "workspace.composition.details"},
            {"text": "Operation", "action": "onMenuItemPressed", "state": "workspace.interface_operation"},
            {"text": "Activity Log", "action": "onMenuItemPressed", "state": "workspace.activity_log"},
            {"text": "Properties Assignment", "action": "onMenuItemPressed", "state": "workspace.properties_assignment"},
            {"text": "Req. & Capabilities", "action": "onMenuItemPressed", "state": "workspace.reqAndCapEditable"}
        ],
        "CR": [
            {"text": "General", "action": "onMenuItemPressed", "state": "workspace.general"},
            {"text": "Deployment Artifact", "action": "onMenuItemPressed", "state": "workspace.deployment_artifacts"},
            {"text": "Information Artifact", "action": "onMenuItemPressed", "state": "workspace.information_artifacts"},
            {"text": "TOSCA Artifacts", "action": "onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text": "Composition", "action": "onMenuItemPressed", "state": "workspace.composition.details", "disabledCategories":["Partner Domain Service"]},
            {"text": "Operation", "action":"onMenuItemPressed", "state": "workspace.interface_operation"},
            {"text": "Activity Log", "action": "onMenuItemPressed", "state": "workspace.activity_log"},
            {"text": "Properties Assignment", "action": "onMenuItemPressed", "state": "workspace.properties_assignment"},
            {"text": "Req. & Capabilities", "action": "onMenuItemPressed", "state": "workspace.reqAndCapEditable"}
        ],
        "SERVICE": [
            {"text": "General", "action": "onMenuItemPressed", "state": "workspace.general", "hiddenCategories":["Partner Domain Service"]},
            {"text": "TOSCA Artifacts", "action": "onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text": "Composition", "action": "onMenuItemPressed", "state": "workspace.composition.details", "disabledCategories":["Partner Domain Service"]},
            {"text": "Operation", "action":"onMenuItemPressed", "state": "workspace.interface_operation"},
            {"text": "Activity Log", "action": "onMenuItemPressed", "state": "workspace.activity_log"},
            {"text": "Management Workflow", "action": "onMenuItemPressed", "state": "workspace.management_workflow"},
            {"text": "Network Call Flow ", "action": "onMenuItemPressed", "state": "workspace.network_call_flow"},
            {"text": "Distribution","action": "onMenuItemPressed","state": "workspace.distribution","disabledRoles": ["ADMIN"]},
            {"text": "Deployment", "action": "onMenuItemPressed", "state": "workspace.deployment"},
            {"text": "Properties Assignment", "action": "onMenuItemPressed", "state": "workspace.properties_assignment"},
            {"text": "Attributes & Outputs", "action": "onMenuItemPressed", "state": "workspace.attributes_outputs"}
        ],
        "DataType": [
            {"text": "General", "action": "onMenuItemPressed", "state": "general"},
            {"text": "Properties", "action": "onMenuItemPressed", "state": "properties"},
        ]
    }

};

module.exports = SDC_MENU_CONFIG;
