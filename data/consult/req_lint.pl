#!/usr/bin/perl
open FH, "<", "api_tests.req" or die "Could not open api_tests.req\n";
@api_tests = <FH>;
close FH;

for (@api_tests) {
  $line = $_;
  if (/TESTCASE/) {
    $line =~ s/^\s+|\s+$//g;
    print "$line\n";
  } else { 
    $line =~ s/^\s+|\s+$//g;
    print "  $line\n";
  }
}
