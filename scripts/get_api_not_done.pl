#!/usr/bin/perl
$input_js = $ARGV[0];

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
for (@list) {
 ($url) = $_ =~ /([\/|\w|\{|\}]+)/;
 ($method) = $_ =~ /[\/|\w|\{|\}]+\s(\w+)/;
 push(@fixedlist,"$method $url\n");
}

open FH,">../reports/all_api_s.html" or die "Could not open all_api_s.html\n";

print FH "<html>\n";
print FH "  <body>\n";
print FH "    <pre>\n";
print FH @fixedlist;

print FH "    </pre>\n";
print FH "  </body>\n";
print FH "</html>\n";
close FH;
