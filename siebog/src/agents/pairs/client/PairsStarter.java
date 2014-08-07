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

package agents.pairs.client;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import siebog.server.xjaf.Global;
import siebog.server.xjaf.agm.AID;
import siebog.server.xjaf.agm.AgentManagerI;
import siebog.server.xjaf.msm.MessageManagerI;
import siebog.server.xjaf.msm.fipa.acl.ACLMessage;
import siebog.server.xjaf.msm.fipa.acl.Performative;
import siebog.server.xjaf.utils.config.XjafCluster;

/**
 * Entry point for the Sender-Receiver case study.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class PairsStarter
{

	public static void main(String[] args) throws NamingException, IOException,
			ParserConfigurationException, SAXException
	{
		String addr = System.getProperty("java.rmi.server.hostname");
		if (args.length != 4 || addr == null)
		{
			System.out.println("I need 4 arguments: NumOfPairs NumIterations "
					+ "PrimeLimit MsgContentLen");
			System.out.println("In addition, set the property java.rmi.server.hostname "
					+ "to the address of this computer.");
			return;
		}

		int numPairs = Integer.parseInt(args[0]);
		int numIterations = Integer.parseInt(args[1]);
		int primeLimit = Integer.parseInt(args[2]);
		int contentLength = Integer.parseInt(args[3]);

		XjafCluster.init(true);

		List<AID> senders = new ArrayList<>();
		AgentManagerI agm = Global.getAgentManager();
		for (int i = 0; i < numPairs; i++)
		{
			// receiver
			AID aid = new AID(Global.SERVER, "Receiver", "R" + i);
			Map<String, Serializable> rcArgs = new HashMap<>();
			rcArgs.put("primeLimit", primeLimit);
			rcArgs.put("numIterations", numIterations);
			agm.start(aid, rcArgs);
			// sender
			aid = new AID(Global.SERVER, "Sender", "S" + i);
			Map<String, Serializable> snArgs = new HashMap<>();
			snArgs.put("myIndex", i);
			snArgs.put("numIterations", numIterations);
			snArgs.put("contentLength", contentLength);
			snArgs.put("resultsServiceAddr", addr);
			agm.start(aid, snArgs);
			senders.add(aid);
		}

		Registry reg = LocateRegistry.createRegistry(1099);
		reg.rebind("ResultsService", new ResultsServiceImpl(numPairs));

		MessageManagerI msm = Global.getMessageManager();
		for (AID aid : senders)
		{
			ACLMessage msg = new ACLMessage(Performative.REQUEST);
			msg.addReceiver(aid);
			msm.post(msg);
		}
	}

}