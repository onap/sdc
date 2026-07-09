# AngularJS Directive Migration Inventory (SDC-4829, Phase 10)

**Created:** 2026-07-09 · **Branch:** `angularjs-test-safety-net` · **Base:** `origin/master` = `25398ac74`

This is the **pre-migration triage** for the 39 pure-AngularJS directives registered in
`catalog-ui/src/app/modules/directive-module.ts` (the `directiveModule.directive('name', X.factory)`
entries — NOT the `downgradeComponent` bridge entries, which are already-migrated Angular components).

It classifies every directive into one of three buckets so Phase 10 knows, per directive, whether to
write a behavior contract, migrate-then-verify in isolation, or delete outright.

> ⚠️ **The usage counts below are PROVISIONAL.** They come from a grep sweep of `.html` templates for
> the camelCase and kebab-case forms. AngularJS also lets a directive be applied from JS (`$compile`),
> from a `template`/`templateUrl` string, or transitively from another directive's template — none of
> which a plain HTML grep catches. **Before deleting anything in bucket C, re-verify unreachability the
> way Phase 9 CR 1 did**: confirm zero live consumers AND that a production AOT build still succeeds
> with the directive removed (that build is the real unreachability proof — see failure-catalog §GG on
> the `.less` `@import` trap that a naive grep missed). Treat bucket C as "candidates for deletion",
> not "delete list".

## Buckets

- **(A) Playwright-contractable** — reachable on a normal SDC page (home / catalog / a workspace tab)
  and has observable behaviour worth a framework-agnostic behavior contract. The contract asserts the
  same DOM/behaviour before and after the AngularJS→Angular migration, so it is a true cross-boundary
  regression gate. Contracts live in `integration-tests/playwright-tests/tests/directive-contracts/`.
- **(B) Migrate-then-verify (logic-bearing, not cheaply reachable)** — has real logic but is either a
  validation-only directive with no distinct visual output, deep in a hard-to-reach flow, or otherwise
  not observable on the sim without heavy fixture setup. `angular-mocks` is NOT a dependency of this
  project, so a `$compile` unit test would require adding it; the recommended tactic at migration time
  is to extract the pure logic into a testable function/pipe and Jest-test that, then verify the
  rendered result in the browser.
- **(C) Dead / flag-for-deletion candidate** — no HTML consumer found; likely superseded by an Angular
  equivalent or simply abandoned. Verify (see the warning above), then delete instead of migrating.

## Triage table

| Directive (kebab) | HTML refs (provisional) | Logic | Bucket | Notes |
|---|---|---|---|---|
| `loader` | many (ng2 app shell + pages) | heavy | **A** | Global/section loading indicator; asserted indirectly by the existing `workspace-shell.spec.ts` "waitForLoader parity" test. Migrate carefully — the `[display]` binding already bit Phase 5 (failure-catalog §Y). |
| `ellipsis` | several (ng2) | thin | **A** | Text-overflow tooltip; rendered on catalog tiles + properties/attributes tables. Contract via a catalog tile. |
| `sdc-tags` / `sdc-tag` | tag list only (NOT general tab) | heavy | **B** | ⚠️ Reclassified A→B on 2026-07-09: the General tab does NOT use these directives — Phase 3 replaced them with plain Angular markup that reproduces the `i-sdc-tag-text` chip (see `general-tab.component.html` and failure-catalog §U). A contract on the General tab would test the already-Angular impl, not this ng1 directive. Its remaining consumers must be located and confirmed still-AngularJS before a contract is worth writing; treat as migrate-then-verify. |
| `expand-collapse` | many (ng2) | heavy | **A** | Row/section expand toggle in properties/inputs; observable on the properties tab. |
| `download-artifact` | artifact pages (ng2) | heavy | **A** | Reachable on the tosca/deployment/information artifact tabs. |
| `file-upload` | general tab, artifact forms | heavy | **A** | Reachable on General tab / artifact upload; has base64 + validation logic. |
| `sdc-smart-tooltip` | tag/checkbox/radio/property | thin | **A** | Dynamic tooltip; broadly reachable. |
| `prevent-double-click` | workspace view | thin | **A** | Button double-submit guard; observable on any workspace action button. |
| `structure-tree` | own template only | heavy | **B** | Recursive tree build; deep composition flow, not cheaply reachable. |
| `ecomp-header` | ecomp-header template | heavy | **B** | Menu flatten/nest logic; only rendered inside the deferred `dcae` host shell. Pairs with the `dcae` decision CR. |
| `file-type` | (none found) | thin | **B** | `ngModel` file-extension validator; validation-only, no distinct visual output. |
| `perfect-scrollbar` | 1 ng2 | thin | **B** | Scrollbar wrapper; largely superseded by CSS. |
| `ng1-modal` | icon modal, property form | thin | **B** | ngui.bootstrap.modal wrapper; presentation-only. |
| `validation-on-load` | modal-import-type | thin | **B** | Form-validity-on-load utility; test in isolation. |
| `expand-collapse-menu-box` | menu boxes | thin | **B** | Simple expand/collapse presentation. |
| `input-row` | input rows | thin | **B** | Composition-specific; minimal link logic. |
| `property-row` | property display | thin | **B** | Delete-icon toggle; properties page reachable but low-value contract. |
| `punch-out` | plugin integration | heavy | **B** | Watches data + calls `PunchOutRegistry`; the flow-editor tabs already migrated off it (Phase 5), remaining consumers are plugin pages. |
| `top-progress` | progress bar | thin | **B** | Import progress display; low-value contract. |
| `invalid-characters` | none | thin | **C** | No HTML consumer found. |
| `custom-validation` | none | thin | **C** | No HTML consumer found. |
| `edit-name-popover` | 1 (own?) | thin | **C** | Verify the single ref is a live consumer, not a self-reference. |
| `info-tooltip` | 1 (own template) | thin | **C** | The only ref is the directive's own template file — self-reference, likely dead. |
| `ng1-tabs` / `sdc-single-tab` / `inner-sdc-single-tab` | 0–1 | thin | **C** | The custom tab family; the sole ref is a self-reference. Superseded by `<sdc-tabs>` downgrade. |
| `json-export-excel` | none | heavy | **C** | CSV/Excel export; no consumer found — dead feature. |
| `ng1-checkbox` | input-row/property-row views | thin | **C** | Only referenced by ng1 property/input row templates that are themselves migration targets — verify jointly. |
| `sdc-radio-button` | none | thin | **C** | No HTML consumer found. |
| `on-last-repeat` | none | thin | **C** | `ng-repeat` completion utility; no consumer found. |
| `capabilities-list` / `requirements-list` | none | thin | **C** | Empty link fns, only own templates — superseded by the downgraded `<req-and-capabilities>` Angular component. |
| `clicked-outside` | none | heavy | **C** | Outside-click detection; no consumer found. |
| `user-header-details` | none | heavy | **C** | Header details; not rendered in current UI. |
| `sdc-keyboard-events` | 1 (own?) | heavy | **C** | Keyboard handler; verify the single ref. |

## Verified spot-audit (2026-07-09) — the directive layer is more migrated than the grep suggests

Eight bucket-A/B candidates were audited by opening every template that references them (not just
counting greps). The result reshaped the whole directive picture and is the most important finding
of this CR:

| Directive | Verdict | Evidence |
|---|---|---|
| `loader` | **Live only in deferred surfaces** | The workspace `<loader data-display=...>` usages are in `workspace-view.html`, which is **dead template** — no `templateUrl` points at it (the workspace state renders `<workspace-container>` since Phase 2). Genuinely-live ng1 `<loader>` remains only in `dcae-app-view.html` (deferred host shell, only mounts if a runtime `dcaeApp` module exists) and `property-form-base-view.html` (the base/module-property modal, a deferred later phase). The catalog/workspace ng2 pages use the **Angular** `<loader [display]>` — same selector, different framework (the §Y trap). |
| `sdc-smart-tooltip`, `prevent-double-click` | **Not verified-live** | Their only found usages are in the dead `workspace-view.html`. Real remaining consumers, if any, must be located before a contract is worth writing. |
| `ellipsis` | **Superseded (bucket C)** | The catalog tile uses the Angular `<multiline-ellipsis>` component; the info-tab uses `<chars-ellipsis>`; the only reference to the pure-ng1 `ellipsis` attribute directive is its own template. |
| `download-artifact` | **Superseded (bucket C)** | All usages are the ng2 `<download-artifact>` component with `[artifact]` property bindings. |
| `expand-collapse`, `file-upload`, `file-type`, `structure-tree` | **Self-ref/dead (bucket C)** | Referenced only inside their own templates (or an unused wrapper). No reachable consumer. |

**Consequence for this CR:** the Playwright directive-contract deliverable has very few valid targets —
the pages that used these directives were already migrated in Phases 2–9, leaving the directives as
dead/superseded code or usable only in deferred surfaces (`dcae`, `property-form-base`). Writing a
Playwright contract against, say, a catalog tile would test the **Angular** look-alike, not the
AngularJS directive, giving false confidence. So **no directive Playwright contracts are added in this
CR** (the initial `ellipsis` contract was written, found to target the Angular component, and removed).
The valuable directive deliverable is THIS triage doc: it tells Phase 10 that the directive layer is
mostly a **deletion** exercise (verify-then-delete per §GG), not a migration one — a materially cheaper
Phase 10 than the roadmap assumed. The Jest filter/service specs (which DO gate) carry the automated
safety-net weight of this CR.

## Coverage of this bucketing

- **Bucket A** directives would each get a Playwright behavior contract — BUT the verified spot-audit
  above shows the reachable pages that used them are already Angular, so a contract would test the
  Angular look-alike, not the ng1 directive. Net: **no directive Playwright contract is added in this
  CR.** The directives that survive on deferred surfaces (`loader` in `dcae`/`property-form-base`) get
  their contract when those surfaces are migrated, alongside the existing workspace/general-tab
  Playwright specs and the CI Selenium `onapUiSanity` suite (which already walks create/edit/onboard
  flows through the live widgets).
- **Bucket B** directives are the Phase-10 "migrate-then-verify in the browser" worklist; no automated
  test is added now (a `$compile` test would need `angular-mocks`, which is not a project dependency —
  extract-and-Jest the logic at migration time instead).
- **Bucket C** directives are the Phase-10 deletion-candidate worklist — verify unreachability with a
  production AOT build (per §GG), then delete rather than migrate.
