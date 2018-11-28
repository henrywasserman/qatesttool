<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 

<xsl:output indent="yes" />

<xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>

  <xsl:template match="@*[.='']"/>
  <xsl:template match="*[not(node())]"/>


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

<xsl:template match="mainmessagetext">
	<xsl:copy>
		<xsl:apply-templates select="*|text()"/>	
	</xsl:copy>
</xsl:template>

<xsl:template match="mainmessagephonetics">
	<xsl:copy>
		<xsl:apply-templates select="*|text()"/>	
	</xsl:copy>
</xsl:template>

<xsl:template match="htmltemplateid"/>

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

<!-- Leave only the first 8 chars, the file header and strip away the rest since it can vary after coming over the wire -->

<!--
<xsl:template match="//image/data">
	<xsl:copy>
		<xsl:copy-of select="substring(text(),1,8)"/> 

	</xsl:copy>
</xsl:template>
-->

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

<xsl:template match="message">
  <xsl:copy>
    <xsl:apply-templates>
	    <xsl:sort data-type="number" select="type"/>
    </xsl:apply-templates>
  </xsl:copy>
</xsl:template>

<xsl:strip-space elements="*"/>
</xsl:stylesheet> 
