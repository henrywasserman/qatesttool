open FH, "<", "C:/Users/hwasserman/CCA_QA/trunk/sqltesttool/data/consult/mp/edi_files/CCA_AH_U_FULL_834_08212018111731.834" or die "Could not open test.txt\n";

$string = <FH>;
@line=split("~INS",$string);
for (@line) {
    if (/.*5365604699.*/ ||
	    /.*5365555421.*/ ||
		/.*5365558028.*/ ||
		/.*5365558330.*/ ||
		/.*5365558664.*/ ||
		/.*5364524906.*/ ||
		/.*5364521225.*/ ||
		/.*5364523007.*/ ||
		/.*5365619549.*/)
	{
		print "~INS$_\n\n";
	}
}
close FH or die "Could not close FH";