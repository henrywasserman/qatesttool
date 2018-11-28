package com.pqi.responsecompare.configuration;

import org.apache.log4j.Logger;

import java.util.*;

public enum CreateOutput {
	
	Instance;
		
	public Properties responsecompare_props = null;
	private String responseCompareRoot = null;

	Logger logger = Logger.getLogger(CreateOutput.class);

	public String sqlMapToJSON(HashMap<String,ArrayList<String>> sqlMap) throws Exception {
		StringBuffer json = new StringBuffer();
		json.append("{");
		int columnCounter = 0;
		for (String column : sqlMap.get("COLUMN_NAMES")) {
            columnCounter++;
            json.append("\"" + column + "\":[");
            ArrayList<String> sqlArray = sqlMap.get(column);
            int rowCounter = 0;
            if (sqlArray == null) {
                if (columnCounter == sqlMap.get("COLUMN_NAMES").size()) {
                    json.append("],");
                } else {
                    json.append("]}");
                }
            } else {


                for (String data : sqlArray) {
                    rowCounter++;
                    if (rowCounter == sqlArray.size()) {
                        if (columnCounter == sqlMap.get("COLUMN_NAMES").size()) {
                            json.append("{\"" + Integer.valueOf(rowCounter) + "\":\"" + data + "\"}]}");

                        } else {
                            json.append("{\"" + Integer.valueOf(rowCounter) + "\":\"" + data + "\"}],\n");
                        }
                    } else {
                        if (columnCounter == sqlMap.get("COLUMN_NAMES").size()) {
                            json.append("{\"" + Integer.valueOf(rowCounter) + "\":\"" + data + "\"},\n");
                        } else {
                            json.append("{\"" + Integer.valueOf(rowCounter) + "\":\"" + data + "\"},\n");
                        }
                    }
                }
            }
        }
		return json.toString();
	}

	public String sqlMapToHTML(HashMap<String,ArrayList<String>> sqlMap) throws Exception {
		StringBuffer html = new StringBuffer();
		html.append("<html>\n");
		html.append("  <title></title>\n");
		html.append("  <body>\n");
		html.append("    <style>\n");
		html.append("      table,th,td\n");
		html.append("      {\n");
		html.append("        border:1px solid black;\n");
		html.append("        border-collapse:collapse;\n");
		html.append("        text-align:left;\n");
		html.append("        vertical-align: text-top;\n");
		html.append("        margin: 20px;\n");
		html.append("        padding: 5px;\n");
		html.append("      }\n");
		html.append("      th\n");
		html.append("    {\n");
		html.append("      background-color: #f1f1f1;\n");
		html.append("    }\n");
		html.append("    </style>\n");
		html.append("    <table style=\"width:1000px\">\n");
		html.append("     <tr>\n");

		for (String column : sqlMap.get("COLUMN_NAMES")) {
			html.append("      <th>" + column + "</th>\n");
		}
		html.append("    </tr>\n");

		int size = 0;
		if (sqlMap.get(sqlMap.get("COLUMN_NAMES").get(0)) != null) {
            size = sqlMap.get(sqlMap.get("COLUMN_NAMES").get(0)).size();
        }
		for (int i = 0; i < size; i++) {
			html.append("    <tr>\n");
			for (String column : sqlMap.get("COLUMN_NAMES")) {
				html.append("      <td>" + sqlMap.get(column).get(i) + "</td>\n");
			}
			html.append("    </tr>\n");
		}
		html.append("</table>");
		html.append("</body>");
		html.append("</html>");
		return html.toString();
	}
}