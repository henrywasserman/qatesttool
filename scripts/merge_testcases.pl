#!/usr/bin/perl

open GEN,"<generated.req" or die "Could not open generated.req\n";
@gen = <GEN>;
close GEN;

open ALL,"<../data/consult/api_tests.req" or die "Could not open api_tests.req\n";
@all = <ALL>;
close ALL;

$found_testcase=0;
for (@all) {
  if ($_ =~ /TESTCASE/) {
    $found_testcase=1;    
    ($testcasename) = $_ =~ /.*TESTCASE\s+([\w|\-]+).*/;
  }
  if ($found_testcase) {
    if ($_ =~ /^\s+$/ && @testcase) {
      $testcase_hash{$testcasename} = [ @testcase ];
      $found_testcase=0; 
      @testcase= ();
    } else {
      push @testcase,$_;
    }
  }
}

open FH, ">../reports/merged_report.html" or die "Could not open merged_report.html\n";
print FH "<html><body><pre>\n";

for(@gen) {
  ($gen_testcase_name) = $_ =~ /.*TESTCASE\s+([\w|\-]+).*/;
  if (exists $testcase_hash{$gen_testcase_name}) {
    print FH "\n";
    for (@{$testcase_hash{$gen_testcase_name}}) {
      #print $_;
      print FH $_;
    }
    delete $testcase_hash{$gen_testcase_name};
    print FH "\n";
  } else {
    #print $_;
    print FH $_;
  }
}
print "#Printing all testcases that are not in miredot\n";
foreach my $name (sort keys %testcase_hash) { 
  for (@{$testcase_hash{$name}}) {
    #print $_;
    print FH $_;
  }
}
print FH "</pre></body></html>";

close FH;
