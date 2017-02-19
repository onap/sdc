# Prerequisites

1. install [node.js](http://nodejs.org/download/)
2. install [git](http://git-scm.com/). __Make sure to select the option to add git into $PATH__
3. install grunt with dependencies `npm install -g bower grunt-cli`

 if running on MacOS/Linux the command should be run with `sudo`

# Running the server

Make sure all the client and npm dependencies installed by running the following commands:

1. `npm install`
2. `bower install`



You are then presented with 3 options `ngnix`, `test`, `build`

1. `grunt serve  --env=mock` will setup a dev(nginx) server under `http://localhost:9000` with mock configurations. The are also `grunt serve:test` and `grunt serve:prod` options
2. `grunt test` will run all the unit tests in the project
3. `grunt build` will run a build process resulting with a `dist/` folder including the version ready to be deployed (this task should be mainly run on the CI server)
3. `grunt build:dev` will deploy to nginx a production artifact, (minify files)  

# Webstorm

Although any text editor can be used to write angular applications Webstorm is the most convenient for the task. In case Webstorm is chosen make sure it has the following plugins:

* `.editorconfig` - this plugin will keep line indentation same across all developers
* `angular.js` - this plugin will help autocompleting angular syntax
* `markdown` - this one will give nice support to write .md files such as this one you are reading right now

These can be found in plugins settings section by pressing PC: `CTRL + SHIFT + A` MAC: `CMD + SHIFT + A` and typing addons


--> DO NOT COMMIT ANYTHING BEFORE RUNNING grunt build / grunt nginx / grunt nginx:mock --env=mock <--
