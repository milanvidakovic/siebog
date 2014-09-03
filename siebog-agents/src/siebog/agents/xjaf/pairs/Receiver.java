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

package siebog.agents.xjaf.pairs;

import java.util.Map;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * Upon receiving a request, the agent uses a brute-force algorithm for counting all prime numbers
 * in the [1..primeLimit] interval.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class Receiver extends XjafAgent {
	private static final long serialVersionUID = -677935957265970587L;
	private int primeLimit;
	private int numIterations;

	@Override
	protected void onInit(Map<String, String> args) {
		primeLimit = Integer.parseInt(args.get("primeLimit"));
		numIterations = Integer.parseInt(args.get("numIterations"));
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		--numIterations;
		ACLMessage reply = msg.makeReply(Performative.INFORM);
		reply.setSender(myAid);
		reply.setContent(msg.getContent() + "" + process());
		msm.post(reply);
		if (numIterations <= 0)
			agm.stopAgent(myAid);
	}

	private int process() {
		int primes = 0;
		for (int i = 1; i <= primeLimit; i++) {
			int j = 2;
			while (j <= i) {
				if (i % j == 0)
					break;
				++j;
			}
			if (j == i)
				++primes;
		}
		return primes;
	}
}