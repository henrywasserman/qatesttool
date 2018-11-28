<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 
<xsl:output indent="yes" />
<xsl:output method="xml"/>


	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="indexedUniquePatIdsCount">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>
	
	<xsl:template match="dbUniquePatIdsCount">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="aboutToBeIndexedUniquePatIdsCount">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="lastUpdated">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="updateSyncLastRun">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="sslPort">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="currentDate">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>
    
   	<xsl:template match="@*|id">
	    <xsl:copy>
		    <xsl:apply-templates select="id"/>	
	    </xsl:copy>
    </xsl:template>
   
   	<xsl:template match="@*|clientName">
	    <xsl:copy>
		    <xsl:apply-templates select="clientName"/>	
	    </xsl:copy>
    </xsl:template>

   	<xsl:template match="@*|schedSyncLastRun">
	    <xsl:copy>
		    <xsl:apply-templates select="schedSyncLastRun"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:strip-space elements="*"/>
</xsl:stylesheet>