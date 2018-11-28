<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 

<xsl:output indent="yes" />

<xsl:output method="xml"/>
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
<xsl:template match="ETag">
	<xsl:copy>
		<xsl:apply-templates select="*|@*"/>
	</xsl:copy>
</xsl:template>	
<xsl:template match="ExecutionTime">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
	</xsl:copy>
</xsl:template>	
<xsl:template match="ResponseHeaders">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
	</xsl:copy>
</xsl:template>	
<xsl:template match="MatchCount">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
	</xsl:copy>
</xsl:template>	
<xsl:template match="TransactionKey">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
	</xsl:copy>
</xsl:template>	

	<xsl:strip-space elements="*"/>
</xsl:stylesheet> 
