package com.pqi.responsecompare.json;
 
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.request.Get;
import com.pqi.responsecompare.request.InterpolateRequest;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
 
public enum  SetCustomJSONMap {
	Instance;
		
	static Logger logger = Logger.getLogger(Get.class);
	
	private ObjectMapper mapper = null;
	private JsonNode rootNode = null;
	
	private SetCustomJSONMap() {
		mapper = new ObjectMapper();
	}

	public void setCustomVariables(HashMap<String,String> variable) throws Exception {
		for (Map.Entry <String, String> entry : variable.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			key = InterpolateRequest.Instance.interpolateString(key);

			if (key.toLowerCase().contains("json_object") && (key.toLowerCase().trim().startsWith("hashmap"))) {
				HashMap<String,Integer> map = null;
				map = JSONToNashorn.Instance.evaluateJSONHash(key);
				JSONToMap.Instance.putTestRailDescriptions(map);
			}
			else if (key.toLowerCase().contains("json_object")) {
                key = JSONToNashorn.Instance.evaluateJSON(key);
                JSONToMap.Instance.put(value,key);
            }
            else if (JSONToMap.Instance.getMap().containsKey(key)) {
				JSONToMap.Instance.put(value,JSONToMap.Instance.getMap().get(key));
				//Added this line to be able to assign any string to a variable
				if (PropertiesSingleton.Instance.getProperty(key).isEmpty()) {
					JSONToMap.Instance.put(key, value);
				} else
				{
					JSONToMap.Instance.put(key,PropertiesSingleton.Instance.getProperty(key));
				}

				if (!PropertiesSingleton.Instance.getProperty(value).isEmpty()) {
					JSONToMap.Instance.put(key,PropertiesSingleton.Instance.getProperty(value));
				} else if (key.equals("static_mpi")) {
					JSONToMap.Instance.put(key,"");
				}
			}
		}
	}

	public void setMailBoxFolderId(String foldername, String json_filename) throws JsonProcessingException, IOException {
		JsonNode mailbox = null;
		rootNode = mapper.readTree(new File(json_filename));
		JsonNode folders = rootNode.findPath("folders");
		Iterator<JsonNode> mailboxes =  folders.elements();
		while(mailboxes.hasNext()) {
			mailbox = mailboxes.next();
			JsonNode folder = mailbox.findValue(foldername);
			if (folder.isBoolean() && folder.booleanValue()) {
				JSONToMap.Instance.put(foldername + "ID", mailbox.findValue("id"));
			}
		}
	}
}