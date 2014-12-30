/* @flow */

// This is a node script you can use to monitor the /dropZone folder
// Anytime a new file is added the scinia scala application is launched
// to deal with the file.

var FP     = require('filepath')
var watchr = require('watchr');
var sys    = require('sys')
var exec   = require('child_process').exec;

var path   = '/Users/cabraham/devscinia/dropZone'

function puts(error, stdout, stderr) { sys.puts(stdout) }

// returns the parent dir of the file located at path
function sourceFromPath(path) {
  var parts = FP.newPath(path).split();
  return parts[parts.length - 2];
} 

console.log('start watching');

watchr.watch({
  paths: [path],
  listeners: {
    error: function(err){
      console.log('an error occured:', err);
    },
    change: function(change,filePath,c,p){
      if (change == "create") {
        try {
          var command = 'sbt \'run process ' + sourceFromPath(filePath) + ' ' + filePath + '\''
          console.log(command)
          exec(command, puts);
        }
        catch(err) {
          console.log(err)
        }
      }
    }
  }
});
