/**
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements. See the NOTICE file 
 * distributed with this work for additional information regarding 
 * copyright ownership. The ASF licenses this file to you under 
 * the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may 
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. 
 * 
 * See the License for the specific language governing permissions 
 * and limitations under the License.
 */

package siebog.agents.test.pingpong;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import siebog.agents.Agent;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.LoggerUtil;

/**
 * Example of a pong agent.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class Pong extends XjafAgent {
	private static final long serialVersionUID = 1L;
	//private static final Logger LOG = LoggerFactory.getLogger(Pong.class);
	private String nodeName;
	private int counter;

	@Override
	protected void onInit(AgentInitArgs args) {
		nodeName = getNodeName();
		LoggerUtil.log("Pong created on " + nodeName, true);
		//LOG.info("Pong created on {}.", nodeName);
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		//System.out.println("Message to Pong: " + msg);
		LoggerUtil.log("Message to Pong: " + msg, true);
		ACLMessage reply = msg.makeReply(Performative.INFORM);
		reply.userArgs.put("pongCreatedOn", nodeName);
		reply.userArgs.put("pongWorkingOn", getNodeName());
		reply.userArgs.put("pongCounter", ++counter);
		msm().post(reply);
	}
}
