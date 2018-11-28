package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.Utilities;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PostImage extends Request {

	static final Logger logger = Logger.getLogger(PostImage.class);
	Integer TestNumber = null;
	static int testnumber = 0;

	public PostImage(TestCase test) throws Exception {
		super(test);
	}

	public PostImage() {
		super();
	}

	public void sendRequest() throws Exception {
		HttpClient httpclient = HttpClientBuilder.create().build();
		String postImageUrl = "";
		String infoUrl = "";
		String variables = "";

		if (test.getRequests().size() == 2) {
			infoUrl = test.getRequests().get(0).getURL().trim();
			postImageUrl = test.getRequests().get(1).getURL().trim();
			variables = Utilities.Instance.extractVariablesFromRequest(postImageUrl);

			infoUrl = "http://" + infoUrl;
			logger.info("The information gathering request is: " + infoUrl);
			httppost = new HttpPost(infoUrl.trim());
			HttpResponse response = httpclient.execute(httppost);
			validateHeaders(response,0);
			HttpEntity entity = response.getEntity();

			setIDs(entity, variables);

			String fixreplace = "";
			for (String replace : replacements.keySet()) {
				fixreplace = replace.replace("$", "\\$");
				fixreplace = fixreplace.replace("{", "\\{");
				fixreplace = fixreplace.replace("}", "\\}");
				postImageUrl = postImageUrl.replaceAll(fixreplace,
						replacements.get(replace));
			}
		} else {
			postImageUrl = test.getRequests().get(0).getURL().trim();
		}

		postImageUrl = "http://" + postImageUrl.trim();

		logger.info("Request: " + postImageUrl.toString());
		logger.info("\n");

		TestNumber = new Integer(++testnumber);
		test.getRequests().get(1).setFinalURL(postImageUrl);
		requestAndSaveImageToFile(this, postImageUrl, true, "");
	}

	protected void setIDs(HttpEntity entity, String nodes) {

		nodes = nodes.replace("${", "");
		nodes = nodes.replace("}", "");

		String[] nodenames = nodes.split(",");

		if (nodes.contains("currenttime")) {

			Calendar calendar = Calendar.getInstance();
			Date currentDate = calendar.getTime();
			Date date = new Date(currentDate.getTime());

			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy.MM.dd.hh.mm.ss");
			replacements.put("${currenttime}", formatter.format(date));
		}

		try {
			InputStream is = entity.getContent();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			while ((output = br.readLine()) != null) {
				logger.info(output);
				for (String node : nodenames) {
					if (output.contains(node)) {
						output = output.trim();
						output = output.replace("<![CDATA[", "");
						output = output.replace("]]>", "");
						output = output.substring(("<" + node + ">").length(),
								output.indexOf("</" + node + ">"));
						replacements.put("${" + node + "}", output);
					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
