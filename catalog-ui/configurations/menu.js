const SDC_MENU_CONFIG = {
    "roles":{
        "ADMIN":{
            "title": "Admin's Workspace",
            "pages":[],
            "states":{
                "NOT_CERTIFIED_CHECKOUT":{
                    "ANY":[
                        {"text":"Submit for Testing","action":"changeLifecycleState", "url":"lifecycleState/certificationRequest" , "emailModal": "lifecycleState/CERTIFICATIONREQUEST"}
                    ]
                },
                "NOT_CERTIFIED_CHECKIN": {
                    "ANY": [
                        {"text": "Submit for Testing", "action": "changeLifecycleState", "url": "lifecycleState/certificationRequest", "emailModal": "lifecycleState/CERTIFICATIONREQUEST"}
                    ]
                },
                "READY_FOR_CERTIFICATION": {
                    "ANY":[
                    ]
                },
                "CERTIFICATION_IN_PROGRESS":{
                    "ANY":[
                    ]
                },
                "CERTIFIED":{
                    "ANY":[
                    ]
                }
            }
        },
        "DESIGNER":{
            "title": "Designer's Workspace",
            "pages":[],
            "dashboard": {
                "showCreateNew": true,
                "showCreateNewProduct": false
            },
            "changeLifecycleStateButtons":{
                "NOT_CERTIFIED_CHECKOUT":{  "submitForTesting": {"text":"Submit for Testing", "url":"lifecycleState/certificationRequest", "emailModal": "lifecycleState/CERTIFICATIONREQUEST"},
                                            "checkIn":          {"text":"Check in",  "url":"lifecycleState/CHECKIN", "confirmationModal": "lifecycleState/CHECKIN"},
                                            "deleteVersion":    {"text":"Delete Version", "url":"lifecycleState/UNDOCHECKOUT", "alertModal": "lifecycleState/UNDOCHECKOUT"}
                },
                "CERTIFIED":{               "checkOut":         {"text":"Check Out",  "url":"lifecycleState/CHECKOUT"}
                },
                "NOT_CERTIFIED_CHECKIN":{   "submitForTesting": {"text":"Submit for Testing", "url":"lifecycleState/certificationRequest", "emailModal": "lifecycleState/CERTIFICATIONREQUEST"},
                                            "checkOut":         {"text":"Check Out",  "url":"lifecycleState/CHECKOUT"}
                }
            },
            "states":{
                "NOT_CERTIFIED_CHECKOUT":{
                    "ANY":[
                        {"text":"Submit for Testing","action":"changeLifecycleState", "url":"lifecycleState/certificationRequest", "emailModal": "lifecycleState/CERTIFICATIONREQUEST"}
                    ]
                },
                "NOT_CERTIFIED_CHECKIN": {
                    "ANY": [
                        {"text": "Submit for Testing", "action": "changeLifecycleState", "url": "lifecycleState/certificationRequest", "emailModal": "lifecycleState/CERTIFICATIONREQUEST"}
                    ]
                },
                "READY_FOR_CERTIFICATION": {
                    "ANY":[
                    ]
                },
                "CERTIFICATION_IN_PROGRESS":{
                    "ANY":[
                    ]
                },
                "CERTIFIED":{
                    "ANY":[
                    ]
                }
            },
            "folder":[
                {"text": "Active Projects",     "groupname": "IN_PROGRESS" },
                {"text": "Check Out",         "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKOUT"},
                {"text": "Check In",          "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKIN"},
                {"text": "Followed Projects",   "groupname": "FOLLOWING" },
                {"text": "Ready For Testing",   "group": "FOLLOWING", "state": "READY_FOR_CERTIFICATION"},
                {"text": "In Testing", "group": "FOLLOWING", "state": "CERTIFICATION_IN_PROGRESS"},
                {"text": "Certified",           "group": "FOLLOWING", "state": "CERTIFIED"}
            ]

        },
        "PRODUCT_STRATEGIST":{
            "title": "Product Strategist's Workspace",
            "pages":[],
            "dashboard": {
                "showCreateNew": false,
                "showCreateNewProduct": false
            },
            "changeLifecycleStateButtons":{},

            "states":{
                "NOT_CERTIFIED_CHECKOUT":{
                    "ANY":[
                    ]
                },
                "NOT_CERTIFIED_CHECKIN": {
                    "ANY": [
                    ]
                },
                "READY_FOR_CERTIFICATION": {
                    "ANY":[
                    ]
                },
                "CERTIFICATION_IN_PROGRESS":{
                    "ANY":[
                    ]
                },
                "CERTIFIED":{
                    "ANY":[
                    ]
                }
            },
            "folder":[
                {"text": "Active Projects",     "groupname": "IN_PROGRESS" },
                {"text": "Check Out",         "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKOUT"},
                {"text": "Check In",          "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKIN"},
                {"text": "Followed Projects",   "groupname": "FOLLOWING" },
                {"text": "Ready For Testing",   "group": "FOLLOWING", "state": "READY_FOR_CERTIFICATION"},
                {"text": "In Testing", "group": "FOLLOWING", "state": "CERTIFICATION_IN_PROGRESS"},
                {"text": "Certified",           "group": "FOLLOWING", "state": "CERTIFIED"}
            ]
        },
        "PRODUCT_MANAGER":{
            "title": "Product Manager Workspace",
            "pages":[],
            "dashboard": {
                "showCreateNew": false,
                "showCreateNewProduct": true
            },
            "changeLifecycleStateButtons":{
                "NOT_CERTIFIED_CHECKOUT":{  "checkIn":      {"text":"Check in",  "url":"lifecycleState/CHECKIN", "confirmationModal": "lifecycleState/CHECKIN"},
                                            "deleteVersion":{"text":"Delete Version", "url":"lifecycleState/UNDOCHECKOUT", "alertModal": "lifecycleState/UNDOCHECKOUT"}
                },
                "NOT_CERTIFIED_CHECKIN":{   "checkOut":     {"text":"Check Out",  "url":"lifecycleState/CHECKOUT"}
                }
            },
            "states":{
                "NOT_CERTIFIED_CHECKOUT":{
                    "ANY":[
                    ]
                },
                "NOT_CERTIFIED_CHECKIN": {
                    "ANY": [
                    ]
                },
                "READY_FOR_CERTIFICATION": {
                    "ANY":[
                    ]
                },
                "CERTIFICATION_IN_PROGRESS":{
                    "ANY":[
                    ]
                },
                "CERTIFIED":{
                    "ANY":[
                    ]
                }
            },
            "folder":[
                {"text": "Active Projects",     "groupname": "IN_PROGRESS" },
                {"text": "Check Out",         "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKOUT"},
                {"text": "Check In",          "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKIN"},
                {"text": "Followed Projects",   "groupname": "FOLLOWING" },
                {"text": "Ready For Testing",   "group": "FOLLOWING", "state": "READY_FOR_CERTIFICATION"},
                {"text": "In Testing", "group": "FOLLOWING", "state": "CERTIFICATION_IN_PROGRESS"},
                {"text": "Certified",           "group": "FOLLOWING", "state": "CERTIFIED"}
            ]
        },
        "TESTER":{
            "title": "Tester's Workspace",
            "pages":[],
            "dashboard": {
                "showCreateNew": false,
                "showCreateNewProduct": false
            },
            "changeLifecycleStateButtons":{
                "READY_FOR_CERTIFICATION":{     "startTesting": {"text":"Start Testing",  "url":"lifecycleState/startCertification"}

                },
                "CERTIFICATION_IN_PROGRESS":{   "accept":       {"text":"Accept", "url":"lifecycleState/certify", "confirmationModal": "lifecycleState/certify"},
                                                "reject":       {"text":"Reject", "url":"lifecycleState/failCertification", "confirmationModal": "lifecycleState/failCertification"},
                                                "cancel":       {"text":"Cancel","action":"changeLifecycleState", "url":"lifecycleState/cancelCertification", "confirmationModal": "lifecycleState/cancel"}
                }
            },
            "states": {
                "READY_FOR_CERTIFICATION":{
                    "ANY":[
                        {"text":"Start Testing","action":"changeLifecycleState", "url":"lifecycleState/startCertification"}
                    ]
                },
                "CERTIFICATION_IN_PROGRESS":{
                    "ANY":[
                        {"text":"Accept","action":"changeLifecycleState", "url":"lifecycleState/certify", "confirmationModal": "lifecycleState/certify"},
                        {"text":"Reject","action":"changeLifecycleState", "url":"lifecycleState/failCertification", "confirmationModal": "lifecycleState/failCertification"},
                        {"text":"Cancel","action":"changeLifecycleState", "url":"lifecycleState/cancelCertification", "confirmationModal": "lifecycleState/cancel" }
                    ]
                }
            },
            "folder":[
                {"text": "Active Projects",     "groupname": "FOLLOWING" },
                {"text": "Ready For Testing",   "group": "FOLLOWING", "state": "READY_FOR_CERTIFICATION"},
                {"text": "In Testing", "group": "FOLLOWING", "state": "CERTIFICATION_IN_PROGRESS"}
            ]
        },
        "OPS":{
            "title": "Operations Workspace",
            "pages":[],
            "dashboard": {
                "showCreateNew": false,
                "showCreateNewProduct": false
            },
            "changeLifecycleStateButtons":{
                "DISTRIBUTION_APPROVED":{   "distribute":   {"text":"Distribute",  "url":"distribution/PROD/activate", "conformanceLevelModal": { "url":"distribution-state/reject", "confirmationModal": "distribution-state/reject" }},
                                            "monitor":      {"text":"Monitor", "disabled":true}
                },
                "DISTRIBUTED":{             "redistribute": {"text":"Redistribute", "url":"distribution/PROD/activate", "conformanceLevelModal": { "url":"distribution-state/reject", "confirmationModal": "distribution-state/reject" }},
                                            "monitor":      {"text":"Monitor", "url":"distribution-state/monitor"}
                }
            },
            "states": {
                "CERTIFIED": {
                    "DISTRIBUTION_APPROVED": [
                        {"text": "Distribute","action":"changeLifecycleState", "url":"distribution/PROD/activate"}
                    ],
                    "DISTRIBUTED": [
                        {"text": "Monitor","action":"openDistributionModal", "url":"distribution-state/monitor"},
                        {"text": "Redistribute","action":"changeLifecycleState", "url":"distribution/PROD/activate"}
                    ]
                }
            },
            "folder":[
                {"text": "Active Projects",         "groupname": "FOLLOWING" },
                {"text": "Waiting For Distribution",  "group": "FOLLOWING", "state": "CERTIFIED", "dist": "DISTRIBUTION_APPROVED"},
                {"text": "Distributed",    "group": "FOLLOWING", "state": "CERTIFIED", "dist": "DISTRIBUTED"}
            ]
        },
        "GOVERNOR":{
            "title": "Governance Rep's Workspace",
            "pages":[],
            "dashboard": {
                "showCreateNew": false,
                "showCreateNewProduct": false
            },
            "changeLifecycleStateButtons":{
                "DISTRIBUTION_NOT_APPROVED":{   "approve":  {"text":"Approve", "url":"distribution-state/approve", "confirmationModal": "distribution-state/approve", "conformanceLevelModal": { "url":"distribution-state/reject", "confirmationModal": "distribution-state/reject" }},
                                                "reject":   {"text":"Reject", "url":"distribution-state/reject", "confirmationModal": "distribution-state/reject"}
                },
                "DISTRIBUTION_APPROVED":{       "reject":   {"text":"Reject", "url":"distribution-state/reject", "confirmationModal": "distribution-state/reject"}
                },
                "DISTRIBUTED": {                "reject":   {"text":"Reject", "url":"distribution-state/reject", "confirmationModal": "distribution-state/reject"}
                },
                "DISTRIBUTION_REJECTED": {      "approve":  {"text": "Approve", "url": "distribution-state/approve", "confirmationModal": "distribution-state/approve", "conformanceLevelModal": { "url":"distribution-state/reject", "confirmationModal": "distribution-state/reject" }}
                }
            },
            "states": {
                "CERTIFIED": {
                    "DISTRIBUTION_NOT_APPROVED": [
                        {"text":"Approve","action":"changeLifecycleState", "url":"distribution-state/approve", "confirmationModal": "distribution-state/approve"},
                        {"text":"Reject","action":"changeLifecycleState", "url":"distribution-state/reject", "confirmationModal": "distribution-state/reject"}
                    ],
                    "DISTRIBUTION_APPROVED": [
                        {"text":"Reject","action":"changeLifecycleState", "url":"distribution-state/reject", "confirmationModal": "distribution-state/reject"}
                    ],
                    "DISTRIBUTED": [
                        {"text":"Reject","action":"changeLifecycleState", "url":"distribution-state/reject", "confirmationModal": "distribution-state/reject"}
                    ],
                    "DISTRIBUTION_REJECTED": [
                        {"text":"Approve","action":"changeLifecycleState", "url":"distribution-state/approve", "confirmationModal": "distribution-state/approve"}
                    ]
                }
            },
            "folder":[
                {"text": "Active Projects",  "groupname": "FOLLOWING" },
                {"text": "Waiting For Approval",  "group": "FOLLOWING", "state": "CERTIFIED", "dist": "DISTRIBUTION_NOT_APPROVED"},
                {"text": "Distribution Rejected", "group": "FOLLOWING", "state": "CERTIFIED", "dist": "DISTRIBUTION_REJECTED"},
                {"text": "Distribution Approved", "group": "FOLLOWING", "state": "CERTIFIED", "dist": "DISTRIBUTION_APPROVED,DISTRIBUTED"}

            ]
        }
    },
    "confirmationMessages": {
        "lifecycleState/CHECKIN": {"showComment":true, "title": "Check in confirmation", "message": "Please add comment and confirm the check in."},
        "lifecycleState/CHECKOUT": {"showComment":true, "title": "Check out confirmation", "message": "Please add comment and confirm the check out."},
        "lifecycleState/certify": {"showComment":true, "title": "Certification confirmation", "message": "Please add comment and confirm test results."},
        "lifecycleState/cancel": {"showComment":true, "title": "Cancel test", "message": "Please add comment and cancel test."},
        "lifecycleState/failCertification": {"showComment":true, "title": "Rejection confirmation", "message": "Please add comment and confirm test results."},
        "lifecycleState/CERTIFICATIONREQUEST": {"showComment":true, "title": "Submit for testing", "message": "Please add comment and submit for testing."},
        "distribution-state/approve": {"showComment":true, "title": "Distribution confirmation", "message": "Please add comment and confirm %1 approval for distribution."},
        "distribution-state/reject": {"showComment":true, "title": "Rejection confirmation", "message": "Please add comment and confirm %1 rejection for distribution."},
        "updateTemplate": {"showComment":false, "title": "Update Template Confirmation", "message": "Modifying the Template might cause losing of previous information"}
    },
    "alertMessages": {
        "lifecycleState/UNDOCHECKOUT": {"title": "Delete Version Confirmation", "message": "Are you sure you want to delete this version?"},
        "exitWithoutSaving": {"title": "Exit Without Saving Confirmation", "message": "All unsaved changes will be lost. Are you sure you want to exit this page?"},
        "deleteInstance": {"title": "Delete Confirmation", "message": "Are you sure you would like to delete %1 ?"},
        "deleteInput": {"title": "Delete Confirmation", "message": "Are you sure you would like to delete %1 ?"}
    },
    "statuses": {
        "inDesign": {
            "name": "In Design",
            "values": [
                "NOT_CERTIFIED_CHECKOUT",
                "NOT_CERTIFIED_CHECKIN"
            ]
        },
        "readyForCertification": {
            "name": "Ready For Testing",
            "values": ["READY_FOR_CERTIFICATION"]
        },
        "inCertification": {
            "name": "In Testing",
            "values": ["CERTIFICATION_IN_PROGRESS"]
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
    "catalogMenuItem":{
        "DESIGNER":{
            "states":{
                "NOT_CERTIFIED_CHECKOUT":{
                    "ANY":[
                        {"text":"Submit for Testing","action":"changeLifecycleState", "url":"lifecycleState/certificationRequest", "emailModal": "lifecycleState/CERTIFICATIONREQUEST"}
                    ],
                    "NOT_OWNER":[
                    ]
                },
                "NOT_CERTIFIED_CHECKIN": {
                    "ANY": [
                        {"text": "Submit for Testing", "action": "changeLifecycleState", "url": "lifecycleState/certificationRequest", "emailModal": "lifecycleState/CERTIFICATIONREQUEST"}
                    ]
                },
                "READY_FOR_CERTIFICATION": {
                    "ANY":[
                    ]
                },
                "CERTIFICATION_IN_PROGRESS":{
                    "ANY":[
                    ]
                },
                "CERTIFIED":{
                    "ANY":[
                    ]
                }

            }
        },
        "PRODUCT_MANAGER":{
            "states":{
                "NOT_CERTIFIED_CHECKOUT":{
                    "ANY":[
                    ],
                    "NOT_OWNER":[
                    ]
                },
                "NOT_CERTIFIED_CHECKIN": {
                    "ANY": [
                    ]
                },
                "READY_FOR_CERTIFICATION": {
                    "ANY":[
                    ]
                },
                "CERTIFICATION_IN_PROGRESS":{
                    "ANY":[
                    ]
                },
                "CERTIFIED":{
                    "ANY":[
                    ]
                }
            },
            "folder":[
                {"text": "Active Projects",     "groupname": "IN_PROGRESS" },
                {"text": "Check Out",         "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKOUT"},
                {"text": "Check In",          "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKIN"},
                {"text": "Followed Projects",   "groupname": "FOLLOWING" },
                {"text": "Ready For Testing",   "group": "FOLLOWING", "state": "READY_FOR_CERTIFICATION"},
                {"text": "In Testing", "group": "FOLLOWING", "state": "CERTIFICATION_IN_PROGRESS"},
                {"text": "Certified",           "group": "FOLLOWING", "state": "CERTIFIED"}
            ]
        },
        "PRODUCT_STRATEGIST":{
            "title": "Product Strategist's Workspace",
            "pages":[],
            "dashboard": {
                "showCreateNew": false,
                "showCreateNewProduct": true
            },
            "states":{
                "NOT_CERTIFIED_CHECKOUT":{
                    "ANY":[
                    ],
                    "NOT_OWNER":[
                    ]
                },
                "NOT_CERTIFIED_CHECKIN": {
                    "ANY": [
                    ]
                },
                "READY_FOR_CERTIFICATION": {
                    "ANY":[
                    ]
                },
                "CERTIFICATION_IN_PROGRESS":{
                    "ANY":[
                    ]
                },
                "CERTIFIED":{
                    "ANY":[
                    ]
                }
            },
            "folder":[
                {"text": "Active Projects",     "groupname": "IN_PROGRESS" },
                {"text": "Check Out",         "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKOUT"},
                {"text": "Check In",          "group": "IN_PROGRESS", "state": "NOT_CERTIFIED_CHECKIN"},
                {"text": "Followed Projects",   "groupname": "FOLLOWING" },
                {"text": "Ready For Testing",   "group": "FOLLOWING", "state": "READY_FOR_CERTIFICATION"},
                {"text": "In Testing", "group": "FOLLOWING", "state": "CERTIFICATION_IN_PROGRESS"},
                {"text": "Certified",           "group": "FOLLOWING", "state": "CERTIFIED"}
            ]
        },
        "OTHER":{
            "states":{
                "ANY":{
                    "ANY":[
                    ]
                }
            }
        }
    },
    "LifeCycleStatuses":{
        "NOT_CERTIFIED_CHECKOUT":    { "text":"In Design Check Out", "icon": "checkout-editable-status-icon"},
        "NOT_CERTIFIED_CHECKIN" :    { "text":"In Design Check In", "icon": "checkin-status-icon "},
        "READY_FOR_CERTIFICATION":   { "text":"Ready for testing"},
        "CERTIFICATION_IN_PROGRESS": { "text":"In Testing"},
        "CERTIFIED":                 { "text":"Certified", "icon": "checkin-status-icon "}
    },
    "DistributionStatuses":{
        "DISTRIBUTION_NOT_APPROVED": { "text":"Waiting For Distribution"},
        "DISTRIBUTION_APPROVED" :    { "text":"Distribution Approved"},
        "DISTRIBUTION_REJECTED":     { "text":"Distribution Rejected"},
        "DISTRIBUTED":               { "text":"Distributed"}
    },
    "canvas_buttons":{
        "checkIn":          {"text":"Check in","action":"changeLifecycleState", "url":"lifecycleState/CHECKIN", "confirmationModal": "lifecycleState/CHECKIN"},
        "submitForTesting": {"text":"Submit for Testing","action":"changeLifecycleState", "url":"lifecycleState/certificationRequest", "emailModal": "lifecycleState/CERTIFICATIONREQUEST"},
        "deleteVersion":    {"text":"Delete Version", "action":"changeLifecycleState", "url":"lifecycleState/UNDOCHECKOUT", "alertModal": "lifecycleState/UNDOCHECKOUT"}
    },

    "component_workspace_menu_option": {
        "VFC":[
            {"text":"General", "action":"onMenuItemPressed", "state": "workspace.general"},
            {"text":"Icon",  "action":"onMenuItemPressed", "state": "workspace.icons"},
            {"text":"Deployment Artifact", "action":"onMenuItemPressed", "state": "workspace.deployment_artifacts"},
            {"text":"Information Artifact", "action":"onMenuItemPressed", "state": "workspace.information_artifacts"},
            {"text":"TOSCA Artifacts", "action":"onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text":"Properties", "action":"onMenuItemPressed", "state": "workspace.properties"},
            {"text":"Attributes", "action":"onMenuItemPressed", "state": "workspace.attributes"},
            {"text":"Req. & Capabilities", "action":"onMenuItemPressed", "state": "workspace.reqAndCap"},
            {"text":"Activity Log", "action":"onMenuItemPressed", "state": "workspace.activity_log"}
        ],
        "VL":[
            {"text":"General", "action":"onMenuItemPressed", "state": "workspace.general"},
            {"text":"Icon",  "action":"onMenuItemPressed", "state": "workspace.icons"},
            {"text":"Deployment Artifact", "action":"onMenuItemPressed", "state": "workspace.deployment_artifacts"},
            {"text":"Information Artifact", "action":"onMenuItemPressed", "state": "workspace.information_artifacts"},
            {"text":"TOSCA Artifacts", "action":"onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text":"Properties", "action":"onMenuItemPressed", "state": "workspace.properties"},
            {"text":"Attributes", "action":"onMenuItemPressed", "state": "workspace.attributes"},
            {"text":"Req. & Capabilities", "action":"onMenuItemPressed", "state": "workspace.reqAndCap"},
            {"text":"Activity Log", "action":"onMenuItemPressed", "state": "workspace.activity_log"}
        ],
        "CP":[
            {"text":"General", "action":"onMenuItemPressed", "state": "workspace.general"},
            {"text":"Icon",  "action":"onMenuItemPressed", "state": "workspace.icons"},
            {"text":"Deployment Artifact", "action":"onMenuItemPressed", "state": "workspace.deployment_artifacts"},
            {"text":"Information Artifact", "action":"onMenuItemPressed", "state": "workspace.information_artifacts"},
            {"text":"TOSCA Artifacts", "action":"onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text":"Properties", "action":"onMenuItemPressed", "state": "workspace.properties"},
            {"text":"Attributes", "action":"onMenuItemPressed", "state": "workspace.attributes"},
            {"text":"Req. & Capabilities", "action":"onMenuItemPressed", "state": "workspace.reqAndCap"},
            {"text":"Activity Log", "action":"onMenuItemPressed", "state": "workspace.activity_log"}
        ],
        "VF":[
            {"text":"General", "action":"onMenuItemPressed", "state": "workspace.general"},
            {"text":"Icon",  "action":"onMenuItemPressed", "state": "workspace.icons"},
            {"text":"Deployment Artifact", "action":"onMenuItemPressed", "state": "workspace.deployment_artifacts"},
            {"text":"Information Artifact", "action":"onMenuItemPressed", "state": "workspace.information_artifacts"},
            {"text":"TOSCA Artifacts", "action":"onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            // {"text":"Properties", "action":"onMenuItemPressed", "state": "workspace.properties"},
            {"text":"Composition", "action":"onMenuItemPressed", "state": "workspace.composition.details"},
            {"text":"Activity Log", "action":"onMenuItemPressed", "state": "workspace.activity_log"},
            {"text":"Deployment", "action":"onMenuItemPressed", "state": "workspace.deployment"},
            // {"text":"Inputs", "action":"onMenuItemPressed", "state": "workspace.resource_inputs"},
            {"text":"Properties Assignment", "action":"onMenuItemPressed", "state": "workspace.properties_assignment"}
        ],
        "SERVICE":[
            {"text":"General", "action":"onMenuItemPressed", "state": "workspace.general"},
            {"text":"Icon",  "action":"onMenuItemPressed", "state": "workspace.icons"},
            {"text":"TOSCA Artifacts", "action":"onMenuItemPressed", "state": "workspace.tosca_artifacts"},
            {"text":"Composition", "action":"onMenuItemPressed", "state": "workspace.composition.details"},
            {"text":"Activity Log", "action":"onMenuItemPressed", "state": "workspace.activity_log"},
            {"text":"Management Workflow", "action":"onMenuItemPressed", "state": "workspace.management_workflow"},
            {"text":"Network Call Flow ", "action":"onMenuItemPressed", "state": "workspace.network_call_flow"},
            {"text":"Monitor ", "action":"onMenuItemPressed", "state": "workspace.distribution", "disabledRoles": ["ADMIN", "TESTER", "GOVERNOR", "DESIGNER", "PRODUCT_MANAGER", "PRODUCT_STRATEGIST"]},
            {"text":"Deployment", "action":"onMenuItemPressed", "state": "workspace.deployment"},
            // {"text":"Inputs", "action":"onMenuItemPressed", "state": "workspace.service_inputs"},
            {"text":"Properties Assignment", "action":"onMenuItemPressed", "state": "workspace.properties_assignment"}
        ],
        "PRODUCT":[
            {"text":"General", "action":"onMenuItemPressed", "state": "workspace.general"},
            {"text":"Hierarchy", "action":"onMenuItemPressed", "state": "workspace.hierarchy"},
            {"text":"Icon",  "action":"onMenuItemPressed", "state": "workspace.icons"},
            {"text":"Composition", "action":"onMenuItemPressed", "state": "workspace.composition.details"}
        ]
    }

}

module.exports = SDC_MENU_CONFIG;
