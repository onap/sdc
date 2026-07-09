# AngularJS Removal — Phase 10–12 Test Safety Net (design)

**Date:** 2026-07-09 · **JIRA:** SDC-4829 · **Branch:** `angularjs-test-safety-net` · **Base:** `origin/master` = `25398ac74`

## Purpose

Phases 0–9 of the catalog-ui AngularJS removal are merged: the entire routing/component
shell and the recursive property editors are pure Angular. What remains AngularJS is the
**reusable-widget layer** — directives, services, filters — plus the bootstrap teardown
(Phases 10–14). This CR adds a **characterization test safety net** for that layer *before*
migrating it, mirroring the Phase 9 CR 0 pattern (test net merged just ahead of the code change).

**No production code changes** in this CR. Tests, test-harness config, and a triage doc only.

## The three layers and how each is protected

| Layer | Count | Harness | Gates in CI now? |
|---|---|---|---|
| **Filters** | 10 | Jest unit specs (pure transforms) | ✅ via coverage threshold |
| **Services** | 16 | Jest unit specs (logic-bearing) / `$httpBackend`-shape specs (HTTP) | ✅ via coverage threshold |
| **Directives** | ~29 dirs | Playwright behavior contracts (framework-agnostic) + Jest fallback | ❌ run-on-demand + per-migration checklist |

### Design decisions (locked in brainstorming, 2026-07-09)

1. **All three layers in one CR** (not one CR0 per phase).
2. **Directives → Playwright behavior contracts** — the same test passes before AND after the
   AngularJS→Angular migration (framework-agnostic observable behavior), so it is a true
   cross-boundary regression gate. Jest+angular-mocks would only protect the "before".
3. **Jest service/filter specs gate now; directive contracts are Playwright-local.** Playwright
   is not in CI today (only Selenium `onapUiSanity` gates); wiring it in is out of scope. The
   gating half is the Jest layer, made to count by widening `collectCoverageFrom` + raising the
   coverage threshold. Directive contracts are the documented per-directive pre-migration checklist.
4. **Unreachable/dead directives → triage doc + Jest fallback.** A `directive-migration-inventory.md`
   classifies every directive as (a) Playwright-contractable, (b) logic-bearing but unreachable →
   Jest+angular-mocks fallback, or (c) dead/no-consumer → flag for deletion in Phase 10 (test nothing,
   as with the `select-*` family in Phase 9 CR 1).

## Why the Jest half needs a config change (non-obvious)

`catalog-ui/package.json` `jest.collectCoverageFrom` currently lists only `ng2/**`,
`view-models/workspace/**`, `event-listener-service.ts`, and two `utils/` files. Filters and
most services are **outside coverage scope** — writing specs for them would not move the
coverage number and therefore would not gate. This CR adds `src/app/filters/**` and the
logic-bearing `src/app/services/**` files to `collectCoverageFrom`, then raises the global
threshold by the measured delta. That is the mechanism that makes a dropped/mis-ported service
turn CI red during Phases 11–12.

## Deliverables (as built)

1. `src/app/filters/*.spec.ts` — 10 filter characterization specs (42 assertions).
2. `src/app/services/*.spec.ts` — 9 service specs (28 assertions): pure-logic (`EventListenerService`,
   `ProgressService`, `AvailableIconsService`, `LoaderService`, `CookieService`, `AngularJSBridge`) +
   HTTP request-shape (`SdcVersionService`, `ConfigurationUiService`, `EcompHeaderService`).
3. `catalog-ui/docs/directive-migration-inventory.md` — triage of all 39 directives into buckets
   A/B/C, **with a verified spot-audit** of the 8 top candidates.
4. `package.json` — widened `collectCoverageFrom` (added `filters/**` + the 8 tested service files) +
   raised `coverageThreshold` to `38/35/26/37`.

### What changed vs. the original plan — directive Playwright contracts were DROPPED (with evidence)

The plan called for Playwright behavior contracts on the reachable (bucket-A) directives. Building
them surfaced a material finding (recorded in the inventory's "Verified spot-audit"): the pages that
rendered these directives were already migrated in Phases 2–9, so the pure-AngularJS directives are now
either superseded by Angular look-alikes (`ellipsis`→`<multiline-ellipsis>`, `download-artifact`→ng2
`<download-artifact>`), dead/self-referential, or live only on **deferred** surfaces (`loader` in the
`dcae` host shell and the `property-form-base` modal). A contract against, e.g., a catalog tile would
test the Angular component, not the ng1 directive — false confidence. So no directive contract ships in
this CR; the triage doc (directive layer is mostly a verify-then-delete exercise) is the directive
deliverable, and the gating safety-net weight is carried by the Jest filter/service specs. An initial
`ellipsis` contract was written, found to target the Angular component, and removed.

## Verification ladder

- Jest specs: `npm test` green + `--coverage` at/above the new floor (each characterization
  spec runs against the **current** unmodified code — it must pass on master; a failing
  expectation means either a wrong assumption or a discovered bug, both worth surfacing).
- Playwright contracts: `mvn verify -P run-integration-tests-playwright` (local, needs sim +
  backend) — documented, not a CI gate.
- Triage doc: every directive accounted for exactly once.
