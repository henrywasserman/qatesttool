package com.pqi.responsecompare.reports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pqi.responsecompare.sql.JSONToSQLResponse;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public enum PatchToHTML {

    Instance;


    Logger logger = Logger.getLogger(PatchToHTML.class);

    public String createPatchHTMLForJSON(JsonNode patch, JsonNode actual) {
        StringBuilder html = new StringBuilder();

        html.append(CreateHTMLTableHeader.Instance.header(html,
                0).toString());

        html.append("<th>Operation</th>" +
                "<th>ActualValue</th><th>ExpectedValue</th></tr>");

        for (JsonNode diff : patch) {
            Iterator<String> keys = diff.fieldNames();
            String key = "";
            String value = "";
            String opValue = "";
            String actualValue = "";
            String extraColumn = "";
            String path = "";
            while (keys.hasNext()) {
                key = keys.next();
                value = diff.get(key).toString().replaceAll("\"", "");

                if (key.equals("op")) {
                    opValue = value.replaceAll("\"", "");
                    html.append("<td>" + opValue + "</td>");
                    continue;
                }

                if (opValue.equals("remove")) {
                    if (key.equals("path")) {
                        if (value.split("/").length == 3) {
                            continue;
                        }
                        if (value.split("/").length == 2) {
                            extraColumn = value.split("/")[1].replace("\"", "");
                            html.append("<td>" + extraColumn + "</td><td></td><td></td><td>"
                                    + "</td></tr>");
                            continue;
                        }
                        if (key.equals("value")) {

                            html.append("<td>" +
                                    value + "</td><td></td></tr>");

                            continue;
                        }

                    }

                }

                if (opValue.equals("add")) {
                    if (key.equals("path")) {
                        continue;
                    }
                    if (key.equals("value")) {

                        html.append("<td>" +
                                value + "</td><td></td></tr>");

                        continue;
                    }
                }

                if (opValue.equals("replace")) {

                    if (key.equals("path")) {

                        path = value;
                        continue;
                    }
                    if (key.equals("value")) {

                        html.append("<td>" + actual.at(path).toString().replaceAll("\"","") + "</td>");

                        actual.at("").findValue("");
                        html.append("<td>"
                                + value
                                + "</td></tr>");
                        continue;
                    }
                }

                if (opValue.equals("copy")) {
                    if (key.equals("path")) {
                        if (value.split("/").length == 3) {
                        } else if (value.split("/").length == 2) {
                        }
                        continue;
                    }
                    if (key.equals("from")) {
                        if (value.split("/").length == 3) {
                        } else if (value.split("/").length == 2) {
                        }
                        continue;

                    }
                    if (key.equals("value")) {
                        continue;
                    }

                }

                if (opValue.equals("move")) {
                    if (key.equals("path")) {
                        if (value.split("/").length == 3) {
                        } else if (value.split("/").length == 2) {
                        }
                        continue;
                    }
                    if (key.equals("from")) {
                        if (value.split("/").length == 3) {
                        } else if (value.split("/").length == 2) {
                        }
                        continue;

                    }
                    if (key.equals("value")) {
                        continue;
                    }

                }
            }
        }

        html.append("</table>");

        return html.toString();
    }


    public String createPatchHTML(JsonNode patch, JsonNode actual) {

        StringBuilder html = new StringBuilder();

        html.append(CreateHTMLTableHeader.Instance.header(html,
                0).toString());

        html.append("<th>Operation</th><th>Column</th><th>Row</th>" +
                "<th>ActualValue</th><th>ExpectedValue</th></tr>");

        for (JsonNode diff : patch) {
            Iterator<String> keys = diff.fieldNames();
            String key = "";
            String value = "";
            String opValue = "";
            String column = "";
            String row = "";
            String actualValue = "";
            String extraColumn = "";
            while (keys.hasNext()) {
                key = keys.next();
                value = diff.get(key).toString().replaceAll("\"", "");

                if (key.equals("op")) {
                    opValue = value.replaceAll("\"", "");
                    html.append("<td>" + opValue + "</td>");
                    continue;
                }

                if (opValue.equals("remove")) {
                    if (key.equals("path")) {
                        if (value.split("/").length == 3) {
                            column = value.split("/")[1];
                            row = value.split("/")[2].replace("\"", "");
                            html.append("<td>" + column + "</td>");
                            html.append("<td>" + Integer.valueOf(Integer.valueOf(row) + 1).toString() + "</td><td></td><td></td></tr>");
                            continue;
                        }
                        if (value.split("/").length == 2) {
                            extraColumn = value.split("/")[1].replace("\"", "");
                            html.append("<td>" + extraColumn + "</td><td></td><td></td><td>"
                                    + "</td></tr>");
                            continue;
                        }
                        if (key.equals("value")) {
                            if (actual.get(column) == null) {
                                html.append("<td></td><td></td></tr>");
                            } else {
                                html.append("<td>" +
                                        value + "</td><td></td></tr>");
                            }
                            continue;
                        }

                    }

                }

                if (opValue.equals("add")) {
                    if (key.equals("path")) {
                        if (value.split("/").length == 3) {
                            column = value.split("/")[1];
                            row = value.split("/")[2].replace("\"", "");
                            html.append("<td>" + column + "</td>");
                            html.append("<td>" + Integer.valueOf(Integer.valueOf(row) + 1).toString() + "</td>");
                        } else if (value.split("/").length == 2) {
                            column = value.split("/")[1].replace("\"", "");
                            html.append("<td>" + column + "</td><td></td>");
                        } else if (value.split("/").length == 0) {
                            html.append("<td></td><td></td>");
                        }

                        continue;
                    }
                    if (key.equals("value")) {
                        if (actual.get(column) == null) {
                            html.append("<td></td><td></td></tr>");
                        } else {
                            html.append("<td>" +
                                    value + "</td><td></td></tr>");
                        }
                        continue;
                    }
                }

                if (opValue.equals("replace")) {

                    if (key.equals("path")) {
                        if (value.split("/").length == 3) {
                            column = value.split("/")[1];
                            row = value.split("/")[2].replace("\"", "");
                            html.append("<td>" + column + "</td>");
                            html.append("<td>" + Integer.valueOf(Integer.valueOf(row) + 1).toString() + "</td>");
                        } else if (value.split("/").length == 2) {
                            html.append("<td>made it here</td><td></td>");
                        }
                        continue;
                    }
                    if (key.equals("value")) {
                        if (!column.isEmpty() && !row.isEmpty()) {
                            html.append("<td>" +
                                    actual.get(column).get(Integer.valueOf(row)) + "</td>");
                        } else {
                            html.append("<td></td>");
                        }

                        html.append("<td>"
                                + value
                                + "</td></tr>");
                        continue;
                    }
                }

                if (opValue.equals("copy")) {
                    if (key.equals("path")) {
                        if (value.split("/").length == 3) {
                            column = value.split("/")[1];
                            row = value.split("/")[2].replace("\"", "");
                            html.append("<td>" + column + "</td>");
                            html.append("<td>" + Integer.valueOf(Integer.valueOf(row) + 1).toString() + "</td></tr>");
                        } else if (value.split("/").length == 2) {
                            column = value.split("/")[1].replace("\"", "");
                            html.append("<td>" + column + "</td><td></td></tr>");
                        }
                        continue;
                    }
                    if (key.equals("from")) {
                        if (value.split("/").length == 3) {
                            column = value.split("/")[1];
                            row = value.split("/")[2].replace("\"", "");
                            html.append("<td>" + column + "</td>");
                            html.append("<td>" + Integer.valueOf(Integer.valueOf(row) + 1).toString() + "</td>");
                        } else if (value.split("/").length == 2) {
                            column = value.split("/")[1].replace("\"", "");
                            html.append("<td>" + column + "</td><td></td>");
                        }
                        continue;

                    }
                    if (key.equals("value")) {
                        html.append(actual.get(column) == null ?
                                "<td></td><td></td></tr>" : "<td>" +
                                actual.get(column).get(Integer.valueOf(row))
                                        .toString() + "</td><td></td></tr>");
                        continue;
                    }

                }

                if (opValue.equals("move")) {
                    if (key.equals("path")) {
                        if (value.split("/").length == 3) {
                            column = value.split("/")[1];
                            row = value.split("/")[2].replace("\"", "");
                            html.append("<td>" + column + "</td>");
                            html.append("<td>" + Integer.valueOf(Integer.valueOf(row) + 1).toString() + "</td></tr>");
                        } else if (value.split("/").length == 2) {
                            column = value.split("/")[1].replace("\"", "");
                            html.append("<td>" + column + "</td><td></td></tr>");
                        }
                        continue;
                    }
                    if (key.equals("from")) {
                        if (value.split("/").length == 3) {
                            column = value.split("/")[1];
                            row = value.split("/")[2].replace("\"", "");
                            html.append("<td>" + column + "</td>");
                            if (!column.isEmpty() && !row.isEmpty()) {
                                html.append("<td>" + Integer.valueOf(Integer.valueOf(row) + 1).toString() + "</td>");
                            }
                        } else if (value.split("/").length == 2) {
                            column = value.split("/")[1].replace("\"", "");
                            html.append("<td>" + column + "</td><td></td>");
                        }
                        continue;

                    }
                    if (key.equals("value")) {
                        html.append(actual.get(column) == null ?
                                "<td></td><td></td></tr>" : "<td>" +
                                actual.get(column).get(Integer.valueOf(row))
                                        .toString() + "</td><td></td></tr>");
                        continue;
                    }

                }
            }
        }

        html.append("</table>");

        return html.toString();
    }

    public String JSONToHTML(String json) throws Exception {
        if (json.isEmpty()) {
            return returnEmptyJsonStringHTML();
        }

        JSONToSQLResponse.Instance.jsonToColumnsAndRows(json);

        ArrayList<String> columns = JSONToSQLResponse.Instance.getColumns();
        HashMap<String, ArrayList> rows = JSONToSQLResponse.Instance.getRows();

        StringBuffer html = new StringBuffer();
        createHTMLHeader(html, 0);
        for (String column : columns) {
            html.append("      <th>" + column + "</th>\n");
        }
        html.append("    </tr>\n");

        int numberOfColumns = rows.get(0).size();
        int numberOfRows = rows.size();
        String column = "";
        String data = "";
        for (int rowCount = 0; rowCount < numberOfRows; rowCount++) {
            html.append("    <tr>\n");
            for (int columnCount = 0; columnCount < numberOfColumns; columnCount++) {
                column = columns.get(columnCount);
                for (int dataCounter = 0; dataCounter < rows.get(columnCount).size(); dataCounter++) {
                    //data = rows.get(columnCount).get(column).get(dataCounter).toString();
                    html.append("      <td>" + data + "</td>\n");
                }
            }
            html.append("    </tr>\n");
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

    public String sqlMapToHTML(HashMap<String, ArrayList<String>> sqlMap) throws Exception {

        String firstColumnName = sqlMap.get("COLUMN_NAMES").get(0);
        int numberOfRecords = sqlMap.get(firstColumnName).size();

        StringBuffer html = new StringBuffer();
        createHTMLHeader(html, numberOfRecords);

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

    private StringBuffer createHTMLHeader(StringBuffer html, int numberOfRecords) {
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
        if (numberOfRecords > 50) {
            html.append("<h3><i><font color=\"blue\">Only Displaying the first 50 of "
                    + Integer.valueOf(numberOfRecords).toString() + " records</font></i></h3>");
        }
        html.append("    <table style=\"width:1000px\">\n");
        html.append("     <tr>\n");
        return html;
    }
}