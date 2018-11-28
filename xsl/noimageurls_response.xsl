<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 

<xsl:output indent="yes" />

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
<xsl:template match="//image/url"> <!-- Image url file names are encoded using creative id, which is environment dependent -->
	<xsl:copy>
		<xsl:apply-templates select="*|@*"/>	
	</xsl:copy>
</xsl:template>

<xsl:template match="url">
    <xsl:copy>
    	<xsl:choose>
    	    <xsl:when test="contains(text(),'http://')">
				<xsl:copy-of select="substring-after(substring-after(text(),'http://'),'/')"/>
    		</xsl:when>
    		<xsl:when test="contains(text(),'http%3A%2F%2F')">
				<xsl:copy-of select="concat(substring-before(substring-after(text(),'/'),'http%3A%2F%2F'),'http%3A%2F%2F',
    				substring-after(substring-after(substring-after(text(),'%2F'),'%2F'),'%2F'))"/>
    		</xsl:when>
    		<xsl:when test="contains(text(),'/')">
	    		<xsl:copy-of select="substring-after(text(), '/')" />
    		</xsl:when>
    	</xsl:choose>
    </xsl:copy>
</xsl:template>

<xsl:template match="distance">
	<xsl:copy>
		<xsl:value-of select="concat(substring-before(text(),'.'),'.',substring(substring-after(text(),'.'),1,8))" />
	</xsl:copy>
</xsl:template>

<xsl:strip-space elements="*"/>
</xsl:stylesheet> 
