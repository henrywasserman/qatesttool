<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output indent="yes" />

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*|network_addresses">
	    <xsl:copy>
		    <xsl:apply-templates select="network_addresses"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="release_version">
		<xsl:copy>
			<xsl:apply-templates select="@*" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*|build_version">
	    <xsl:copy>
		    <xsl:apply-templates select="build_version"/>	
	    </xsl:copy>
    </xsl:template>
    
	<xsl:template match="@*|data_model_version">
	    <xsl:copy>
		    <xsl:apply-templates select="data_model_version"/>	
	    </xsl:copy>
    </xsl:template>
    
	<xsl:template match="@*|build_date">
	    <xsl:copy>
		    <xsl:apply-templates select="build_date"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:strip-space elements="*" />
</xsl:stylesheet>