package com.pqi.responsecompare.ICD9;

import com.pqi.responsecompare.configuration.SSHTunnel;
import com.pqi.responsecompare.request.Request;
import com.pqi.responsecompare.request.TestCase;
import com.pqi.responsecompare.sql.OracleDbManager;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VerifyICD extends Request
{
	static final Logger logger = Logger.getLogger(VerifyICD.class);
	Properties agnosticProps = null;
	OracleDbManager db = null;

	public VerifyICD(TestCase test) throws Exception
	{
		super(test);
	}

	public void sendRequest() throws Exception
	{
		String fs = File.separator;
		logger.info("TestID: " + test.getTestCaseID());

		try
		{
			ResultSet resultSet = null;
			String icdFileString = test.getRequests().get(test_request_counter).getICDFile();
			File icdFile = new File(icdFileString);

			SSHTunnel.Instance.openTunnel();

			List<String> icdlist = FileUtils.readLines(icdFile);

			ExecutorService executor = Executors.newFixedThreadPool(50);

			int totalsize = icdlist.size();
			int start = 0;
			int stop = 0;
			int counter = 0;
			int chunks = 300;
			int endloop = totalsize/chunks;

			List <ICDThread> icdThreads = new ArrayList<ICDThread>();
			for (counter = 0; counter < endloop; counter++) {
				start = counter * chunks;
				stop = start + chunks - 1;
				icdThreads.add(new ICDThread(icdlist.subList(start,stop)));
				executor.submit(icdThreads.get(counter));
				start = stop;
			}

			int extra = totalsize % chunks;
			if (extra > 0 )
			{
				icdThreads.add(new ICDThread(icdlist.subList(start,stop + extra)));
				executor.submit(icdThreads.get(counter));
			}

			executor.shutdown();
			executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

			int newCounter = 0;
			for (ICDThread thread:icdThreads) {
				start = newCounter * chunks;
				stop = start + chunks - 1;
				logger.debug("Thread " + (newCounter + 1) + " start: " + (start + 1) + " stop: " + (stop + 1) + ' ' + thread.getErrors());
				start = stop;
				newCounter++;
				if (newCounter == counter) {
					extra = totalsize % chunks;
					logger.debug("Thread " + (newCounter + 1) + " start: " + (start + 2) + " stop: " + (stop + extra + 1) + ' ' + thread.getErrors());
					break;
				}
			}
		}
		finally
		{
			//db.closeStatement();
			//db.closeConnection();
			SSHTunnel.Instance.closeTunnel();
		}
	}
	private void runQuery(String query, ResultSet rs) throws Exception
	{
		db.executeQuery(query);
	}
}
