var copy = require('copy');

copy(['**/*.json', '**/*.svg', '**/*.html'], 'lib', {
	cwd: './src'
}, function (err, file) {
	// exposes the vinyl `file` created when the file is copied
});