# qatesttool
Web API, SQL Query, EDI Parsing Test Tool

To Install Oracle Driver
mvn install:install-file -Dfile=ojdbc7.jar -DgroupId=com.oracle -DartifactId=ojdbc7 -Dversion=12.1.0.2 -Dpackaging=jar

To Install Teradata Drivers
mvn install:install-file -Dfile=terajdbc4.jar -DgroupId=com.teradata.jdbc -DartifactId=terajdbc4 -Dversion=16.20.00.08 -Dpackaging=jar
mvn install:install-file -Dfile=tdgssconfig.jar -DgroupId=com.teradata.jdbc -DartifactId=tdgssconfig -Dversion=16.20.00.08 -Dpackaging=jar
