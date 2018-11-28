#!/usr/bin/perl

`curl https://zelda.consult.lumira.com/api/input/input.js -o ../api/input/input.js`;

open INPUT,"<../api/input/input.js" or die "Could not open input.js\n";
@input = <INPUT>;
close INPUT;

@list = ();
$temp_line = "";
for (@input) {
  if (/^\s+\"url\"/) {
    $temp_line = $_;
    chomp $temp_line;
    ($temp_line) = $temp_line =~ /\s+\t+\"url\"\:\"([\/|\w|\{|\}|]+).*/;
    #print "Found url and here is temp_line: $temp_line\n";
  }
  if (/\"http\"/) {
    chomp $_;
    ($_) = $_ =~ /\t+\"http\"\:\"(\w+)/;
    #print "Found http and here is \$_ $_\n";
    $temp_line = "$temp_line $_\n";
    push(@list,$temp_line);
    $temp_line = "";
  }
}

@list = sort { lc($a) cmp lc($b) } @list;

open FH,">generated.req" or die "Could not open generated.req\n";
open FH2, ">../reports/generated_req.html" or die "Could not open generated_req.html\n";
print FH2 "<html><body><pre>\n";

$counter=1;
for (@list) {
  ($url) = $_ =~ /([\/|\w|\{|\}]+)/;
  ($method) = $_ =~ /[\/|\w|\{|\}]+\s(\w+)/;
  $testcase_name = "$method$url";
  $testcase_name =~ s/\//\-/g;
  $testcase_name =~ s/\-$//;
  $testcase_name =~ s/\{|\}//g;
  print FH "# " . $counter . " TESTCASE $testcase_name, $method $url\n"; 
  print FH2 "# " . $counter . " TESTCASE $testcase_name, $method $url\n"; 
  $generated_hash{"$method-$url"}=1;
  $counter++;
}
print FH2 "\n</pre></body></html>";

close FH;
close FH2;

open FH,"<../data/consult/generated.req" or die "Cound not open ../data/consult/generated.req\n";
@generated = <FH>;
close FH;

$counter = 1;
foreach $key ( keys %generated_hash )
{
  #print "$counter key: $key, value: $generated_hash{$key}\n";
  $counter++;
}
