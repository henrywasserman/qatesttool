package com.pqi.responsecompare.edi.mp.enums;

import java.util.HashMap;

public enum Maps {

    Instance;

    public static final HashMap<String, String> raceOrEthnicity;
    static
    {
        raceOrEthnicity = new HashMap<String, String>();
        raceOrEthnicity.put("C", "White");
        raceOrEthnicity.put("7", "Unknown");
        raceOrEthnicity.put("N","Black");
        raceOrEthnicity.put("H","Hispanic");
        raceOrEthnicity.put("A","Asian");
        raceOrEthnicity.put("I", "Indian");
    }

    public static final HashMap<String,String> confidentialityCode;
    static
    {
        confidentialityCode = new HashMap<String,String>();
        confidentialityCode.put("null","good cause");
    }

    public static final HashMap<String, String> maintenanceTypeCode;
    static
    {
        //024 - 021 -
        maintenanceTypeCode = new HashMap<String, String>();
        maintenanceTypeCode.put("021", "ADD");
        maintenanceTypeCode.put("001", "CHANGE");
        maintenanceTypeCode.put("002","DELETE");
        maintenanceTypeCode.put("030","MODIFY");
        maintenanceTypeCode.put("024","TERMINATE");
    }

    public static final HashMap<String,String> entityTypeQualifier;
    static
    {
        entityTypeQualifier = new HashMap<String,String>();
        entityTypeQualifier.put("1","Person");
    }

    public static final HashMap<String,String> dateTimeQualifier;
    static
    {
        dateTimeQualifier = new HashMap<String,String>();
        dateTimeQualifier.put("007","");
        dateTimeQualifier.put("348","");
        dateTimeQualifier.put("356","");
        dateTimeQualifier.put("357","");
    }


    public static final HashMap<String,String> entityIdentificationCode;
    static
    {
        entityIdentificationCode = new HashMap<String,String>();
        entityIdentificationCode.put("70","IL");
    }


    public static HashMap<String,String> ediResults;
    static
    {
        ediResults = new HashMap<String,String>();
        ediResults.put("x_mdm_member_id","null");
        ediResults.put("x_mass_health_id","null");
        ediResults.put("x_entity_identification_code","null");
        ediResults.put("x_entity_type_qualifier","null");
        ediResults.put("x_last_name","null");
        ediResults.put("x_first_name","null");
        ediResults.put("x_middle_name","null");
        ediResults.put("x_ssn","null");
        ediResults.put("x_dob","null");
        ediResults.put("x_gender_code","null");
        ediResults.put("x_race_or_ethnicity_code","null");
        ediResults.put("x_primary_language_spoken","English");
        ediResults.put("x_primary_language_written","English");
        ediResults.put("x_response_code","null");
        ediResults.put("x_individual_relationship_code","null");
        ediResults.put("x_maintenance_type_code","null");
        ediResults.put("x_maintenance_reason_code","null");
        ediResults.put("x_benefit_status_code","null");
        ediResults.put("x_employment_status_code","null");
        ediResults.put("x_handicap_indicator","null");
        ediResults.put("x_insured_individual_death_date","null");
        ediResults.put("x_confidentiality_code","good cause");
        ediResults.put("x_member_reference_code","null");
        ediResults.put("x_plan_start_date","null");
        ediResults.put("x_plan_end_date","2299-12-31 00:00:00.0");
        ediResults.put("x_enroll_date","null");
        ediResults.put("x_responsible_person_lastname","null");
        ediResults.put("x_responsible_person_firstname","null");
        ediResults.put(        "x_responsible_person_middle_initial","null");
        ediResults.put("x_member_prior_details_x_talend_id","null");
        ediResults.put("x_file_create_date","null");
        ediResults.put("x_enrolled_by","null");
        ediResults.put("x_hic_number","null");
        ediResults.put("x_rating_category","null");
        ediResults.put("x_filename","null");
        ediResults.put("x_fileseq_num","null");
        ediResults.put("x_talend_timestamp","null");
        ediResults.put("x_talend_task_id","null");
        ediResults.put("x_stg_seq_num","null");
    }
}