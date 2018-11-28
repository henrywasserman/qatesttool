<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 
<xsl:output indent="yes" />
<xsl:output method="xml"/>


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
	
	<xsl:template match="@*|selectedDate">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|createdTime">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|earliestColumnDate">
	    <xsl:copy>
		    <xsl:apply-templates select="earliestColumnDate"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|versionId">
	    <xsl:copy>
		    <xsl:apply-templates select="versionId"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|latestColumnDate">
	    <xsl:copy>
		    <xsl:apply-templates select="latestColumnDate"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|recentlySelectedFacilities">
	    <xsl:copy>
		    <xsl:apply-templates select="recentlySelectedFacilities"/>	
	    </xsl:copy>
    </xsl:template>
	
	<xsl:template match="@*|medical_record_number">
	    <xsl:copy>
		    <xsl:apply-templates select="medical_record_number"/>	
	    </xsl:copy>
    </xsl:template>
        
 	<xsl:template match="folders">
  		<xsl:copy>
    		<xsl:apply-templates>
	    		<xsl:sort select="folders" />
    		</xsl:apply-templates>
  		</xsl:copy>
	</xsl:template>
 
	<xsl:template match="folders">
  		<xsl:copy>
    		<xsl:apply-templates>
	    		<xsl:sort select="id" data-type="number" />
    		</xsl:apply-templates>
  		</xsl:copy>
	</xsl:template>
	
	<xsl:strip-space elements="*"/>
</xsl:stylesheet>