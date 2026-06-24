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

## Fast iteration with webpack-dev-server (recommended for UI work)

Instead of rebuilding Docker images on every code change (~3 min per iteration),
you can run the catalog-ui webpack-dev-server with HMR and point Playwright at it
directly (~2-5 seconds per iteration).

### Prerequisites

- The Docker backend must be running (Cassandra + sdc-backend exposing port 8080)
- Node.js installed in `catalog-ui/`

#### Getting the Docker backend up

The dev server only needs the **backend** containers (Cassandra + sdc-backend),
not the frontend or simulator. Start the full integration stack once (it brings
up everything; the dev server then bypasses the Dockerised frontend):

```bash
# from the repo root — build images once (see step 1 of "Running locally")
mvn clean install -P all,docker -DskipTests

# start the stack (backend, Cassandra, onboarding, FE, simulator)
mvn pre-integration-test -P run-integration-tests-playwright -f integration-tests/pom.xml
```

Verify the backend is reachable on **8080** (the dev server proxies
`/sdc1/feProxy/rest` → `localhost:8080/sdc2/rest`):

```bash
curl -s http://localhost:8080/sdc2/rest/v1/screen?excludeTypes=VFCMT -H "USER_ID: cs0008" | head -c 80
# expect: {"resources":[...
```

The containers are named `sdc-backend-all-plugins-1`, `sdc-cassandra-1`,
`sdc-onboard-backend-1`, `sdc-simulator-1`, `sdc-frontend-1` and join the
`sdc-network` Docker bridge network. List them with `docker ps`.

> **Backend on a non-default port?** Point the dev server at it with
> `SDC_BACKEND_HOST` / `SDC_BACKEND_PORT` (see "Pointing at a remote backend").

#### (Optional) Rebuilding only the Dockerised frontend

If you instead want to test a **production** frontend build inside Docker (what
CI runs) rather than the dev server, rebuild just `sdc-frontend` and restart its
container. The container **must** join `sdc-network` with the network alias
`sdc-FE` — the webseal-simulator proxies to the host `sdc-FE:8181`, so without
the alias every `/sdc1/feProxy/...` call through the simulator (port 8285)
returns `500 UnknownHostException: sdc-FE`:

```bash
# from repo root, after editing catalog-ui/
cd catalog-ui && npx webpack --config webpack.production.js && cd ..
rm -rf catalog-fe/src/main/webapp/scripts && cp -r catalog-ui/dist/* catalog-fe/src/main/webapp/
mvn package -pl catalog-fe -DskipTests -Dcheckstyle.skip -Djacoco.skip=true -DskipPMD -q
cp catalog-fe/target/catalog-fe-*-SNAPSHOT.war catalog-fe/sdc-frontend/
mvn process-resources docker:build -pl catalog-fe -P docker -DskipTests -Ddocker.noCache=true -q
docker rm -f sdc-frontend-1
docker run -d --name sdc-frontend-1 --network sdc-network --network-alias sdc-FE \
  -e ENVNAME=AUTO -e FE_HOSTNAME=sdc-frontend-1 \
  -e BE_HOSTNAME=sdc-backend-all-plugins-1 -e BE_PORT=8443 \
  -e ONBOARDING_BE_HOSTNAME=sdc-onboard-backend-1 -e ONBOARDING_BE_PORT=8445 \
  onap/sdc-frontend:latest
# then run Playwright against the simulator: SDC_BASE_URL=http://localhost:8285
```

This production-via-simulator path is the closest local reproduction of the CI
Selenium environment; the dev server (below) is faster but not byte-identical.

### Start the dev server

```bash
cd catalog-ui
npm install   # first time only
npm run start:local
```

This starts webpack-dev-server on **port 9000** with:
- Hot Module Replacement (code changes reload in ~2s)
- A built-in `/login` page (no webseal-simulator needed)
- API proxy to the local backend (`localhost:8080/sdc2/rest/...`)

### Run tests against the dev server

```bash
cd integration-tests/playwright-tests
SDC_BASE_URL=http://localhost:9000 npx playwright test
```

### Workflow

1. Edit code in `catalog-ui/src/`
2. Webpack recompiles automatically (~2s)
3. Re-run the Playwright test
4. Repeat

### Pointing at a remote backend

To proxy API calls to a remote SDC instance (e.g. tnaplab) instead of the local
Docker backend:

```bash
cd catalog-ui
SDC_BACKEND_HOST=sdc-fe-ui-oom-sm-master.tnaplab.telekom.de \
SDC_BACKEND_PORT=443 \
SDC_BACKEND_PROTOCOL=https \
SDC_DIRECT_FE=true \
npm start
```

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
