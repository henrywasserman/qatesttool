package com.pqi.responsecompare.splunk;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.SSHTunnel;
import com.pqi.responsecompare.security.SSLSocketFactorySingleton;
import com.splunk.*;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;

import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;


public enum SplunkManager {
	Instance;

	static final Logger logger = Logger.getLogger(SplunkManager.class);

	private Service service = null;
	private Args oneshotSearchArgs = new Args();
	private String oneshotSearchQuery = "";
	private DateTime lastLog = null;
	private ArrayList<Event> events;
	private Properties props = PropertiesSingleton.Instance.getProps();

	private void login() throws Exception {
		ServiceArgs loginArgs = new ServiceArgs();
		loginArgs.setUsername(props.getProperty("splunk.username"));
		loginArgs.setPassword(props.getProperty("splunk.password"));
		loginArgs.setHost(props.getProperty("splunk.host"));
		loginArgs.setPort(Integer.parseInt(props.getProperty("splunk.port")));
		loginArgs.setScheme(props.getProperty("splunk.scheme"));
		
		// Create a Service instance and log in with the argument map
		if (PropertiesSingleton.Instance.getProperty("ssh.tunnel.enabled").equals("true")) {
			SSHTunnel.Instance.openTunnel();
		}
		
		SSLSocketFactory SSLOnlySSLFactory = SSLSocketFactorySingleton.INSTANCE.getSSLConnectionSocketFactory();
		Service.setSSLSocketFactory(SSLOnlySSLFactory);
		service = Service.connect(loginArgs);
	}

	public void getLastEventTime() throws Exception {
		oneshotSearchArgs.clear();

		oneshotSearchQuery = "search * index="
				+ props.getProperty("splunk.index") + " | head 1";
		lastLog = DateTime.now(DateTimeZone.forID("UTC")).minusYears(1);
		search(oneshotSearchQuery);
	}

	private void setLastLog() {
		if (!events.isEmpty()) {
			String time = events.get(0).get("_time");
			DateTimeFormatter dtf = DateTimeFormat
					.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'+00:00");
			lastLog = dtf.parseDateTime(time);
		}

	}

	public void search() throws Exception {
		search("search * index=" + props.getProperty("splunk.index")
				+ " | reverse");
	}

	public void search(String oneshotSearchString) throws Exception {

		if (props.get("splunk.enabled").toString().toLowerCase().equals("true")) {

			DateTime end = lastLog.plusYears(2);
			String startString = lastLog.plusMillis(1).toString(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'-00:00");
			String endString = end.toString("yyyy-MM-dd'T'HH:mm:ss.SSS'-00:00");

			oneshotSearchArgs.clear();
			oneshotSearchArgs.put("earliest_time", startString);
			oneshotSearchArgs.put("latest_time", endString);
			oneshotSearchQuery = oneshotSearchString;

			doSearch();
			setLastLog();
		}
	}

	public void doSearch() throws Exception {
		boolean foundException = false;
		if (service == null) {
			login();
		}

		InputStream results_oneshot = null;
		ResultsReaderXml resultsReader = null;
		events = new ArrayList<Event>();

		Iterator<Event> i = null;

		int counter = 0;
		try {
			do {
				results_oneshot = service.oneshotSearch(oneshotSearchQuery,
						oneshotSearchArgs);
				resultsReader = new ResultsReaderXml(results_oneshot);
				i = resultsReader.iterator();
				// Thread.sleep(500);
				counter++;

			}
			// Wait for a Maximum of 5 seconds
			while (i.hasNext() == false && counter < 10);

			if (counter == 10) {
				return;
			}

			// Get the search results and use the built-in XML parser to display
			// them
			logger.info("Searching everything between the "
					+ oneshotSearchArgs.get("earliest_time") + " and "
					+ oneshotSearchArgs.get("latest_time")
					+ " time of the test:\n");
			int j = 0;
			while (i.hasNext()) {
				events.add(i.next());
				logger.info(events.get(j).get("_raw"));
				if (!oneshotSearchQuery.contains("head 1")
						&& events.get(j).get("_raw").toLowerCase()
								.contains("exception")) {
					foundException = true;
				}
				j++;
			}
			resultsReader.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (foundException) {
				getStackTrace();
			}
		}
	}

	private void getStackTrace() {
		String eventsString = "";
		for (Event e : events) {
			eventsString = eventsString + e.get("_raw");
		}
		Assert.assertTrue("Request threw this exception stackTrace: "
				+ eventsString, false);
	}
}