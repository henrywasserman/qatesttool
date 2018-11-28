package com.pqi.responsecompare.reports;

import com.pqi.responsecompare.sql.JSONToSQLResponse;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public enum CreateHTMLTableHeader {
	
	Instance;
		
	public Properties responsecompare_props = null;
	private String responseCompareRoot = null;

	Logger logger = Logger.getLogger(CreateHTMLTableHeader.class);

	public StringBuilder header(StringBuilder html, int numberOfRecords) {
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