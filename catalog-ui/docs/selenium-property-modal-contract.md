# Selenium Property Modal `data-tests-id` Contract

> **Updated 2026-07-07 (Phase 9 CR 2):** the editable modal is now the Angular
> `PropertyFormModalComponent`, and the recursive value editor is the Angular
> `<dynamic-property>` component with a **path-based** `data-tests-id` scheme.
> No CI Selenium test opens this modal (verified) — the **scalar / container ids
> are preserved** for the ungated Vf / PNF / ImportVFCAsset popup tests and any
> future gate additions, but the **recursive value-editor ids changed** (the old
> `type-map` / `type-list` / `data-type-fields-structure` directives were deleted).
> The old numeric `{fieldsPrefixName}{index}` prefix scheme NO LONGER APPLIES.

**Binding Rule**: CR 2 (Phase 9 property-form Angular rewrite) preserves every
scalar / container `data-tests-id` and CSS class listed below verbatim. The
recursive value-editor ids are re-mapped to the `<dynamic-property>` scheme (see
the recursive rows). The Selenium page objects (`PropertyPopup.java`,
`DataTestIdEnum.PropertiesPopupEnum`) and the Cypress oracle
(`edit-property-modal.spec.js`) depend on these selectors to navigate and validate
the edit-property modal. Any mismatch breaks the integration test suite.

All scalar / container ids live in the new Angular modal template
`catalog-ui/src/app/ng2/pages/property-form-modal/property-form-modal.component.html`
(abbreviated `pfm.component.html` below). All recursive value-editor ids live in
`catalog-ui/src/app/ng2/components/logic/properties-table/dynamic-property/dynamic-property.component.html`
(abbreviated `dynamic-property.component.html`).

### Scalar / container ids (PRESERVED)

| data-tests-id / CSS class | Current Template : Line | Consumer | Notes |
|---|---|---|---|
| `sdc-edit-property-container` | `pfm.component.html:21` | Selenium `PropertyPopup.closePopup()` (DataTestIdEnum line 265) | Modal container `<div>` |
| `propertyName` | `pfm.component.html:54` | Selenium `PropertyPopup.getPropertyName()` (DataTestIdEnum line 257) | Text input field for property name |
| `propertyType` | `pfm.component.html:70` | Selenium `PropertyPopup.getPropertyType()` (DataTestIdEnum line 261); `PropertyPopup.selectPropertyType()` | Dropdown select for property type |
| `schema-type` | `pfm.component.html:87` | Selenium `PropertyPopup.selectEntrySchema()` (DataTestIdEnum line 262) | Dropdown select for entry schema type (for complex types like map/list) |
| `description` | `pfm.component.html:111` | Selenium `PropertyPopup.getPropertyDescription()` (DataTestIdEnum line 260) | Text area for property description |
| `defaultvalue` | `pfm.component.html:160` | Selenium `PropertyPopup.getPropertyValue()` (DataTestIdEnum line 258); Cypress `edit-property-modal.spec.js` | Input field for property value (simple/scalar types only — complex map/list/datatype values are now edited via `<dynamic-property>`, see recursive rows) |
| `booleantype` | `pfm.component.html:168` | (boolean scalar) | `<select>` shown instead of `defaultvalue` when the scalar type is boolean |
| `delete_property` | `pfm.component.html:30` | Selenium `PropertyPopup.delete()` via `PropertyPopup.deleteProperty()` (not yet in enum but referenced via button click) | Delete button in the modal top bar |
| `get-prev` | `pfm.component.html:35` | Selenium `PropertyPopup.getPrev()` (not yet in enum but referenced via button click) | Previous arrow button; navigates to prior property in list |
| `get-next` | `pfm.component.html:39` | Selenium `PropertyPopup.getNext()` (not yet in enum but referenced via button click) | Next arrow button; navigates to next property in list |
| `propertyRadioButton_{name}` | `pfm.component.html:121, 130` (`propertyRadioButton_value` / `propertyRadioButton_toscaFunction`) | Selenium `PropertyPopup.selectRadioButtonByName(propertyName)` (DataTestIdEnum line 268) | Value-type radio (Value/Entries vs TOSCA Function); page object builds `PROPERTY_RADIO_BUTTON_CONTAINER + propertyName` |
| `tlv-radio-label` (CSS class) | `pfm.component.html:128, 137` | Selenium `PropertyPopup.selectRadioButtonByName()` uses `.findElement(By.className(...))` (DataTestIdEnum line 269) | CSS class on the radio button label `<span>`; located via className lookup within the `propertyRadioButton_*` label |

### Recursive value-editor ids (CHANGED — `<dynamic-property>`, path-based)

The recursive value editor is now `<dynamic-property>` (see
`pfm.component.html:186` — the `isComplexType` branch). Its ids are built from
`propertyTestsId`, a **dotted path** computed by
`getPropertyTestsId() = [rootProperty.name].concat(rootProperty.getParentNamesArray(...)).join('.')`
(e.g. `map_prop`, `map_prop.0`, `nested_prop.vlan_range_plan`). The container-icon
ids (`add-to-list-`, `delete-from-list-`, `expand-`) render **directly** on their
elements. The key/value `<input>`s are produced by the `ui-element-*` form
components, which **prepend `value-`** to the `<dynamic-element>` `testId`, so the
actual DOM id has a `value-` prefix.

| data-tests-id (DOM) | Current Template : Line | testId expression | Consumer | Notes |
|---|---|---|---|---|
| `add-to-list-{path}` | `dynamic-property.component.html:72` (list), `:73` (map) | `'add-to-list-' + propertyTestsId` | Cypress `edit-property-modal.spec.js` map + list round-trip | **One id for BOTH list and map** add buttons. Cypress: `[data-tests-id^="add-to-list-"]` |
| `delete-from-list-{path}` | `dynamic-property.component.html:74` | `'delete-from-list-' + propertyTestsId` | Selenium/Cypress via click (delete a list/map item) | One id for both list and map delete |
| `expand-{path}` | `dynamic-property.component.html:75` | `'expand-' + propertyTestsId` | (expand/collapse complex child) | Round expand toggle on complex/list/map rows with children |
| `value-prop-key-{path}` | `dynamic-property.component.html:40` (`prop-key-` testId) → rendered by `input/ui-element-input.component.html:28` as `value-` + testId | `'prop-key-' + propertyTestsId` | Cypress map round-trip | Map **key** input. Cypress: `[data-tests-id^="value-prop-key-"]` |
| `value-prop-{path}` | `dynamic-property.component.html:56` (`prop-` testId) → rendered by `ui-element-*` as `value-` + testId | `'prop-' + propertyTestsId` | Cypress map value, list item, nested-datatype leaf | Leaf **value** input (string/dropdown/integer/etc.). Cypress: `[data-tests-id^="value-prop-"]`. **Collision** with the key (below) |

## Path Scheme and Edge Cases

### Path-based `data-tests-id` (`propertyTestsId`)

The old numeric `{{fieldsPrefixName}}{{$index}}` prefix scheme is **gone**. In
`<dynamic-property>` every recursive id ends with `propertyTestsId`, a dotted path
built by `getPropertyTestsId()`:

```
propertyTestsId = [rootProperty.name]
    .concat(rootProperty.getParentNamesArray(property.propertiesName, [], true))
    .join('.')
```

- Root property: just its name, e.g. `map_prop`, `list_prop`, `nested_prop`.
- Map entry: `mapKey` inputs use the parent path (`map_prop`); list entries use the
  positional index in the path (`list_prop.0`, `list_prop.1`, …); datatype leaves
  use the sub-field name (`nested_prop.vlan_range_plan`).

**Consequence for selectors** — the id is **stable across add and reopen** (no more
`-1` → `0` prefix flip). Use path-suffix matching, not numeric prefixes:

- Add list/map item: `[data-tests-id^="add-to-list-"]` (one id for both list and map)
- Map key: `[data-tests-id^="value-prop-key-"]`
- Nested-datatype leaf: `[data-tests-id^="value-prop-"][data-tests-id$="vlan_range_plan"]`
- Selenium page objects that need a specific entry should match on the property name
  suffix, or query the input within the row container, rather than reconstructing a
  numeric index.

### `value-prop-key-` vs `value-prop-` collision (map key vs value)

The map **key** input (`value-prop-key-{path}`) and the leaf **value** input
(`value-prop-{path}`) both start with `value-prop-`. To target the VALUE input
specifically, exclude the key:

- Cypress: `cy.get('[data-tests-id^="value-prop-"]').not('[data-tests-id^="value-prop-key-"]')`
- CSS: `[data-tests-id^="value-prop-"]:not([data-tests-id^="value-prop-key-"])`

For a **list** item there is no key input, so `[data-tests-id^="value-prop-"]` alone
is unambiguous (with `.first()` for the active new-item input).

### `propertyRadioButton_{name}` Construction

The Selenium page object constructs this id at runtime as `DataTestIdEnum.PropertiesPopupEnum.PROPERTY_RADIO_BUTTON_CONTAINER.getValue() + propertyName`. Since `PROPERTY_RADIO_BUTTON_CONTAINER = "propertyRadioButton_"`, the final id is `propertyRadioButton_{propertyName}`. The new Angular modal renders two such labels verbatim — `propertyRadioButton_value` and `propertyRadioButton_toscaFunction` (`pfm.component.html:121, 130`) — plus any page-object-constructed variant for nested properties with TOSCA-function selectors.

### `tlv-radio-label` CSS Class Lookup

The Selenium page object finds radio buttons by CSS class, not `data-tests-id`. This class must be present on the radio label element within the `propertyRadioButton_*` container to enable the `selectRadioButtonByName()` action.

## Design References

- **Phase 9 CR 2** replaced the AngularJS `property-form-base-view.html` /
  `property-form-view.html` / `type-map-directive.html` / `type-list-directive.html`
  / `data-type-fields-structure.html` templates (all DELETED) with the Angular
  `PropertyFormModalComponent` (`pfm.component.html`) and the reused
  `<dynamic-property>` recursive value editor.
- The old `{fieldsPrefixName}{index}` prefix scheme (`-1` on add, `0` on reopen,
  `listNewItem` shared id) is obsolete. It has been replaced by the path-based
  `getPropertyTestsId()` scheme documented above.
- No CI Selenium test opens the editable property modal (verified). The scalar /
  container ids are preserved for the ungated Vf / PNF / ImportVFCAsset popup tests
  and future gate additions; only the recursive value-editor ids changed.
