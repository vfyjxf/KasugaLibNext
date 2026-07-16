var timer = require("kasuga:timer").default;

ScriptingTestApi.printText("[ScriptingTest] Script loaded!");

timer.setInterval(function() {
    ScriptingTestApi.printText("[ScriptingTest] Hello from JS!");
}, 5000);
