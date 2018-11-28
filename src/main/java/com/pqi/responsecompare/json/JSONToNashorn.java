package com.pqi.responsecompare.json;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.request.Get;


import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.HashMap;

public enum JSONToNashorn {
	Instance;

	Logger logger = Logger.getLogger(Get.class);

	private ScriptEngineManager manager = null;
	private ScriptEngine engine = null;
    private String result = "";
	private HashMap<String,Integer> hash = null;
	private HashMap<String,Integer> fixedHash = null;
	private HashMap<String,String> stringHash = null;
    private String json = "";

	private JSONToNashorn() {
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("JavaScript");
		try {
			engine.eval("load('https://raw.githubusercontent.com/lodash/lodash/4.13.1/dist/lodash.js');");
		} catch (Exception e) {
			logger.error("Could not load lodash");
		}
	}

	public void setJsonResponse(String json) throws Exception {
		this.json = json;
	}
	
	public String evaluateJSON(String scriptlet) throws Exception {
		engine.eval("var json_object = " + json );
		result =  engine.eval(scriptlet).toString();
        return result;
	}

	public HashMap<String,Integer> evaluateJSONHash(String scriptlet) throws Exception {
		String key = "";
		Integer value = null;
		ScriptObjectMirror scriptObjectMirror = null;
		engine.eval("var json_object = " + json );
		engine.eval(scriptlet);
		scriptObjectMirror = (ScriptObjectMirror) engine.get("hashmap");
		hash = new HashMap(scriptObjectMirror);
		if (PropertiesSingleton.Instance.getProperty("testrail-create-template").equals("true")) {
			createReqFile(hash);
		}
		fixedHash = new HashMap<String,Integer>();
		for (HashMap.Entry<String,Integer> entry : hash.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if(key.contains("/")) {
				fixedHash.put(StringUtils.replace(key,"/","_"), value);
			} else {
				fixedHash.put(key,value);
			}
		}

		return fixedHash;

	}

	private void createReqFile(HashMap<String,Integer> testmap) throws Exception {
		File reqfile = new File("testrail_outline.req");
		if (reqfile.exists()) {
			reqfile.delete();
		}
		for (HashMap.Entry <String,Integer> entry : testmap.entrySet()) {
			FileUtils.write(reqfile, "TESTCASE ","UTF-8",true);
			FileUtils.write(reqfile, entry.getKey(),"UTF-8",true);
			FileUtils.write(reqfile, " ,description\n","UTF-8",true);
			FileUtils.write(reqfile, "","UTF-8",true);
		}
	}
}