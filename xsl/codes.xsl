<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 
<xsl:output indent="yes" />
<xsl:output method="xml"/>

<xsl:template match="@* | node()">
	<xsl:copy>
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates select="node()">
			<xsl:sort select="name()"/>
		</xsl:apply-templates>
	</xsl:copy>
</xsl:template>
</xsl:stylesheet>