<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes" />
	<xsl:output method="xml" />

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*|authToken">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>

 	<xsl:template match="root">
 		<xsl:copy>
    		<xsl:apply-templates>
    			<xsl:sort select="name()" />
    			<xsl:sort select="primaryId" />
    		</xsl:apply-templates>
    	</xsl:copy>
	</xsl:template>

	<xsl:strip-space elements="*" />
</xsl:stylesheet>