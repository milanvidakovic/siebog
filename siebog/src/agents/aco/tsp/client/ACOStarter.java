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

package agents.aco.tsp.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import siebog.server.xjaf.Global;
import siebog.server.xjaf.agm.AID;
import siebog.server.xjaf.agm.AgentManagerI;
import siebog.server.xjaf.utils.config.XjafCluster;

/**
 * Entry point for ACO example.
 * 
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */
public class ACOStarter 
{
	public static void main(String[] args) throws NamingException, IOException, ParserConfigurationException, SAXException
	{
		if (args.length != 2)
		{
			System.out.println("I need 2 arguments: NumberOfAnts PathToMapFile");
			return;
		}
		
		XjafCluster.init(true);
		
		final AgentManagerI agm = Global.getAgentManager();
		AID mapAid = new AID(Global.SERVER, "Map", "Map");
		Map<String, Serializable> mapArgs = new HashMap<>();
		mapArgs.put("fileName", args[1]);
		agm.start(mapAid, mapArgs);

		int nAnts = Integer.parseInt(args[0].toString());
		for (int i = 1; i <= nAnts; ++i)
		{
			AID aid = new AID(Global.SERVER, "Ant", "Ant" + i);
			agm.start(aid, null);
		}
	}
}