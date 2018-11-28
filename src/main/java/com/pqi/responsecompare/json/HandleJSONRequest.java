package com.pqi.responsecompare.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqi.responsecompare.request.Get;
import com.pqi.responsecompare.request.TestCase;
import net.javacrumbs.json2xml.JsonXmlReader;
import nu.xom.Builder;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

public enum  HandleJSONRequest {
	Instance;
		
	static Logger logger = Logger.getLogger(Get.class);
	private HashMap<String,String> variableHash;
	
	private HandleJSONRequest() {
	}

	public void handleJSON(StringBuffer outputfile, TestCase test ) throws IOException, ParsingException, Exception {
		String jsonString = FileUtils.readFileToString(new File(
				outputfile.toString()));
		try {
			if (!jsonString.isEmpty() && !jsonString.contains("<!DOCTYPE html>")) {

				if (!jsonString.contains("{") && !jsonString.contains("[")) {
					jsonString = "[\""+ jsonString + "\"]";
				}

				JSONToNashorn.Instance.setJsonResponse(jsonString);
				jsonString = "{\"root\":" + jsonString + "}";

				ObjectMapper mapper = new ObjectMapper();
				JsonNode node = mapper.readTree(jsonString);

				JSONToMap.Instance.setResponseMap(node, "");
				//LinkedHashMap<String,Object> variable_map = JSONToMap.Instance.getMap(jsonString);
				//JSONToMap.Instance.combineMaps(variable_map);
				logger.debug(jsonString);

				variableHash = test.getCurrentParsedRequest().getVariableHash();
				if (!variableHash.isEmpty())
				{
					SetCustomJSONMap.Instance
						.setCustomVariables(variableHash);
				}

				if (!test.getCurrentParsedRequest().getMailBoxFolderName().isEmpty()) {
					logger.info("Adding mailboxfolderid");
					String foldername = test.getCurrentParsedRequest().getMailBoxFolderName();
					SetCustomJSONMap.Instance.setMailBoxFolderId(foldername, outputfile.toString());
				}

				String res = convertToXml(jsonString, "", true);

				//ByteArrayOutputStream out = new ByteArrayOutputStream();
				//Serializer serializer = new Serializer(out);
				//serializer.setIndent(2);  // or whatever you like
				//serializer.write(new Builder().build(res, ""));
				//res = out.toString("UTF-8");

				logger.debug(res);
				FileUtils.writeStringToFile(new
						File(outputfile.toString().replace(".json", ".xml")), res);
			} else {
				jsonString = "{\"root\":\"json_was_empty\"}";
				String res = convertToXml(jsonString, "", true);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				Serializer serializer = new Serializer(out);
				serializer.setIndent(2);  // or whatever you like
				serializer.write(new Builder().build(res, ""));
				res = out.toString("UTF-8");

				logger.debug(res);
				FileUtils.writeStringToFile(new
						File(outputfile.toString().replace(".json", ".xml")), res);

			}
			logger.debug(jsonString);
		} catch (Exception e) {
			jsonString = "{\"root\":\"could_not_parse_json\"}";
			String res = convertToXml(jsonString, "", true);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Serializer serializer = new Serializer(out);
			serializer.setIndent(2);  // or whatever you like
			serializer.write(new Builder().build(res, ""));
			res = out.toString("UTF-8");

			logger.debug(res);
			FileUtils.writeStringToFile(new
					File(outputfile.toString().replace(".json", ".xml")), res);
			throw e;
		}

	}
	
	public String convertToXml(final String json, final String namespace, final boolean addTypeAttributes) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
		InputSource source = new InputSource(new StringReader(json));
		Result result = new StreamResult(out);
		transformer.transform(new SAXSource(new JsonXmlReader(namespace, addTypeAttributes),source), result);
        return new String(out.toByteArray()).replace("UNKNOWN HL7 MESSAGE TYPE","UNKNOWN_HL7_MESSAGE_TYPE");
	}


}
