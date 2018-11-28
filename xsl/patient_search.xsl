<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 
<xsl:output indent="yes" />
<xsl:output method="xml"/>

	<xsl:template match="@*|authToken">
	    <xsl:copy>
		    <xsl:apply-templates select="@*"/>	
	    </xsl:copy>
    </xsl:template>
    
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*|patient_id">
	    <xsl:copy>
		    <xsl:apply-templates select="patient_id"/>	
	    </xsl:copy>
    </xsl:template>
	
	<xsl:template match="@*|last_name">
	    <xsl:copy>
		    <xsl:apply-templates select="last_name"/>	
	    </xsl:copy>
    </xsl:template>
	
	<xsl:template match="@*|middle_name">
	    <xsl:copy>
		    <xsl:apply-templates select="middle_name"/>	
	    </xsl:copy>
    </xsl:template>
	
	<xsl:template match="@*|prev_visit_facility">
	    <xsl:copy>
		    <xsl:apply-templates select="prev_visit_facility"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|key">
	    <xsl:copy>
		    <xsl:apply-templates select="key"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|identifiers">
	    <xsl:copy>
		    <xsl:apply-templates select="identifiers"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|patient_patlist_status">
	    <xsl:copy>
		    <xsl:apply-templates select="patient_patlist_status"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|demographics">
	    <xsl:copy>
		    <xsl:apply-templates select="demographics"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|next_visit_date">
	    <xsl:copy>
		    <xsl:apply-templates select="next_visit_date"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|next_visit_id">
	    <xsl:copy>
		    <xsl:apply-templates select="next_visit_id"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|next_visit_reason">
	    <xsl:copy>
		    <xsl:apply-templates select="next_visit_reason"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|next_visit_facility">
	    <xsl:copy>
		    <xsl:apply-templates select="next_visit_facility"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|next_visit_organization">
	    <xsl:copy>
		    <xsl:apply-templates select="next_visit_organization"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|abbr">
	    <xsl:copy>
		    <xsl:apply-templates select="abbr"/>	
	    </xsl:copy>
    </xsl:template>
   
	<xsl:template match="@*|name">
	    <xsl:copy>
		    <xsl:apply-templates select="name"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|prefix">
	    <xsl:copy>
		    <xsl:apply-templates select="prefix"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|suffix">
	    <xsl:copy>
		    <xsl:apply-templates select="suffix"/>	
	    </xsl:copy>
    </xsl:template>
   
	<xsl:template match="@*|locked_nonmember">
	    <xsl:copy>
		    <xsl:apply-templates select="locked_nonmember"/>	
	    </xsl:copy>
    </xsl:template>
	
	<xsl:template match="@*|opt_status">
	    <xsl:copy>
		    <xsl:apply-templates select="opt_status"/>	
	    </xsl:copy>
    </xsl:template>
	
	<xsl:template match="@*|visit_organization">
	    <xsl:copy>
		    <xsl:apply-templates select="visit_organization"/>	
	    </xsl:copy>
    </xsl:template>
    
	<xsl:template match="@*|first_name">
	    <xsl:copy>
		    <xsl:apply-templates select="first_name"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|medical_record_number">
	    <xsl:copy>
		    <xsl:apply-templates select="medical_record_number"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|home_phone">
	    <xsl:copy>
		    <xsl:apply-templates select="home_phone"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|date_of_birth">
	    <xsl:copy>
		    <xsl:apply-templates select="date_of_birth"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|gender">
	    <xsl:copy>
		    <xsl:apply-templates select="gender"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|last_visit_id">
	    <xsl:copy>
		    <xsl:apply-templates select="last_visit_id"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|last_visit_date">
	    <xsl:copy>
		    <xsl:apply-templates select="last_visit_date"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|authorityId">
	    <xsl:copy>
		    <xsl:apply-templates select="authorityId"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|last_updated">
	    <xsl:copy>
		    <xsl:apply-templates select="last_updated"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|selectedDate">
	    <xsl:copy>
		    <xsl:apply-templates select="selectedDate"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|visit_type">
	    <xsl:copy>
		    <xsl:apply-templates select="visit_type"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|visit_reason">
	    <xsl:copy>
		    <xsl:apply-templates select="visit_reason"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|mpi">
	    <xsl:copy>
		    <xsl:apply-templates select="mpi"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|id">
	    <xsl:copy>
		    <xsl:apply-templates select="id"/>	
	    </xsl:copy>
    </xsl:template>

	<xsl:template match="@*|value">
	    <xsl:copy>
		    <xsl:apply-templates select="value"/>	
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