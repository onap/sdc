# Catalog-ui

## Development server

Use the Node 16.20.0 / npm 6.14.13 runtime installed by Maven. The application
uses Angular 8.2.14, Angular CLI 8.3.29 and the `angular.json` workspace.
Production AOT and development JIT use View Engine; Ivy is disabled.
Angular 8 is an end-of-life migration checkpoint, not a supported final platform.

The `onap-ui-angular` 6.0.0 dependency must be published by the sibling library
release before merging the consumer pin. During coordinated local development,
install the library's `npm pack` tarball with `npm install --no-save
--no-package-lock /path/to/onap-ui-angular-6.0.0.tgz`; do not commit a local path.

1. Run `npm install`, then `npm start` for a dev server.
2. Navigate to <http://localhost:9000/>.
   * The app will automatically reload if you change any of the source files.

To Specify the user role:

`npm start -- --env.role designer`

`npm run designer` or `npm run admin`

The loopback-only gateway exposes `/login`, simulates WebSeal identities and
forwards to `ng serve` on an automatically selected internal port. Live reload
passes through the gateway. Stop the command to close both servers.


## Build

### dev

Run `npm run build` to build the project.
The build artifacts will be stored in the `dist/` directory.

### prod

Run `npm run build:prod` to build the project.
The build artifacts will be stored in the `dist/` directory.

Production uses AOT, CLI-generated content hashes and `/sdc1/` asset URLs.
`tools/package-production.js` emits `.jsgz` files and updates the generated
index for catalog-fe's gzip filter, retaining `.js` files for lazy chunks.
Maven copies `dist/` to catalog-fe as before. Do not copy only an index or
individual chunks from a different build.

## Configuration

### Development Proxies

`proxy.conf.js` and `tools/dev-proxy.js` configure the API proxies. Configure
the target with environment variables, not webpack internals:

```bash
# Direct backend, the default
SDC_BACKEND_HOST=localhost SDC_BACKEND_PORT=8080 npm start

# catalog-fe, which performs the REST/uicache rewrites itself
SDC_BACKEND_HOST=localhost SDC_BACKEND_PORT=8181 SDC_DIRECT_FE=true npm start

# Remote frontend
SDC_BACKEND_HOST=sdc-fe-ui.example.com SDC_BACKEND_PORT=443 \
   SDC_BACKEND_PROTOCOL=https SDC_DIRECT_FE=true npm start
```

`SDC_DEV_PORT` changes the public port from 9000. Use 9000-9009 locally.
Pair `SDC_DIRECT_FE=true` with the frontend target; leave it unset for a backend.
Use the frontend target when testing onboarding: the catalog backend does not
serve the onboarding JavaScript or API proxy routes.

The gateway creates development-only authorization cookies and headers. The cookies and user information come from the
`/configurations/mock.json` file under the nodes `sdcConfig.cookie` and `sdcConfig.userTypes` respectively.

The default user role is the node `sdcConfig.userTypes.designer` (configurable with the npm parameter --env.role).


### Application Configuration

Application configuration `dev.js` or `prod.js` and `menu.js` are located in
`configurations/`. CLI replaces `src/environments/environment.ts` with its
production counterpart for the production build, selecting both the configuration
and production mode. Asset copying and global-script order live in `angular.json`.


## Running unit tests

Run `npm test -- --runInBand` for the Jest application suite and coverage.
The npm pretest hook also runs the Node tooling tests for packaging, login and
proxy behavior. Run `npm run test:tools` for only those tests. Karma is not used.

## Running end-to-end tests

Run `npm run cypress` or `npm run cypress-headless` to execute the end-to-end tests via [Cypress](https://www.cypress.io/).

## Working with WebStorm

1. go to File --> Settings --> Languages & Frameworks
2. Under JavaScript -> set JavaScript language version to ECMAScript6
3. Under TypeScript -> select `Enable TypeScript Compiler` and choose `Use tsconfige.json`
