#!/usr/bin/perl
#example response:
#{"app_name":"Phoenix API Web Application","release_version":"origin/develop - afe9d85164517e609cd9f538cf787b91cf602212","api_version":"1.0","build_version":"2249","build_date":"2016-10-24_14-18-49","data_model_version":"201610231011","active_profiles":"hazelcast,oracle","database_info":{"database_url":"jdbc:oracle:thin:@welld3.db.wellogic.com:1521:welld3","database_user_name":"LOTUS","product_name":"Oracle","driver_name":"Oracle JDBC driver"},"network_addresses":"10.8.0.126 (ip-10-8-0-126.ec2.internal), 172.24.3.175 (core2.lotus.lumiradx.com)","application_env_name":"Duffman"}
open GH,"</var/lib/jenkins/git_hash.txt" or die "Could not open /var/lib/jenkins/git_hash.txt\n";
$git_hash = <GH>;
close GH;
chomp $git_hash;


open LOG, ">/var/lib/jenkins/lotus.log" or die "Could not open /var/lib/jenkins/lotus.log\n";
$response = `curl -H username:tester1 -H password:well1024! -k https://lotus.lumiradx.com/system/buildInfo`;
($new_git_hash) = $response =~ /.*origin\/develop\s+\-\s+([\w]+).*/;
if ($new_git_hash eq '') {
  print LOG "New git hash was blank\n";
  exit;
}
if ($git_hash ne $new_git_hash) {
  open GH,">/var/lib/jenkins/git_hash.txt" or die "Could not open /var/lib/jenkins/git_hash.txt\n";
  print LOG "printing $new_git_hash to git_hash.txt\n";
  print GH $new_git_hash;
  close GH;
  print LOG "$new_git_hash is different from $git_hash so I'm starting the Lotus Smoke Test in 10 minutes.\n";
  sleep 600;
  `curl -H username:henry.wasserman@wellogic.com -H password:jesusislord http://performance.ec201.wellogic.com/view/API-Test-Tool/job/Lotus_Smoke_Test/build?token=LotusAuthenticationToken`;
}
print LOG "$git_hash and $new_git_hash were the same\n";
close LOG;
