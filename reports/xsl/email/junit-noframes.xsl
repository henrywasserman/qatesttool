<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:lxslt="http://xml.apache.org/xslt"
	xmlns:stringutils="xalan://org.apache.tools.ant.util.StringUtils">
	<xsl:output method="html" indent="yes" encoding="UTF-8"
		doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" />
	<xsl:decimal-format decimal-separator="."
		grouping-separator="," />
	<!-- Licensed to the Apache Software Foundation (ASF) under one or more 
		contributor license agreements. See the NOTICE file distributed with this 
		work for additional information regarding copyright ownership. The ASF licenses 
		this file to You under the Apache License, Version 2.0 (the "License"); you 
		may not use this file except in compliance with the License. You may obtain 
		a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless 
		required by applicable law or agreed to in writing, software distributed 
		under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES 
		OR CONDITIONS OF ANY KIND, either express or implied. See the License for 
		the specific language governing permissions and limitations under the License. -->

	<xsl:param name="TITLE">
		API Test Results.
	</xsl:param>

    <xsl:param name="build_url" required="yes" as="xs:string"/>

	<!-- Sample stylesheet to be used with Ant JUnitReport output. It creates 
		a non-framed report that can be useful to send via e-mail or such. -->

	<!-- Global Variable -->
	<xsl:param name="output.dir" select="'.'" />
	<xsl:variable name="xstrcmproot">
		<xsl:value-of select="//property[@name='compare.dir']/@value" />
	</xsl:variable>
	<xsl:variable name="datestamp">
		<xsl:value-of select="//property[@name='datestamp']/@value" />
	</xsl:variable>

	<xsl:variable name="RunDate">
		<xsl:value-of select="//property[@name='TestRunDate']/@value" />
	</xsl:variable>

	<xsl:variable name="lcletters">
		abcdefghijklmnopqrstuvwxyz
	</xsl:variable>
	<xsl:variable name="ucletters">
		ABCDEFGHIJKLMNOPQRSTUVWXYZ
	</xsl:variable>


	<xsl:template match="testsuites">
		<html>
			<head>
				<title>
					<xsl:value-of select="$TITLE" />
				</title>
				<style type="text/css">
					body {
					font:normal 68% verdana,arial,helvetica;
					color:#000000;
					}
					table tr td, table tr th {
					font-size: 68%;
					}
					table.details tr th{
					font-weight: bold;
					text-align:left;
					background:#a6caf0;
					}
					table.details tr td{
					background:#eeeee0;
					}

					p {
					line-height:1.5em;
					margin-top:0.5em; margin-bottom:1.0em;
					}
					h1 {
					margin: 0px 0px 5px; font: 165% verdana,arial,helvetica
					}
					h2 {
					margin-top: 1em; margin-bottom: 0.5em; font: bold 125% verdana,arial,helvetica
					}
					h3 {
					margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica
					}
					h4 {
					margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
					}
					h5 {
					margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
					}
					h6 {
					margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
					}
					.Error {
					font-weight:bold; color:red;
					}
					.Failure {
					font-weight:bold; color:purple;
					}
					.Properties {
					text-align:right;
					}
				</style>
				<script type="text/javascript" language="JavaScript">
					var TestCases = new Array();
					var cur;
					<xsl:for-each select="./testsuite">
						<xsl:apply-templates select="properties" />
					</xsl:for-each>

				</script>
				<script type="text/javascript" language="JavaScript"><![CDATA[
        function displayProperties (name) {
          var win = window.open('','JUnitSystemProperties','scrollbars=1,resizable=1');
          var doc = win.document;
          doc.open();
          doc.write("<html><head><title>Properties of " + name + "</title>");
          doc.write("<style>")
          doc.write("body {font:normal 68% verdana,arial,helvetica; color:#000000; }");
          doc.write("table tr td, table tr th { font-size: 68%; }");
          doc.write("table.properties { border-collapse:collapse; border-left:solid 1 #cccccc; border-top:solid 1 #cccccc; padding:5px; }");
          doc.write("table.properties th { text-align:left; border-right:solid 1 #cccccc; border-bottom:solid 1 #cccccc; background-color:#eeeeee; }");
          doc.write("table.properties td { font:normal; text-align:left; border-right:solid 1 #cccccc; border-bottom:solid 1 #cccccc; background-color:#fffffff; }");
          doc.write("h3 { margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica }");
          doc.write("</style>");
          doc.write("</head><body>");
          doc.write("<h3>Properties of " + name + "</h3>");
          doc.write("<div align=\"right\"><a href=\"javascript:window.close();\">Close</a></div>");
          doc.write("<table class='properties'>");
          doc.write("<tr><th>Name</th><th>Value</th></tr>");
          for (prop in TestCases[name]) {
            doc.write("<tr><th>" + prop + "</th><td>" + TestCases[name][prop] + "</td></tr>");
          }
          doc.write("</table>");
          doc.write("</body></html>");
          doc.close();
          win.focus();
        }
      ]]>
				</script>
			</head>
			<body>
				<a target="_blank">
					<img height="45" width="75" src="http://performance.ec201.wellogic.com/userContent/atl.site.logo" />
				</a>
				<p></p>
				<i></i>
				<br></br>
				<p></p>
				<a name="top"></a>
				<xsl:call-template name="pageHeader" />

				<!-- Summary part -->
				<xsl:call-template name="summary" />
				<hr size="1" width="95%" align="left" />

				<!-- For each class create the part -->
				<xsl:call-template name="classes" />

			</body>
		</html>
	</xsl:template>



	<!-- ================================================================== -->
	<!-- Write a list of all packages with an hyperlink to the anchor of -->
	<!-- of the package name. -->
	<!-- ================================================================== -->
	<xsl:template name="packagelist">
		<h2>Packages</h2>
		Note: package statistics are not computed recursively, they only sum
		up all of its testsuites numbers.
		<table class="details" border="0" cellpadding="5" cellspacing="2"
			width="95%">
			<xsl:call-template name="testsuite.test.header" />
			<!-- list all packages recursively -->
			<xsl:for-each
				select="./testsuite[not(./@package = preceding-sibling::testsuite/@package)]">
				<xsl:sort select="@package" />
				<xsl:variable name="testsuites-in-package"
					select="/testsuites/testsuite[./@package = current()/@package]" />
				<xsl:variable name="testCount" select="sum($testsuites-in-package/@tests)" />
				<xsl:variable name="errorCount"
					select="sum($testsuites-in-package/@errors)" />
				<xsl:variable name="failureCount"
					select="sum($testsuites-in-package/@failures)" />
				<xsl:variable name="timeCount" select="sum($testsuites-in-package/@time)" />

				<!-- write a summary for the package -->
				<tr valign="top">
					<!-- set a nice color depending if there is an error/failure -->
					<xsl:attribute name="class">
                        <xsl:choose>
                            <xsl:when test="$failureCount &gt; 0">Failure</xsl:when>
                            <xsl:when test="$errorCount &gt; 0">Error</xsl:when>
                        </xsl:choose>
                    </xsl:attribute>
					<td>
						<a href="#{@package}">
							<xsl:value-of select="@package" />
						</a>
					</td>
					<td>
						<xsl:value-of select="$testCount" />
					</td>
					<td>
						<xsl:value-of select="$errorCount" />
					</td>
					<td>
						<xsl:value-of select="$failureCount" />
					</td>
					<td>
						<xsl:call-template name="display-time">
							<xsl:with-param name="value" select="$timeCount" />
						</xsl:call-template>
					</td>
					<td>
						<xsl:value-of select="$testsuites-in-package/@timestamp" />
					</td>
					<td>
						<xsl:value-of select="$testsuites-in-package/@hostname" />
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>


	<!-- ================================================================== -->
	<!-- Write a package level report -->
	<!-- It creates a table with values from the document: -->
	<!-- Name | Tests | Errors | Failures | Time -->
	<!-- ================================================================== -->
	<xsl:template name="packages">
		<!-- create an anchor to this package name -->
		<xsl:for-each
			select="/testsuites/testsuite[not(./@package = preceding-sibling::testsuite/@package)]">
			<xsl:sort select="@package" />
			<a name="{@package}"></a>
			<h3>
				Package
				<xsl:value-of select="@package" />
			</h3>

			<table class="details" border="0" cellpadding="5" cellspacing="2"
				width="95%">
				<xsl:call-template name="testsuite.test.header" />

				<!-- match the testsuites of this package -->
				<xsl:apply-templates
					select="/testsuites/testsuite[./@package = current()/@package]"
					mode="print.test" />
			</table>
			<a href="#top">Back to top</a>
			<p />
			<p />
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="classes">
		<xsl:for-each select="testsuite">
			<xsl:sort select="@name" />
			<!-- create an anchor to this class name -->
			<a name="{@name}"></a>
			<h3>Not All Test Failures are Application Defects</h3>

			<table class="details" border="0" cellpadding="5" cellspacing="2"
				width="95%">
				<xsl:call-template name="testcase.test.header" />
				<!-- test can even not be started at all (failure to load the class) 
					so report the error directly -->
				<xsl:if test="./error">
					<tr class="Error">
						<td colspan="4">
							<xsl:apply-templates select="./error" />
						</td>
					</tr>
				</xsl:if>
				<xsl:apply-templates select="./testcase" mode="print.test" />
			</table>
			<div class="Properties">
				<a>
					<xsl:attribute name="href">javascript:displayProperties('<xsl:value-of
						select="@package" />.<xsl:value-of select="@name" />');</xsl:attribute>
					Properties &#187;
				</a>
			</div>
			<p />

			<a href="#top">Back to top</a>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="summary">
		<h2>Summary</h2>
		<xsl:variable name="testCount" select="sum(testsuite/@tests)" />
		<xsl:variable name="errorCount" select="sum(testsuite/@errors)" />
		<xsl:variable name="failureCount" select="sum(testsuite/@failures)" />
		<xsl:variable name="timeCount" select="sum(testsuite/@time)" />
		<xsl:variable name="successRate"
			select="($testCount - $failureCount - $errorCount) div $testCount" />
		<table class="details" border="0" cellpadding="5" cellspacing="2"
			width="95%">
			<tr valign="top">
				<th>Tests</th>
				<th>Failures</th>
				<th>Errors</th>
				<th>Success rate</th>
				<th>Time</th>
			</tr>
			<tr valign="top">
				<xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="$failureCount &gt; 0">Failure</xsl:when>
                    <xsl:when test="$errorCount &gt; 0">Error</xsl:when>
                </xsl:choose>
            </xsl:attribute>
				<td>
					<xsl:value-of select="$testCount" />
				</td>
				<td>
					<xsl:value-of select="$failureCount" />
				</td>
				<td>
					<xsl:value-of select="$errorCount" />
				</td>
				<td>
					<xsl:call-template name="display-percent">
						<xsl:with-param name="value" select="$successRate" />
					</xsl:call-template>
				</td>
				<td>
					<xsl:call-template name="display-time">
						<xsl:with-param name="value" select="$timeCount" />
					</xsl:call-template>
				</td>

			</tr>
		</table>
		<table border="0" width="95%">
			<tr>
				<td style="text-align: justify;">
					Note:
					<i>failures</i>
					are anticipated and checked for with assertions while
					<i>errors</i>
					are unanticipated.
				</td>
			</tr>
		</table>
	</xsl:template>

	<!-- Write properties into a JavaScript data structure. This is based on 
		the original idea by Erik Hatcher (ehatcher@apache.org) -->
	<xsl:template match="properties">
		cur = TestCases['
		<xsl:value-of select="../@package" />
		.
		<xsl:value-of select="../@name" />
		'] = new Array();
		<xsl:for-each select="property">
			<xsl:sort select="@name" />
			cur['
			<xsl:value-of select="@name" />
			'] = '
			<xsl:call-template name="JS-escape">
				<xsl:with-param name="string" select="@value" />
			</xsl:call-template>
			';
		</xsl:for-each>
	</xsl:template>

	<!-- Page HEADER -->
	<xsl:template name="pageHeader">
		<h1>
			<xsl:value-of select="$TITLE" />
		</h1>
		<table width="100%">
			<tr>
				<td align="left"></td>
				<td align="right">
					Designed for use with
					<a href='http://www.junit.org'>JUnit</a>
				</td>
			</tr>
		</table>
		<hr size="1" />
	</xsl:template>

	<xsl:template match="testsuite" mode="header">
		<tr valign="top">
			<th width="80%">Name</th>
			<th>Tests</th>
			<th>Errors</th>
			<th>Failures</th>
			<th nowrap="nowrap">Time(s)</th>
		</tr>
	</xsl:template>

	<!-- class header -->
	<xsl:template name="testsuite.test.header">
		<tr valign="top">
			<th width="80%">Name</th>
			<th>Tests</th>
			<th>Errors</th>
			<th>Failures</th>
			<th nowrap="nowrap">Time(s)</th>
			<th nowrap="nowrap">Time Stamp</th>
			<th>Host</th>
		</tr>
	</xsl:template>

	<!-- method header -->
	<xsl:template name="testcase.test.header">
		<tr valign="top">
			<th>Testcase</th>
			<th>Status</th>
			<th width="60%">Type</th>
			<th nowrap="nowrap">Time(s)</th>
		</tr>
	</xsl:template>


	<!-- class information -->
	<xsl:template match="testsuite" mode="print.test">
		<tr valign="top">
			<!-- set a nice color depending if there is an error/failure -->
			<xsl:attribute name="class">
            <xsl:choose>
                <xsl:when test="@failures[.&gt; 0]">Failure</xsl:when>
                <xsl:when test="@errors[.&gt; 0]">Error</xsl:when>
            </xsl:choose>
        </xsl:attribute>

			<!-- print testsuite information -->
			<td>
				<a href="#{@name}">
					<xsl:value-of select="@name" />
				</a>
			</td>
			<td>
				<xsl:value-of select="@tests" />
			</td>
			<td>
				<xsl:value-of select="@errors" />
			</td>
			<td>
				<xsl:value-of select="@failures" />
			</td>
			<td>
				<xsl:call-template name="display-time">
					<xsl:with-param name="value" select="@time" />
				</xsl:call-template>
			</td>
			<td>
				<xsl:apply-templates select="@timestamp" />
			</td>
			<td>
				<xsl:apply-templates select="@hostname" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="testcase" mode="print.test">
		<xsl:if test="failure | error">
			<tr valign="top">
				<xsl:attribute name="class">
	   				<xsl:choose>
						<xsl:when test="error">Error</xsl:when>
						<xsl:when test="failure">Failure</xsl:when>
						<xsl:otherwise>TableRowColor</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
				<td>
					<xsl:choose>
                        <xsl:when test="contains(@name,'Open Bug') and contains(@name,'Desc')">
                            <xsl:value-of select="substring-before(@name,'Open Bug')" />
                            <font color="green">Open Bug</font><br></br><a href="{translate(substring-before(substring-after(@name,'Open Bug'),'Desc'),'&#32;','')}">
                            <xsl:value-of select="substring-after(@name,'Desc')"/>
                        </a>
                        </xsl:when>
                        <xsl:when test="contains(@name,'Open Bug')">
                            <xsl:value-of select="substring-before(@name,'Open Bug')" />
                            <br></br><a href="{translate(substring-after(@name,'Open Bug'),'&#32;','')}">
                        </a>
                        </xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="@name" />
						</xsl:otherwise>
					</xsl:choose>

				</td>
				<xsl:choose>
					<xsl:when test="failure">
						<td>
							<font color="red">Failure</font>
						</td>
						<td>
							<font color="blue">Filename: </font>
							<xsl:apply-templates select="failure" />
						</td>
					</xsl:when>
					<xsl:when test="error">
						<td>Error</td>
						<td>
							<xsl:apply-templates select="error" />
						</td>
					</xsl:when>
					<xsl:otherwise>
						<td>Success</td>
						<td></td>
					</xsl:otherwise>
				</xsl:choose>
				<td>
					<xsl:call-template name="display-time">
						<xsl:with-param name="value" select="@time" />
					</xsl:call-template>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>


	<!-- Note : the below template error and failure are the same style so just 
		call the same style store in the toolkit template -->
	<xsl:template match="failure">
		<xsl:call-template name="display-failures" />
	</xsl:template>

	<xsl:template match="getfilename">
		<xsl:value-of select="@message" />
	</xsl:template>

	<xsl:template match="error">
		<xsl:call-template name="display-failures" />
	</xsl:template>

	<!-- Style for the error and failure in the testcase template -->
	<xsl:template name="display-failures">
		<script type="javascript">
			function callApplet(f1,f2){
			document.applets[0].readFile1(f1,f2)
			}
		</script>
		<xsl:choose>
			<xsl:when test="not(@message)">
				N/A
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="linkfilename" select="substring-before(@message,'.xml')" />
				<xsl:variable name="linkfilename1"
					select="substring-after($linkfilename,'response')" />
				<xsl:variable name="linkfilename2"
					select="substring-after($linkfilename1,'response')" />
				<xsl:variable name="linkfilename3"
					select="substring-after($linkfilename,'consult')" />
				<xsl:variable name="hostname"
					select="substring-before(substring-after($linkfilename,'/data/consult/'),'/response')" />
				<!-- This shows get, post, or post-xmll -->
				<xsl:variable name="linktype"
					select="substring-before($linkfilename3,'\')" />
				<xsl:variable name="linkfilename5"
					select="substring-before($linkfilename,'response')" />
				<!-- This is just the filename -->
				<xsl:variable name="testfile"
					select="substring-after($linkfilename,$linktype)" />

				<xsl:variable name="dataFile">
					<xsl:call-template name="replace-string-in-text">
						<xsl:with-param name="text" select="$linkfilename2" />
						<xsl:with-param name="replace" select="'\'" />
						<xsl:with-param name="with" select="'/'" />
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of select="$dataFile" />

				<xsl:variable name="xstrcmpDirReplaced">
					<xsl:call-template name="replace-string-in-text">
						<xsl:with-param name="text" select="$xstrcmproot" />
						<xsl:with-param name="replace" select="'\'" />
						<xsl:with-param name="with" select="'/'" />
					</xsl:call-template>
				</xsl:variable>

				<xsl:variable name="fileNameReplaced">
					<xsl:call-template name="replace-string-in-text">
						<xsl:with-param name="text" select="$linkfilename2" />
						<xsl:with-param name="replace" select="'\'" />
						<xsl:with-param name="with" select="'/'" />
					</xsl:call-template>
				</xsl:variable>

				<table>
					<tr>
						<td>
							<a target="_request"
								href="../artifact/qatools/responsecompare/data/consult/request/{$dataFile}.html">Request</a>
						</td>
						<td>
							<a target="_response"
								href="../artifact/qatools/responsecompare/data/consult/{$hostname}/response{$dataFile}.json">Response</a>
						</td>

						<td>
						<a href="{$build_url}API_Test_Results">Compare</a>
						</td>
						<td>
							<a target="_transformedresponse"
								href="../artifact/qatools/responsecompare/data/consult/{$hostname}/response/transformed{$dataFile}.xml">Transformed Response</a>
						</td>
					</tr>
				</table>
				<p></p>
				<xsl:value-of select="@message" />
				<p></p>

				<!-- Doing some color coding display -->
				<!-- <xsl:variable name="findError"> <xsl:value-of select="substring-after(@message,'.xml')"/> 
					</xsl:variable> <xsl:variable name="errorBefore"> <xsl:value-of select="substring-before($findError,'missing.')"/> 
					</xsl:variable> <xsl:if test="not($errorBefore = '')"> <font color="red"><xsl:value-of 
					select="$errorBefore"/> missing.</font> <xsl:value-of select="substring-after(@message,'missing.')"/> 
					</xsl:if> <xsl:if test="$errorBefore = ''"> <xsl:value-of select="substring-after(@message,'.xml')"/> 
					</xsl:if> -->

				<!-- <xsl:value-of select="substring-after(@message,'.xml')"/> -->

			</xsl:otherwise>
		</xsl:choose>

		<!-- display the stacktrace -->
		<!-- <code> <p/> <xsl:call-template name="br-replace"> <xsl:with-param 
			name="word" select="."/> </xsl:call-template> </code> -->
		<!-- the latter is better but might be problematic for non-21" monitors... -->
		<!--pre><xsl:value-of select="."/></pre -->
	</xsl:template>

	<xsl:template name="JS-escape">
		<xsl:param name="string" />
		<xsl:choose>
			<xsl:when test="contains($string,&quot;'&quot;)">
				<xsl:value-of select="substring-before($string,&quot;'&quot;)" />\&apos;
				<xsl:call-template name="JS-escape">
					<xsl:with-param name="string"
						select="substring-after($string,&quot;'&quot;)" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="contains($string,'\')">
				<xsl:value-of select="substring-before($string,'\')" />
				\\
				<xsl:call-template name="JS-escape">
					<xsl:with-param name="string" select="substring-after($string,'\')" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$string" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<!-- template that will convert a carriage return into a br tag @param word 
		the text from which to convert CR to BR tag -->
	<xsl:template name="br-replace">
		<xsl:param name="word" />
		<xsl:choose>
			<xsl:when test="contains($word,'&#xA;')">
				<xsl:value-of select="substring-before($word,'&#xA;')" />
				<br />
				<xsl:call-template name="br-replace">
					<xsl:with-param name="word"
						select="substring-after($word,'&#xA;')" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$word" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- replace a string from a Text -->
	<xsl:template name="replace-string-in-text">
		<xsl:param name="text" />
		<xsl:param name="replace" />
		<xsl:param name="with" />
		<xsl:choose>
			<xsl:when test="contains($text,$replace)">
				<xsl:value-of select="substring-before($text,$replace)" />
				<xsl:value-of select="$with" />
				<xsl:call-template name="replace-string-in-text">
					<xsl:with-param name="text"
						select="substring-after($text,$replace)" />
					<xsl:with-param name="replace" select="$replace" />
					<xsl:with-param name="with" select="$with" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template name="display-time">
		<xsl:param name="value" />
		<xsl:value-of select="format-number($value,'0.000')" />
	</xsl:template>

	<xsl:template name="display-percent">
		<xsl:param name="value" />
		<xsl:value-of select="format-number($value,'0.00%')" />
	</xsl:template>

	<!-- reusable replace-string function -->
	<xsl:template name="replace-string">
		<xsl:param name="text" />
		<xsl:param name="from" />
		<xsl:param name="to" />

		<xsl:choose>
			<xsl:when test="contains($text, $from)">

				<xsl:variable name="before" select="substring-before($text, $from)" />
				<xsl:variable name="after" select="substring-after($text, $from)" />
				<xsl:variable name="prefix" select="concat($before, $to)" />

				<xsl:value-of select="$before" />
				<xsl:value-of select="$to" />
				<xsl:call-template name="replace-string">
					<xsl:with-param name="text" select="$after" />
					<xsl:with-param name="from" select="$from" />
					<xsl:with-param name="to" select="$to" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
