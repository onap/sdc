# Selenium Property Modal `data-tests-id` Contract

**Binding Rule**: CR 2 (Phase 9 property-form Angular rewrite) must preserve every `data-tests-id` and CSS class listed below verbatim, or provide an aliased `data-tests-id` on the new Angular component that maps to the same semantic element. The Selenium page objects (`PropertyPopup.java`, `DataTestIdEnum.PropertiesPopupEnum`) and Cypress specs (`edit-property-modal.spec.js`) depend on these selectors to navigate and validate the edit-property modal. Any mismatch breaks the integration test suite.

| data-tests-id / CSS class | Current Template : Line | Consumer | Notes |
|---|---|---|---|
| `sdc-edit-property-container` | `property-form-base-view.html:16` | Selenium `PropertyPopup.closePopup()` (DataTestIdEnum line 265) | Modal container; on `<ng1-modal>` element |
| `propertyName` | `property-form-base-view.html:39` | Selenium `PropertyPopup.getPropertyName()` (DataTestIdEnum line 257) | Text input field for property name |
| `propertyType` | `property-form-base-view.html:66` | Selenium `PropertyPopup.getPropertyType()` (DataTestIdEnum line 261); `PropertyPopup.selectPropertyType()` | Dropdown select for property type |
| `schema-type` | `property-form-base-view.html:90` | Selenium `PropertyPopup.selectEntrySchema()` (DataTestIdEnum line 262) | Dropdown select for entry schema type (for complex types like map/list) |
| `description` | `property-form-base-view.html:131` | Selenium `PropertyPopup.getPropertyDescription()` (DataTestIdEnum line 260) | Text area for property description |
| `defaultvalue` | `property-form-view.html:220` | Selenium `PropertyPopup.getPropertyValue()` (DataTestIdEnum line 258); Cypress `edit-property-modal.spec.js` | Input field for property value (simple types); also captures primitive map/list values |
| `delete_property` | `property-form-base-view.html:20` | Selenium `PropertyPopup.delete()` via `PropertyPopup.deleteProperty()` (not yet in enum but referenced via button click) | Delete button at top of modal |
| `get-prev` | `property-form-base-view.html:22` | Selenium `PropertyPopup.getPrev()` (not yet in enum but referenced via button click) | Previous arrow button; navigates to prior property in list |
| `get-next` | `property-form-base-view.html:23` | Selenium `PropertyPopup.getNext()` (not yet in enum but referenced via button click) | Next arrow button; navigates to next property in list |
| `propertyRadioButton_{name}` | Dynamic (not in template) | Selenium `PropertyPopup.selectRadioButtonByName(propertyName)` (DataTestIdEnum line 268) | Constructed in page object as `PROPERTY_RADIO_BUTTON_CONTAINER + propertyName`; for TOSCA-function selector in nested properties |
| `tlv-radio-label` (CSS class) | Dynamic (not in template) | Selenium `PropertyPopup.selectRadioButtonByName()` uses `.findElement(By.className(...))` (DataTestIdEnum line 269) | CSS class for radio button label; located via className lookup within `propertyRadioButton_*` container |
| `add-map-item` | `type-map-directive.html:126` | Cypress `edit-property-modal.spec.js` map round-trip test | Button to add a new key/value pair to map property |
| `mapKey{{fieldsPrefixName}}{{$index}}` | `type-map-directive.html:23` | Cypress `edit-property-modal.spec.js`; selector uses `[data-tests-id^=mapKey]` (starts-with) | Text input for map key; interpolated with fieldsPrefixName (current property index or -1 for new) and $index (item position) |
| `mapValue{{fieldsPrefixName}}{{$index}}` | `type-map-directive.html:56, 65` | Cypress `edit-property-modal.spec.js`; selector uses `[data-tests-id^=mapValue]` (starts-with) | Input/select/textarea for map value (varies by schema type); same prefix pattern as mapKey |
| `delete-map-item{{fieldsPrefixName}}{{$index}}` | `type-map-directive.html:122` | Selenium/Cypress via button click (not yet explicit in specs) | Delete button for individual map entry |
| `add-list-item` | `type-list-directive.html:112` | Cypress `edit-property-modal.spec.js` list round-trip test | Button to add a new item to list property |
| `listNewItem{{fieldsPrefixName}}` | `type-list-directive.html:42, 56` | Cypress `edit-property-modal.spec.js`; selector uses `[data-tests-id^=listNewItem]` (starts-with); note `.first()` needed | Input/select/textarea for new list item; **all list items share the same id** (no per-item $index); selector pattern is `listNewItem` + fieldsPrefixName only |
| `{{fieldsPrefixName+property.name}}` (nested leaf) | `data-type-fields-structure.html:116, 130` | Task 3 Cypress nested-datatype test; selector exact (not starts-with) | Input field for nested datatype property leaf; interpolated as fieldsPrefixName (e.g., `-1` for new property, `0` on reopen) + property name, e.g., `-1vlan_range_plan` during add, `0vlan_range_plan` on reopen |

## Prefix Scheme and Edge Cases

### `fieldsPrefixName` Behavior

- **New property (Add mode)**: `fieldsPrefixName = currentPropertyIndex = -1` (returned by `_.findIndex(filteredProperties, p => p.name === newName)` when property not yet created)
- **Existing property (Edit mode)**: `fieldsPrefixName = currentPropertyIndex = 0..N` (the property's position in the `filteredProperties` array after reload)

**Consequence for selectors**:
- During Add: `mapKey-10` (fieldsPrefixName=-1, $index=0), `mapValue-10`, `delete-map-item-10`, `listNewItem-1`, `{-1vlan_range_plan}` (nested leaf)
- On reopen after save: `mapKey01` (fieldsPrefixName=0, $index=1 for second entry), etc., `listNewItem0`, `0vlan_range_plan` (same nested leaf now has prefix=0)
- Selenium page object constructors must account for the property's position after reload, or query dynamically

### `listNewItem` Shared ID

All items in a list property share the same `data-tests-id=listNewItem{{fieldsPrefixName}}` (no per-item $index). When writing Cypress selectors to interact with multiple list items:
- Use `.first()` to pick the first (and only) input for the current "add new item" field
- Multiple items are visible after save/reload, but each still uses the same id during interaction (only one "new item" input is active at a time)

### `propertyRadioButton_{name}` Construction

The Selenium page object constructs this id at runtime as `DataTestIdEnum.PropertiesPopupEnum.PROPERTY_RADIO_BUTTON_CONTAINER.getValue() + propertyName`. Since `PROPERTY_RADIO_BUTTON_CONTAINER = "propertyRadioButton_"`, the final id is `propertyRadioButton_{propertyName}`. This does not appear verbatim in any template; it is generated by the page object for nested properties with TOSCA-function selectors.

### `tlv-radio-label` CSS Class Lookup

The Selenium page object finds radio buttons by CSS class, not `data-tests-id`. This class must be present on the radio label element within the `propertyRadioButton_*` container to enable the `selectRadioButtonByName()` action.

## Design References

- See **Phase 9 Design Spec** (§5, step 5) for the aliasing strategy if the Angular rewrite changes template structure: "aliased id on new component" means adding a secondary `data-tests-id` attribute that maps to the Selenium selector, even if the logical id differs.
- Task 3 Report (§3) documents the runtime prefix behavior and list-item id sharing finding.
