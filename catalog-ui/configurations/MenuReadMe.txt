*******************************************************************
*******             Explanation about menu.json             *******
*******************************************************************

The menu.json defines the menu to show for each type of "roles":

Supported roles:
-----------------------------
ADMIN
DESIGNER
TESTER
OPS
GOVERNOR

The JSON is separated to roles, and for each role we define "states",
what menu to show the user for each state of the component:

Supported states:
-----------------------------
NOT_CERTIFIED_CHECKOUT
NOT_CERTIFIED_CHECKIN
READY_FOR_CERTIFICATION
CERTIFICATION_IN_PROGRESS
CERTIFIED

For each state we can define the user that will see this menu, the available parameters are:

Supported users:
-----------------------------
ANY
NOT_OWNER

Example:
For designer, if the component state is checkout and the component was created by other user, the NOT_OWNER will be used.

"DESIGNER":{
            "states":{
                "NOT_CERTIFIED_CHECKOUT":{
                    "ANY":[
                        {"text":"Edit"    ,"action":"goToEntity"},
                        {"text":"Check in","action":"changeLifecycleState", "url":"lifecycleState/CHECKIN", "confirmationModal": "lifecycleState/CHECKIN"},
                        {"text":"Submit for Testing","action":"changeLifecycleState", "url":"lifecycleState/certificationRequest", "emailModal": "lifecycleState/CERTIFICATIONREQUEST"},
                        {"text":"View"    ,"action":"openViewerModal"}
                    ],
                    "NOT_OWNER":[
                        {"text":"View"    ,"action":"openViewerModal"}
                    ]
                },


Definition of the menu item:
-----------------------------
text                - The text to show
action              - Function that will be called when pressing on the menu item
url                 - Data added to menu item, in case the function need to use it, example: for function "changeLifecycleState", I need to pass also the url "lifecycleState/CHECKOUT" that I want the state to change to.
confirmationModal   - Open confirmation modal (user should select "OK" or "Cancel"), and continue with the action.
emailModal          - Open email modal (user should fill email details), and continue with the action.
blockedForTypes     - This item will not be shown for specific components types.
