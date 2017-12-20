var stdin = process.stdin;
var inputJSON = '';
var startJSON = false;
stdin.resume();
stdin.setEncoding('utf8');

stdin.on('data', function (chunk) {
	if (chunk.startsWith('{')) {
		startJSON = true;
	}
	if (startJSON) {
		inputJSON += chunk;
	}
});

stdin.on('end', function () {
	let report = JSON.parse(inputJSON);
	if (!report.numFailedTestSuites) {
		return;
	} else {
		console.log('--------------------------------');
		console.log('Failure Summary: \n');
	}
	report.testResults.forEach((suite) => {
		if(suite.status === 'failed') {
			console.log('Suite: ' + suite.name);
			suite.assertionResults.forEach((test) => {
				if (test.status === 'failed') {
					console.log('\tTest: ' + test.title);
				}
			});
		}
	});
});
