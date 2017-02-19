/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

'use strict';

var path = require('path');
var gulp = require('gulp');
var gulpHelpers = require('gulp-helpers');
var taskMaker = gulpHelpers.taskMaker(gulp);
var _ = gulpHelpers.framework('_');
var runSequence = gulpHelpers.framework('run-sequence');
var i18nTask = require('./tools/gulp/tasks/i18n');
var prodTask = require('./tools/gulp/tasks/prod');
var gulpCssUsage = require('gulp-css-usage').default;
var webpack = require('webpack');
var WebpackDevServer = require('webpack-dev-server');

var localDevConfig = {};
try {
	localDevConfig = require('./devConfig');
} catch (e) {
}
var devConfig = Object.assign({}, require('./devConfig.defaults'), localDevConfig);
var webpackConfig = require('./webpack.config');

function defineTasks(mode) {
	let appName = 'onboarding';
	let dist = 'dist/' + mode + '/';

	let path = {
		locales: 'i18n/',
		jssource: 'src/**/*.js',
		jsxsource: 'src/**/*.jsx',
		html: '**/*.html',
		output: dist,
		assets: './resources/**/*.{css,png,svg,eot,ttf,woff,woff2,otf}',
		json: './src/**/*.json',
		index: './src/index.html',
		heat: './src/heat.html',
		watch: ['./src/**'],
		scss: './resources/scss/**/*.scss',
		css: dist + '/css',
		war: [dist + 'index.html', dist + 'punch-outs_en.js', dist + '**/*.{css,png,svg,eot,ttf,woff,woff2,otf}', dist + '**/*(config.json|locale.json)', 'tools/gulp/deployment/**', 'webapp-onboarding/**'],
		heatWar: [dist + 'heat.html', dist + 'heat-validation_en.js', dist + '**/*.{css,png,svg,eot,ttf,woff,woff2,otf}', dist + '**/*(config.json|locale.json)', 'webapp-heat-validation/**'],
		wardest: 'dist/'
	};

	taskMaker.defineTask('clean', {taskName: 'clean', src: path.output});
	taskMaker.defineTask('copy', {taskName: 'copy-assets', src: path.assets, dest: path.output});
	taskMaker.defineTask('copy', {
		taskName: 'copy-json',
		src: path.json,
		dest: path.output,
		changed: {extension: '.json'}
	});
	taskMaker.defineTask('copy', {
		taskName: 'copy-index.html',
		src: path.index,
		dest: path.output,
		rename: 'index.html'
	});
	taskMaker.defineTask('copy', {
		taskName: 'copy-heat.html',
		src: path.heat,
		dest: path.output,
		rename: 'heat.html'
	});
	taskMaker.defineTask('sass', {
		taskName: 'sass',
		src: path.scss,
		dest: path.css,
		config: {outputStyle: 'compressed'}
	});
	taskMaker.defineTask('compress', {
		taskName: 'compress-war',
		src: path.war,
		filename: appName + '.war',
		dest: path.wardest
	});
	taskMaker.defineTask('compress', {
		taskName: 'compress-heat-war',
		src: path.heatWar,
		filename: 'heat-validation.war',
		dest: path.wardest
	});
	taskMaker.defineTask('watch', {
		taskName: 'watch-stuff',
		src: [path.assets, path.json, path.index, path.heat],
		tasks: ['copy-stuff']
	});
	taskMaker.defineTask('watch', {taskName: 'watch-sass', src: path.scss, tasks: ['sass']});

	gulp.task('copy-stuff', callback => {
		return runSequence(['copy-assets', 'copy-json', 'copy-index.html', 'copy-heat.html'], callback);
	});

	gulp.task('i18n', () => {
		return i18nTask({
			outputPath: path.output,
			localesPath: path.locales,
			lang: 'en'
		}).catch(err => {
			console.log('i18n Task : Error! ', err);
			throw err;
		});
	});


	gulp.task('dependencies', () => {
		//TODO:
	});

}

gulp.task('dev', callback => {
	defineTasks('dev');
	return runSequence('clean', ['i18n', 'copy-stuff'], 'webpack-dev-server', ['watch-stuff'], callback);
});

// Production build
gulp.task('build', callback => {
	defineTasks('prod');
	return runSequence('clean', ['copy-stuff', 'i18n'], 'prod', ['compress-war', 'compress-heat-war'], callback);
});

gulp.task('default', ['dev']);

gulp.task('prod', () => {

	// configure webpack for production
	let webpackProductionConfig = Object.create(webpackConfig);

	for (let name in webpackProductionConfig.entry) {
		webpackProductionConfig.entry[name] = webpackProductionConfig.entry[name].filter(path => !path.startsWith('webpack'));
	}

	webpackProductionConfig.cache = true;
	webpackProductionConfig.output = {
		path: path.join(__dirname, 'dist/prod'),
		publicPath: '/onboarding/',
		filename: '[name].js'
	};
	webpackProductionConfig.resolveLoader = {
		root: [path.resolve('.')],
		alias: {
			'config-json-loader': 'tools/webpack/config-json-loader/index.js'
		}
	};

	// remove source maps
	webpackProductionConfig.devtool = undefined;
	webpackProductionConfig.module.preLoaders = webpackProductionConfig.module.preLoaders.filter(preLoader => preLoader.loader != 'source-map-loader');
	webpackProductionConfig.module.loaders.forEach(loader => {
		if (loader.loaders && loader.loaders[0] === 'style') {
			loader.loaders = loader.loaders.map(loaderName => loaderName.replace('?sourceMap', ''));
		}
	});

	webpackProductionConfig.module.loaders.push({test: /config.json$/, loaders: ['config-json-loader']});
	webpackProductionConfig.eslint = {
		configFile: './.eslintrc',
		failOnError: true
	};
	webpackProductionConfig.babel = {//TODO: remove this when UglifyJS will support user or
		// Webpack 2.0
		presets: ['es2015', 'stage-0', 'react']
	}
	webpackProductionConfig.plugins = [
		new webpack.DefinePlugin({
			'process.env': {
				// This has effect on the react lib size
				'NODE_ENV': JSON.stringify('production')
			},
			DEBUG: false,
			DEV: false
		}),
		new webpack.optimize.DedupePlugin(),
		new webpack.optimize.UglifyJsPlugin()
	];

	// run production build
	return prodTask({
		webpackProductionConfig,
		outDir: 'dist/prod'
	})
		.then(() => {
		})
		.catch(err => {
			if (err && err.stack) {
				console.error(err, err.stack);
			}
			throw new Error('Webpack build FAILED');
		});
});

gulp.task('webpack-dev-server', () => {
	// modify some webpack config options for development
	let myConfig = Object.create(webpackConfig);

	myConfig.devServer.setup = server => {
		let fixture = require('./fixture/fixture');
		let proxy = require('http-proxy-middleware');
		let proxyConfigDefaults = {
			changeOrigin: true,
			secure: false,
			onProxyRes: (proxyRes, req, res) => {
				let setCookie = proxyRes.headers['set-cookie'];
				if (setCookie) {
					setCookie[0] = setCookie[0].replace(/\bSecure\b(; )?/, '');
				}
			}
		};

		let middlewares = [
			(req, res, next) => {
				let match = req.url.match(/^(.*)_en.js$/);
				let newUrl = match && match[1] + '.js';
				if (newUrl) {
					console.log(`REWRITING URL: ${req.url} -> ${newUrl}`);
					req.url = newUrl;
				}
				next();
			},
			fixture({
				enabled: devConfig.useFixture
			})
		];

		// standalon back-end (proxyTarget) has higher priority, so it should be first
		if (devConfig.proxyTarget) {
			middlewares.push(
				proxy(['/api', '/onboarding-api', '/sdc1/feProxy/onboarding-api'], Object.assign({}, proxyConfigDefaults, {
					target: devConfig.proxyTarget,
					pathRewrite: {
						'/sdc1/feProxy/onboarding-api': '/onboarding-api'
					}
				}))
			)
		}

		// Ecorp environment (proxyATTTarget) has lower priority, so it should be second
		if (devConfig.proxyATTTarget) {
			middlewares.push(
				proxy(['/sdc1', '/onboarding-api'], Object.assign({}, proxyConfigDefaults, {
					target: devConfig.proxyATTTarget,
					pathRewrite: {
						// Workaround for some weird proxy issue
						'/sdc1/feProxy/onboarding-api': '/sdc1/feProxy/onboarding-api',
						'/onboarding-api': '/sdc1/feProxy/onboarding-api'
					}
				}))
			)
		}
		server.use(middlewares);
	};

	// Start a webpack-dev-server
	let server = new WebpackDevServer(webpack(myConfig), myConfig.devServer);
	server.listen(myConfig.devServer.port, '0.0.0.0', err => {
		if (err) {
			throw new Error('webpack-dev-server' + err);
		}
	});
});


gulp.task('gulp-css-usage', callback => {
	return gulp.src('src/**/*.jsx').pipe(gulpCssUsage({css: 'dist/dev/css/style.css', babylon: ['objectRestSpread']}));
});

gulp.task('css-usage', callback => {
	defineTasks('dev');
	runSequence('sass', 'gulp-css-usage');
});

