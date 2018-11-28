package com.pqi.responsecompare.configuration;

import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

public enum SSHTunnel {
	Instance;

	private String host = "";
	private String user = "";
	private String filepath = "";
	private String knownhosts = "";
	
	private int sshport = 22;
	private int lport = 0;
	private int rport = 0;
	private String rhost = "";
	
	private Properties props = null;
	private Session session = null;

	Logger logger = Logger.getLogger(SSHTunnel.class);

	SSHTunnel() {
		props = PropertiesSingleton.Instance.getProps();
		user=props.getProperty("ssh.user");
	    host=props.getProperty("ssh.host");
	    sshport=Integer.valueOf(props.getProperty("ssh.port"));
	    lport=Integer.valueOf(props.getProperty("ssh.lport"));
	    filepath=props.getProperty("ssh.key");
	    rport=Integer.valueOf(props.getProperty("ssh.rport"));
	    rhost=props.getProperty("ssh.rhost");
	    knownhosts=props.getProperty("ssh.knownhosts");
	}

	public void openTunnel() throws JSchException {
		try {
			JSch jsch = new JSch();
			//logger.debug("filepath: " + filepath);
			//logger.debug("knownhosts: " + knownhosts);
			//logger.debug("user: " + user);
			//logger.debug("host: " + host);
			//logger.debug("sshport: " + sshport);
			//logger.debug("lport: " + lport);
			//logger.debug("rhost: " + rhost);
			//logger.debug("rport: " + rport);
			//logger.debug("Calling add Identity with filepath: " + filepath);
			jsch.addIdentity(filepath);
			//logger.debug("Calling set KnownHosts with: " + knownhosts);
			jsch.setKnownHosts(knownhosts);
			//logger.debug("Calling get Session");
			session=jsch.getSession(user,host,sshport);
			if (session != null) {
				//logger.debug("Here is session: " + session.toString());
			} else {
				logger.debug("session is null");
				throw new JSchException("Session is null");
			}
			//logger.debug("Calling session.connect");
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			//logger.debug("Finished calling session connect");
			session.setPortForwardingL(	lport, rhost, rport);
			if (session.isConnected()) {
				//logger.debug("Session is connected");
			}
			else {
				throw new JSchException("Could not connect with: filepath: "
						+ filepath + " knownhosts: " + knownhosts + " user: " + user
						+ " host: " + host + " sshport: " + sshport + " lport: " + lport
						+ " rhost: " + rhost + " rport: " + rport );
			}
		} catch (JSchException e) {
			e.printStackTrace();
			logger.debug("Session Exception: " + e.getLocalizedMessage());
			throw e;
		}
	}

	public String sendCommand(String command) throws Exception  {
		Channel channel = session.openChannel("exec");
		String output = "";
		try
		{
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();
			InputStream error = ((ChannelExec) channel).getErrStream();

			channel.connect();

			output = IOUtils.toString(in);

			//Don't think I need this here - removing for now
			/*
			if (output.isEmpty()) {

			Output = IOUtils.toString(error);
				throw new Exception(output);
			}
			*/

			//logger.debug(output);

			return output;
		}

		catch(Exception e) {
			throw e;
		}

		finally {
			channel.disconnect();
			session.disconnect();
		}
	}



	public void closeTunnel() {
		if (session.isConnected()) {
			session.disconnect();
		}
	}
}
