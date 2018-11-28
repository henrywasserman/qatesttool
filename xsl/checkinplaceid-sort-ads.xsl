<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 

<xsl:output indent="yes" />

<xsl:template match="@campaignid"/>

<xsl:template match="campaignid">
	<xsl:copy>
		<xsl:apply-templates select="*|@*"/>
	</xsl:copy>
</xsl:template>	


<xsl:template match="expirydate">
	<xsl:copy>
		<xsl:apply-templates select="*|@*"/>	
	</xsl:copy>
</xsl:template>
<xsl:template match="creativeid">
	<xsl:copy>
		<xsl:apply-templates select="*|@*"/>	
	</xsl:copy>
</xsl:template>
<xsl:template match="storefrontid">
	<xsl:copy>
		<xsl:apply-templates select="*|@*"/>	
	</xsl:copy>
</xsl:template>

<xsl:template match="*|@*|text()">
  <xsl:copy>
  <xsl:apply-templates select="*|@*|text()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="ilapresponse">
  <xsl:copy>
    <xsl:apply-templates>
	    <xsl:sort select="advertisername" />
    </xsl:apply-templates>
  </xsl:copy>
</xsl:template>

<xsl:strip-space elements="*"/>
</xsl:stylesheet> 
