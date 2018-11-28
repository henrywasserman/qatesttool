package com.pqi.responsecompare.reports;

import java.io.File;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import org.apache.maven.surefire.shade.org.apache.maven.shared.utils.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.XSLTProcess;
import org.apache.tools.ant.taskdefs.optional.junit.AggregateTransformer;
import org.apache.tools.ant.taskdefs.optional.junit.AggregateTransformer.Format;
import org.apache.tools.ant.taskdefs.optional.junit.XMLResultAggregator;
import org.apache.tools.ant.types.FileSet;

public enum JunitReport {
	Instance;
	
	private Project project = null;
	private Target target = null;
	private AggregateTransformer transformer = null;
	private XMLResultAggregator aggregator = null;
	private Format format = null;
	
	public void createReports() throws Exception {
		project = new Project();
		project.setName("myproject");
		project.init();

		target = new Target();
		format = new Format();
		aggregator = new XMLResultAggregator();
		
		target.setName("junitreport");
		project.addTarget(target);

		FileSet fs = new FileSet();
		fs.setDir(new File("./reports"));
		fs.createInclude().setName("*.xml");
		
		aggregator.setProject(project);
		aggregator.addFileSet(fs);
		transformer = aggregator.createReport();
		transformer.setStyledir(new File("./reports/xsl"));
		transformer.setTodir(new File("./junitreports"));
		format.setValue(AggregateTransformer.NOFRAMES);
		transformer.setFormat(format);

		target.addTask(aggregator);
        XSLTProcess.Param build_url = transformer.createParam();
        build_url.setName("build_url");
        if (System.getProperty("build_url") != null) {
            build_url.setExpression(System.getProperty("build_url"));
        }
        else {
            build_url.setExpression("../");
        }
		project.executeTarget("junitreport");
        transformer.setStyledir(new File("./reports/xsl/email"));
        transformer.setTodir(new File("./junitreports/email"));
        project.executeTarget("junitreport");
	}

}