package com.pqi.responsecompare.configuration;

import com.pqi.responsecompare.json.JSONToMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public enum PropertiesSingleton {

	Instance;

	private Properties props = new Properties();
	private Properties global_header_props = new Properties();
	private Properties environment_agnostic_props = new Properties();
	private String requestprops = null;
	private String headerprops = null;
	private String environmentagnosticprops = null;
	private FileInputStream fis = null;
		
	private PropertiesSingleton() {
		if (System.getProperties().getProperty("data.dir") == null) {
			requestprops = new String(System.getProperty("user.dir") +
				File.separator + "properties/" + ((System.getProperties().getProperty("propfile") == null) ? "responsecompare.properties" :
				System.getProperties().getProperty("propfile") + ".properties"));
			
			headerprops = new String(System.getProperty("user.dir") +
				File.separator + "properties/" + ((System.getProperties().getProperty("propfile") == null) ? "globalheaders.properties" :
				System.getProperties().getProperty("propfile") + ".properties"));

			environmentagnosticprops = new String(System.getProperty("user.dir") +
					File.separator + "properties/" + ((System.getProperties().getProperty("propfile") == null) ? "environmentagnostic.properties" :
					System.getProperties().getProperty("propfile") + ".properties"));

		}
		try {
			fis = new FileInputStream(requestprops);
			props.load(fis);
			fis.close();
			
			fis = new FileInputStream(headerprops);
			global_header_props.load(fis);
			fis.close();

			fis = new FileInputStream(environmentagnosticprops);
			environment_agnostic_props.load(fis);
			fis.close();
			props.putAll(environment_agnostic_props);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}		

	public Properties getProps(){
		return props;
	}

	public void reloadEnvironmentAgnosticProps() throws Exception {
		fis = new FileInputStream(environmentagnosticprops);
		environment_agnostic_props.load(fis);
		fis.close();
		props.putAll(environment_agnostic_props);
		JSONToMap.Instance.addPropertiesToMap();
	}

	public void putAllProps(Properties anyprops) {
		props.putAll(anyprops);
	}

	public void setProperty(String key, String value) {
		props.setProperty(key,value);
	}
	
	public String getProperty(String key) {
		String prop = "";
		if (System.getProperty(key) != null) {
			prop = System.getProperty(key).toString();
		} else {
			if (props.get(key) != null) {
				prop = props.get(key).toString();
			}
		}
		return prop;
	}

	public Properties getHeaderProps() {
		return global_header_props;
	}

	public Properties getEnvironmentAgnosticProps() {return environment_agnostic_props;}
}
