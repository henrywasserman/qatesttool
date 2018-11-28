#!/usr/bin/perl
#Script to go from dbtrace to sql

use FindBin '$Bin';

`unix2dos $Bin$ARGV[0]`;

open (DB_TRACE, "<$Bin$ARGV[0]") or die "Could not open $Bin$ARGV[0]\n";
@trace = <DB_TRACE>;
close DB_TRACE;

$found_sql = 0;
$found_prepare = 0;
$found_bind = 0;
$bind_name = "";
for(@trace) {
  if (/\([\d|\w]+\)\:\s+PREPARE WITH BIND VARIABLES\:\r$/i) {
    #print "Found PREPARE WITH BIND VARIABLES\n";
	$found_prepare = 1;
	next;
  }
	
  if (/\([\d|\w]+\)\:\s+PREPARE\:\r$/i) {
    #print "Found PREPARE\n";
    $found_prepare = 1;
	next;
  }
    
  if ($found_prepare eq 1 and $found_sql eq 0) {	
	#print "Inside select\n";
    $sql = $_;
	#print $sql;
    $found_sql = 1;
	next;
  }
  
  if ( /ID\=\:/ and $found_sql eq 1) {
    $found_bind = 1;
    #print "Inside /ID\=/ and \$found_sql eq 1\n";
	#print $_;
    ($ID) = $_ =~ /.*\*(.*)\*/;
	($bind_name) = $_ =~ /.*ID\=\:([\d|\w]+)\s+.*/;
    #print "bind variable = $ID\n";
	#print "bind name = $bind_name\n";
	#print "Before: $sql";
    $sql =~ s/\:$bind_name/\'$ID\'/;
	#print "After: $sql";
 
  } else {
    #print ".";	
    $found_sql = 0;
    $bind_name = "";
	$found_bind = 0;
  }
 
  if (length $sql) {
    #print "SQL was not empty\n";
	#print $sql;
	if ($sql =~ /^\(/) {
		($sql) = $sql =~ /\([\d|\w]+\)\:\s+(.*)/;
	}	
	#print "Removed prefix\n";
	#print $sql;
    if ($sql =~ /DBI_PREPARE/) {
	  #For example: DBI_PREPARE_FOR_INPUT_PARMS
	  ($sql) = $sql =~ /(.*)\(DBI_.*PREPARE.*\)/;
	}
	#print "Removed DBI_PREPARE STUFF\n";
	#print $sql;

    if ($found_bind eq 0) {
	
	  #print "SQL did not contain a binding variable =: or like :\n";
	  #print "Before removing carriage return and whitespace at the end\n";
	  #print $sql;
	  $sql =~ s/\r//g;
	  $sql =~ s/\s+$//;
	  #print "After removing carriage return and whitespace at the end\n";
      print "$sql;\n";
	  #print "done\n";
	  $sql = "";
	  $found_prepare = 0;
    } 
  }	
}