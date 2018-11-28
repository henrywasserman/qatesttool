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
	
	<xsl:template match="@*|last_updated">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|last_visit_date">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>


 	<xsl:template match="root">
 		<xsl:copy>
    		<xsl:apply-templates>
				<xsl:sort select="authorityId"/>
				<xsl:sort select="lastname" />
				<xsl:sort select="name()" />
    		</xsl:apply-templates>
    	</xsl:copy>
	</xsl:template>

 	<xsl:template match="prev_visit_facility">
 		<xsl:copy>
    		<xsl:apply-templates>
    			<xsl:sort select="name()" />
    		</xsl:apply-templates>
    	</xsl:copy>
	</xsl:template>

 	<xsl:template match="prev_visit_organization">
 		<xsl:copy>
    		<xsl:apply-templates>
    			<xsl:sort select="name()" />
    		</xsl:apply-templates>
    	</xsl:copy>
	</xsl:template>

 	<xsl:template match="identifiers">
 		<xsl:copy>
    		<xsl:apply-templates>
    			<xsl:sort select="name()" />
    		</xsl:apply-templates>
    	</xsl:copy>
	</xsl:template>

 	<xsl:template match="contact_address">
 		<xsl:copy>
    		<xsl:apply-templates>
    			<xsl:sort select="name()" />
    		</xsl:apply-templates>
    	</xsl:copy>
	</xsl:template>

	<xsl:strip-space elements="*" />
</xsl:stylesheet>