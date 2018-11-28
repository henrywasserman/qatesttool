package com.pqi.responsecompare.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.pqi.responsecompare.configuration.PropertiesSingleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.util.*;

public enum JSONToMap {
	
	Instance;
	
	static Logger logger = Logger.getLogger(JSONToMap.class);
	
	private HashMap<String,Object> response_map = null;
	private HashMap<String,Integer> testrail_map = null;
	private ArrayList<String> list = new ArrayList<String>();

	private JSONToMap() {
		Properties envAgnostic = PropertiesSingleton.Instance.getEnvironmentAgnosticProps();
		Properties responseCompare = PropertiesSingleton.Instance.getProps();
		response_map = new HashMap<String,Object>((Map)envAgnostic);
		combineMaps(new HashMap<String,Object>((Map)responseCompare));

		testrail_map = new HashMap<String,Integer>();
	}

	public void addSystemPropertiesToMap() {
		if (System.getProperty("SIMPLE_NAME_IDS") == null) {
			String nameIDS = System.getProperty("NAME_IDS");
			nameIDS = "'" + nameIDS + "'";
			nameIDS = StringUtils.replace(nameIDS,",","','");
			System.setProperty("SIMPLE_NAME_IDS",nameIDS);
		}
		if (System.getProperty("NAME_IDS") != null) {
			String nameIDS = System.getProperty("NAME_IDS");
			if (!nameIDS.contains("'")) {
				nameIDS = "'||'''" + nameIDS + "'''||'";
				nameIDS = StringUtils.replace(nameIDS, ",", "'''||','||'''");
				System.setProperty("NAME_IDS", nameIDS);
			}
		}
		combineMaps(new HashMap<String,Object>((Map)System.getProperties()));
	}


	public void addPropertiesToMap() {
		Properties envAgnostic = PropertiesSingleton.Instance.getEnvironmentAgnosticProps();
		combineMaps(new HashMap<String,Object>((Map)envAgnostic));
	}

	public void cleanJSONToMap() {
		response_map = new LinkedHashMap<String,Object>();
	}
	
	public HashMap<String,Object> getMap() throws Exception {		
	 	return response_map; 
	}

	public void setResponseMap(JsonNode node,String field) throws Exception {
		
		if (node.isObject()) {
			Iterator<String> fieldNames = node.fieldNames();
			while (fieldNames.hasNext()) {
				String inner_field = fieldNames.next();
				//logger.info("Here is inner_field: " + inner_field);
				if (node.isContainerNode()) {
					setResponseMap(node.get(inner_field),inner_field);
				}
			}
			
		} else if (node.isArray()) {
			Iterator<JsonNode> datasetElements = node.iterator();
			while(datasetElements.hasNext()) {
				setResponseMap(datasetElements.next(),"");
			}
			
		} else if (node.isBinary()) {
			//logger.info("Here is node: " + node.toString());
			response_map.put(field, node.toString());
			return;
		} else if (node.isBoolean()) {
			//logger.info("Here is boolean:" + Boolean.valueOf(node.getBooleanValue()).toString());
			response_map.put(field, Boolean.valueOf(node.booleanValue()).toString());
			return;
		} else if (node.isMissingNode()) {
			//logger.info("Node is Missing Node");
		} else if (node.isNull()) {
			//logger.info("Node is Null");
			response_map.put(field,null);
		} else if (node.isNumber()) {
			//logger.info("Here is number:" + Long.toString(node.getLongValue()));
			response_map.put(field, Long.toString(node.longValue()));
		} else if (node.isTextual()) {
			//logger.info("Here is text: " + node.toString().replaceAll("^\"|\"$", ""));
			response_map.put(field, node.toString().replaceAll("^\"|\"$", ""));
			return;
		} else if (node.isPojo()) {
			//logger.info("Node is Pojo");
		}
	}
	
	public void setArrayList(JsonNode node,String field, String key_field) throws Exception {
		
		if (node.isObject()) {
			Iterator<String> fieldNames = node.fieldNames();
			while (fieldNames.hasNext()) {
				String inner_field = fieldNames.next();
				//logger.info("Here is inner_field: " + inner_field);
				if (node.isContainerNode()) {
					setArrayList(node.get(inner_field),inner_field, key_field);
				}
			}
			
		} else if (node.isArray()) {
			Iterator<JsonNode> datasetElements = node.iterator();
			while(datasetElements.hasNext()) {
				setArrayList(datasetElements.next(),"", key_field);
			}
			
		} else if (node.isBinary()) {
			//logger.info("Here is node: " + node.toString());
			//response_map.put(field, node.toString());
			return;
		} else if (node.isBoolean()) {
			//logger.info("Here is boolean:" + Boolean.valueOf(node.getBooleanValue()).toString());
			//response_map.put(field, Boolean.valueOf(node.getBooleanValue()).toString());
			return;
		} else if (node.isMissingNode()) {
			//logger.info("Node is Missing Node");
		} else if (node.isNull()) {
			//logger.info("Node is Null");
			//response_map.put(field,null);
		} else if (node.isNumber()) {
			//logger.info("Here is number:" + Long.toString(node.getLongValue()));
			//response_map.put(field, Long.toString(node.getLongValue()));
		} else if (node.isTextual()) {
			//logger.info("Here is text: " + node.toString().replaceAll("^\"|\"$", ""));
			if (field.equals(key_field)) {
				list.add(URLEncoder.encode(node.toString().replaceAll("^\"|\"$", ""),"UTF8"));
			}
			return;
		} else if (node.isPojo()) {
			//logger.info("Node is Pojo");
		}
	}
	
	public void clearArrayList() {
		list.clear();
	}
	
	public ArrayList<String> getArrayList() {
		return list;
	}
	
	public void combineMaps(HashMap<String,Object> source) {
		
		for (Map.Entry<String,Object> entry : source.entrySet()) {
			response_map.put(entry.getKey(), entry.getValue());
		}
	}
	
	public void put(String string, Object object) {
		response_map.put(string, object);
	}

	public void putTestRailDescriptions(HashMap<String, Integer> map) { testrail_map.putAll(map); }

	public HashMap<String, Integer> getTestRailDescriptions() {
		return testrail_map;
	}
}
