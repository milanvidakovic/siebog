importScripts("/siebog/radigost/radigost.js");

function RemoteAgent() {
};

RemoteAgent.prototype = new Agent();

RemoteAgent.prototype.onMessage = function(msg) {
	this.onStep(msg.sender.str + " says: " + msg.content);
};

setAgentInstance(new RemoteAgent());