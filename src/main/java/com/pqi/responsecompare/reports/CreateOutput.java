package com.pqi.responsecompare.reports;

import com.pqi.responsecompare.request.TestCase;
import com.pqi.responsecompare.sql.JSONToSQLResponse;
import org.apache.log4j.Logger;
import java.util.*;

public enum CreateOutput {
	
	Instance;
		
	public Properties responsecompare_props = null;
	private String responseCompareRoot = null;

	Logger logger = Logger.getLogger(CreateOutput.class);

	public String sqlMapToJSON(HashMap<String, ArrayList<String>> sqlMap) throws Exception {
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
                    json.append("]}");
                } else {
                    json.append("],");
                }
            } else {


                for (String data : sqlArray) {
                    rowCounter++;
                    if (rowCounter == sqlArray.size()) {
                        if (columnCounter == sqlMap.get("COLUMN_NAMES").size()) {
                            json.append("\"" + data + "\"]}");

                        } else {
                            json.append("\"" + data + "\"],\n");
                        }
                    } else {
                        if (columnCounter == sqlMap.get("COLUMN_NAMES").size()) {
                            json.append("\"" + data + "\",\n");
                        } else {
                            json.append("\"" + data + "\",\n");
                        }
                    }
                }
            }
        }
		return json.toString();
	}

	public String JSONToHTML(String json, TestCase test) throws Exception {
		if (json.isEmpty()) {
			return returnEmptyJsonStringHTML();
		}

		JSONToSQLResponse.Instance.jsonToColumnsAndRows(json);

		ArrayList<String> columns = JSONToSQLResponse.Instance.getColumns();
		HashMap<String,ArrayList> rows = JSONToSQLResponse.Instance.getRows();

		int numberOfColumns = columns.size();
		int numberOfRows = 0;
		if (rows.get(columns.get(0)) != null) {
			numberOfRows = rows.get(columns.get(0)).size();
		}

		StringBuilder html = new StringBuilder();

		if (numberOfRows >= 50) {
			html.append("<h3><i><font color=\"blue\">Only Displaying the first 50 of "
					+ numberOfRows +  " records</font></i></h3>");
		}

		CreateHTMLTableHeader.Instance.header(html,0);
		for (String column : columns) {
			html.append("      <th>" + column + "</th>\n");
		}
		html.append("    </tr>\n");

		String column = "";
		String data = "";

		int maxRows = 50;
		for (int rowCount = 0; rowCount < numberOfRows; rowCount++ ) {
			html.append("    <tr>\n");
			for (int columnCount = 0; columnCount < numberOfColumns; columnCount++) {
					column = columns.get(columnCount);
					data = rows.get(column).get(rowCount).toString();
					html.append("      <td>" + data + "</td>\n");
			}
			html.append("</tr>");

			if (rowCount == 50) {
				break;
			}
		}

		html.append("</table>");
		html.append("</body>");
		html.append("</html>");
		return html.toString();

	}

	public String returnEmptyJsonStringHTML() {

		return "<html><body>" +
				"<h3 style=\"color: #4485b8;\">Expected JSON Results have not been saved and/or checked in yet.</h3>" +
				"</body><html>";
	}

	public String sqlMapToHTML(HashMap<String,ArrayList<String>> sqlMap) throws Exception {

		String firstColumnName = sqlMap.get("COLUMN_NAMES").get(0);
		int numberOfRecords = 0;
		if (sqlMap.get(firstColumnName) != null) {
			numberOfRecords = sqlMap.get(firstColumnName).size();
		}

		StringBuilder html = new StringBuilder();
		CreateHTMLTableHeader.Instance.header(html,numberOfRecords);

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
			if (i > 50) {
				break;
			}
		}
		html.append("</table>");
		html.append("</body>");
		html.append("</html>");
		return html.toString();
	}
}