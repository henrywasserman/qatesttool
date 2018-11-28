#!/usr/bin/perl
$input_js = $ARGV[0];
open INPUT,"<$input_js or die "Could not open $input_js\n";
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
   ($​_) = $_​ =~ /\t+\"http\"\:\"(\w+)/;
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
print @fixedlist;
