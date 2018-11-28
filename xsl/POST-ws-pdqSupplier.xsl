<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output indent="yes" />

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

<xsl:template match="//*[name()='MessageID']">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
	</xsl:copy>
</xsl:template>

<xsl:template match="//*[name()='PRPA_IN201306UV02'][namespace-uri()]/*[name()='id']">
	<xsl:copy>
		<xsl:apply-templates select="node()"/>
	</xsl:copy>
</xsl:template>


<xsl:template match="//*[name()='PRPA_IN201306UV02'][namespace-uri()]/*[name()='creationTime']">
	<xsl:copy>
		<xsl:apply-templates select="node()"/>
	</xsl:copy>
</xsl:template>

	
	<xsl:strip-space elements="*" />
</xsl:stylesheet> 
