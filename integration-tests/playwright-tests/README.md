# SDC Playwright E2E Tests

Playwright-based end-to-end tests for the SDC frontend. These tests run against the same
Docker stack used by the existing Selenium/TestNG integration tests (Cassandra, backend,
frontend, webseal-simulator, etc.) but use [Playwright](https://playwright.dev/) instead
of Selenium for browser automation.

## Prerequisites

- Node.js 18+ (Playwright ≥ 1.42 requires it)
- Docker
- Ports 8080, 8285, 8443, 9042, 9443 free on the host

## Running locally

The tests need the integration-test Docker stack (Cassandra, backend, frontend,
simulator). If the stack is already running, skip straight to "Run the tests".

### 1. Build Docker images (one-time)

The Docker images (`onap/sdc-backend-all-plugins`, `onap/sdc-frontend`, etc.)
must exist locally. Build them from the repository root with **both** the
`all` and `docker` profiles (`-P all` keeps the default module list active
when other profiles are specified):

```bash
mvn clean install -P all,docker -DskipTests
```

> **Note:** `clean` is required so that the `build-helper-maven-plugin`
> `parse-version` goal (bound to `pre-clean`) runs and resolves
> `${parsedVersion.*}` variables used in Docker image tags.

### 2. Start the Docker stack

```bash
mvn pre-integration-test -P run-integration-tests-playwright \
    -f integration-tests/pom.xml
```

`pre-integration-test` starts the containers (Cassandra, backend, frontend,
simulator, etc.) but does **not** run the tests or tear them down, so the
stack stays up for iterating locally.

Once healthy, the webseal-simulator is reachable at `http://localhost:8285`.

Stop the containers later with:

```bash
mvn docker:stop -f integration-tests/pom.xml
```

### 3. Run the tests

```bash
cd integration-tests/playwright-tests
npm install
npx playwright install chromium
SDC_BASE_URL=http://localhost:8285 npx playwright test
```

### Headed mode (see the browser)

```bash
SDC_BASE_URL=http://localhost:8285 npm run test:headed
```

### View the HTML report

```bash
npm run test:report
```

## Running via Maven (full lifecycle)

A single Maven command handles the entire lifecycle — spin up Docker, install
Node/npm, install Playwright browsers, run the tests, tear down Docker:

```bash
mvn verify -P run-integration-tests-playwright \
    -f integration-tests/pom.xml
```

> This assumes the Docker images already exist locally (see step 1 above).

Reports are written to:

| Artifact             | Path                                                      |
| -------------------- | --------------------------------------------------------- |
| HTML report          | `integration-tests/target/playwright-report/index.html`   |
| JUnit XML            | `integration-tests/target/playwright-reports/results.xml` |
| Screenshots & traces | `integration-tests/target/playwright-results/`            |

## CI (Jenkins)

The JJB definition in `ci-management` registers the job
`sdc-integration-tests-{stream}-playwright-verify-java`, which triggers on every
Gerrit patch set and archives the report artifacts listed above.

## Writing new tests

Add `.spec.ts` files under `tests/`. The Playwright config (`playwright.config.ts`)
sets `baseURL` from the `SDC_BASE_URL` environment variable (default
`http://localhost:8285`), so you can use relative URLs in `page.goto()`.

The webseal-simulator login flow is straightforward:

```ts
await page.goto("/login");
await page.locator('input[name="userId"]').fill("<userId>");
await page.locator('input[name="password"]').fill("123123a");
await page.locator('input[value="Login"]').click();
await page.waitForURL("**/sdc1**");
```

See `tests/sdc-sanity.spec.ts` for a working example.
