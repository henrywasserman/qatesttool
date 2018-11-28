#!/usr/bin/perl

if (not scalar @ARGV >= 1) {
  print "Usage: [har filename] optional: [no-variables: 1 means none 0 means add variables] [description]\n";
  exit 1;
}

$filename = $ARGV[0];
$novariables = $ARGV[1];
$description = $ARGV[3];

$found_post_data = 0;

$idx = rindex($filename, "/"); 
$testcasename = substr($filename, $idx+1);
($testcasename) = $testcasename =~ /(.*)\.har/;

$testcasename = "testcase" if ($testcasename eq '');
$description = $testcasename if ($description eq '');
$novariables = 0 if ($novariables eq '');

open FH, "<$filename" or die "Could not open $filename\n";
@har = <FH>;
close FH;

for(@har) {
  if (/url/){
    ($protocol,$host) = /\"url\"\:\s+\"(\w+)\:\/\/([\w|\.]+)\//;
    last;
  }
}

#print "Here is \$protocol: $protocol\n";
#print "Here is \$host: $host\n";

$last_and_next = 0;
$recent_events_post = 0;
print "\nTESTCASE $testcasename, $description\n";
for (@har) {
	if (/\"method\"\:/) {
		($method) =  /\s+\"method\"\:\s+\"([A-Z]+)\"/;
	}
	if (/\s+\"url\"\:/ and not /\.png/ and not /\.svg/ and not /\.js$/ and not /\.jpg$/) {
		if (/${host}/) {
			($url) = /\s+\"url\"\:\s+\"${protocol}\:\/\/${host}(.*)\"/,"\n";
			$call = "  " . $method . " "  . $url;
		} else {
			($url) = /\s+\"url\"\:\s+\"(.*)\"/,"\n";
			$call = "  " . $method . " " . $url;
		}
		#print "Here is call: $call\n";

		if ($call =~ /lastAndNext/) {
			next if ($last_and_next == 1);
			$last_and_next = 1;
		}

		if ($call =~ /POST.*recent_events\/patient_search/) {
			#$recent_events_post = 1;
		}
			
		push @testcases, $call; 

	}

	if (/\s+\"postData\"\:/) {
		$found_post_data = 1;
	}

	if ($found_post_data eq 1 and /\s+\"text\"\:/) {
		#example: "text": "{\"mpi\":\"328405\"}"
		($body) = /\s+\"text\"\:\s+\"(\{.*\})/;
		($body) =~ s/\\\"/\"/g;
		if ($recent_events_post == 1) {
			$body = '{"orgId":"${orgId}"}';
			$recent_events_post = 0;
 		}
		if ($call =~ /flowsheet\/columns\/data/) {
			push @testcases, "    BODY ","    \$\{body\}" 
		} else {
			push @testcases, "    BODY ","    $body" if ($body ne '{}');
		}
		$found_post_data = 0;
	}
}

$get = 0;
for (@testcases) {
	@matches = $_ =~ /[\=|\/|\:\"]([\w\-\d]+)/g;
	$testcase = $_;
	for (@matches) {
                next if (length($_) <=2);
		$testcase = find_variable($_,$testcase) if ($novariables eq 0);
	}
	print "$testcase\n";
	print "    ASSIGN status, Active\n" if ($testcase =~ /login/);
	print "    ASSIGN var body = \'{\"versionId\":\"\$\{versionId\}\",\"loggedInOrgId\":\"\$\{orgId\}\",\"columnIds\":[';for(var i = 0; i < json_object.length; i++) {body = body.concat('\"',json_object[i].columnId,'\"');if (i < json_object.length -1) {body = body.concat(',');}} body = body.concat(']}');, body\n" if ($testcase =~ /keyList/);
	@matches = ();
}

sub find_variable  {
	my ($string,$testcase) = @_;
        my $last = 0;
        $counter = 0;
	for (@har) {
		if (/${string}/) {
	  	  if (/.*\"(.*)\\\"\:\\\"${string}/ ne '') {
		    ($variable) = /.*\"(.*)\\\"\:\\\"${string}/;
		    $testcase =~ s/$string/\$\{$variable\}/;
		    last;
		  }
		}
	}
	return $testcase;
}
