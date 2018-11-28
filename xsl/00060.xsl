<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 
<xsl:output indent="yes" />
<xsl:output method="xml"/>

    <!--  this template copies all, and needs to be used for every xsl file -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
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
				<xsl:sort select="id" data-type="text"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>

	<xsl:strip-space elements="*"/>
</xsl:stylesheet>