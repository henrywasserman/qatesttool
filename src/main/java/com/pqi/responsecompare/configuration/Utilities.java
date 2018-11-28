package com.pqi.responsecompare.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;

public enum Utilities {
	
	Instance;
		
	public Properties responsecompare_props = null;
	private String responseCompareRoot = null;
	private static int startNumber = 0;
	private static int incrementNumber = 0;
	
	Logger logger = Logger.getLogger(Utilities.class);
	public void fileChecker(String filename) {
		File file = null;
		file = new File(filename);
		if (!file.exists()) {
			try {
				FileUtils.touch(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    public int getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(String value) {
        this.startNumber = Integer.valueOf(value).intValue();
    }

    public void setIncrementNumber() {
        if (this.incrementNumber == 0) {
            this.incrementNumber = startNumber + incrementNumber;
        } else {
            this.incrementNumber++;
        }
    }

    public String getIncrementNumber() {
        return Integer.valueOf(incrementNumber).toString();
    }

	public void logHeaders(HttpResponse response) {
		Header headers[] = null;
	    
	    headers = response.getAllHeaders();
	    StringBuffer sb = new StringBuffer();
	    sb.append("Here are the response headers: ");
	    for (Header header : headers) {
	    	sb.append(header.toString());
	    }
	    
	    logger.info(sb.toString());
	}
	
	public void logHeaders(HttpGet request) {
		Header headers[] = null;
	    
	    headers = request.getAllHeaders();
	    StringBuffer sb = new StringBuffer();
	    for (Header header : headers) {
	    	sb.append(header.toString());
	    }
	    
	    if(sb.length()>0){
	    	logger.info("Here are the request headers: "+sb.toString());

	    } 
	}
	
	
	/**
	 * Fill in properties.
	 * ToDO make Singleton
	 * @return loaded properties
	 */
	public Properties  getTestProperties(){
	//make singleton	
		if (responsecompare_props==null){
	    	populateResponseCompareProperties();
		}	
		return responsecompare_props;

	}
	
	protected void populateResponseCompareProperties(){
		//already  populated. Nothing to do.
		if (responsecompare_props!=null) return;
		StringBuffer requestprops = null;
		if (System.getProperties().getProperty("data.dir") == null) {
			requestprops = new StringBuffer(System.getProperty("user.dir") +
					File.separator +
					"properties" + File.separator +
					((System.getProperties().getProperty("propfile") == null) ? "responsecompare.properties" :
						System.getProperties().getProperty("propfile") + ".properties"));
		}
		responsecompare_props = new Properties();

		
		try {
				FileInputStream fis = new FileInputStream(
					requestprops.toString());
				responsecompare_props.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	
	}
	
	public  Properties getRequestProperties() {
		Properties props = new Properties();
		StringBuffer builddir = null, requestprops = null;
		
		if (System.getProperties().getProperty("data.dir") == null) {
			builddir = new StringBuffer(System.getProperty("user.dir"));
			requestprops = new StringBuffer("properties/request.properties");
		}

		else {

			if (System.getProperties().getProperty("data.dir")
					.equalsIgnoreCase("${dir.xstream.data}")) {
				builddir = new StringBuffer(System.getProperties().getProperty(
						"user.dir"));
				builddir = new StringBuffer(builddir.substring(0,
						builddir.indexOf("request")));

				requestprops = new StringBuffer(builddir.toString());
				requestprops.append("request/properties/request.properties");

			} else {
				builddir = new StringBuffer(System.getProperties().getProperty(
						"data.dir"));
				builddir = new StringBuffer(builddir.substring(0,
						builddir.indexOf("data")));
			}
		}
		if (requestprops != null) {
			try {
				FileInputStream fis = new FileInputStream(
						requestprops.toString());
				props.load(fis);
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
		return props;
	}
	
	public String extractVariablesFromRequest(String url) throws Exception {
		//The second url must contain at least one ${variable}
		if (!url.contains("${")) {
			throw new Exception("The url: " + url + " must contain at least one ${} variable");
		}
		Pattern p = Pattern.compile("\\$\\{[\\w]+\\}");
		Matcher m = p.matcher(url);
		String variables = "";

	    boolean result = m.find();
	    // Loop through and create a new String with the replacements
	    while(result) {
	    	if (!variables.contains(m.group())) {
	    		variables = variables + (m.group()) + ",";
	    	}
	    	result = m.find();
	    }
	    return variables;
	}
	public List<String> getAllNodeValuesFromResponse(String nodeName, HttpResponse response){
				List<String> values = new ArrayList<String>();
				String content = null;
				try {
					InputStream is = response.getEntity().getContent();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					List<String> nodes = new ArrayList<String>();
					nodes.add(nodeName);
					while ((content = br.readLine()) != null) {
						for (String node : nodes) {
							if (content.contains(node)) {
								content = content.trim();
								content = content.replace("<![CDATA[", "");
								content = content.replace("]]>", "");
								content = content.substring(("<" + node + ">").length(),
								content.indexOf("</" + node + ">"));
								values.add(content);
							}
						}
					}
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				return values;

			}
	
	public String getEndPoint(String name){
		Properties props = PropertiesSingleton.Instance.getProps();
		
		String endPoint = props.getProperty(name + "-protocol") + "://"
				+ props.getProperty(name + "-host")
				+ (props.getProperty(name + "-port").isEmpty() ? "" : ":"
						+ props.getProperty(name + "-port")) 
				+ props.getProperty(name + "-end-point");
	     return  endPoint;
	}
	
	
	/**
	 * @return the top most directory name for ResponseCompare
	 */
	public String getResponseCompareRoot() {
		return System.getProperty("user.dir");
	}
	
	public String getRandomDate(String format) {
		Random  rnd;
		Date    dt;
		long    ms;
		String date = "";

		// Get a new random instance, seeded from the clock
		rnd = new Random();

		// Get an Epoch value roughly between 1940 and 2010
		// -1704153600000L = January 1, 1916
		// Add up to 100 years to it (using modulus on the next long)
		ms = -1704153600000L + (Math.abs(rnd.nextLong()) % (100L * 365 * 24 * 60 * 60 * 1000));

		// Construct a date
		dt = new Date(ms);

		date = new SimpleDateFormat(format).format(dt);

		return date;
	
	}
	
	public HashMap<String,String> createMapFromCSV(ArrayList<String> headers, String line) {
		HashMap<String,String> map = new HashMap<String,String>();
		
		String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)",-1);
		
		int counter = 0;
		for (String value:values) {
			map.put(headers.get(counter), value);
			counter++;
		}
		
		return map;
	}
}