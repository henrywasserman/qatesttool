package com.pqi.responsecompare.tail;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.SSHTunnel;
import org.apache.log4j.Logger;

import java.util.Properties;

public enum TailManager
{
	Instance;

	static final Logger logger = Logger.getLogger(TailManager.class);

	private Properties props = PropertiesSingleton.Instance.getProps();

	private String sshUser = PropertiesSingleton.Instance.getProperty("ssh.user");
	private String consultHost = PropertiesSingleton.Instance.getProperty("consult-host");
	private String core1StartLineCount = "";
	private String core1EndLineCount = "";
	private String core2StartLineCount = "";
	private String core2EndLineCount = "";
	

	public void setStartLogLineCount() throws Exception {

		core1StartLineCount = "";
		core2StartLineCount = "";
		if (PropertiesSingleton.Instance.getProperty("ssh.tunnel.enabled").equals("true")) {
			SSHTunnel.Instance.openTunnel();
			core1StartLineCount = SSHTunnel.Instance.sendCommand("ssh " + sshUser + "@core1." + consultHost + " wc -l /usr/share/tomcat/logs/phoenixServices.log | cut -d' ' -f1").trim();
			SSHTunnel.Instance.closeTunnel();
			SSHTunnel.Instance.openTunnel();
			core2StartLineCount = SSHTunnel.Instance.sendCommand("ssh " + sshUser + "@core2." + consultHost + " wc -l /usr/share/tomcat/logs/phoenixServices.log | cut -d' ' -f1").trim();
			SSHTunnel.Instance.closeTunnel();

		}
	}

	public String getTail() throws Exception {

		String log = "";

		if (PropertiesSingleton.Instance.getProperty("ssh.tunnel.enabled").equals("true")) {
			SSHTunnel.Instance.openTunnel();
			core1EndLineCount = SSHTunnel.Instance.sendCommand("ssh " + sshUser + "@core1." + consultHost + " wc -l /usr/share/tomcat/logs/phoenixServices.log | cut -d' ' -f1").trim();
			SSHTunnel.Instance.closeTunnel();
			SSHTunnel.Instance.openTunnel();
			log = SSHTunnel.Instance.sendCommand("ssh " + sshUser + "@core1." + consultHost + " tail -n "
				+ Integer.toString(Integer.valueOf(core1EndLineCount) - Integer.valueOf(core1StartLineCount)) + " /usr/share/tomcat/logs/phoenixServices.log");
			SSHTunnel.Instance.closeTunnel();
			SSHTunnel.Instance.openTunnel();
			core2EndLineCount = SSHTunnel.Instance.sendCommand("ssh " + sshUser + "@core2." + consultHost + " wc -l /usr/share/tomcat/logs/phoenixServices.log | cut -d' ' -f1").trim();
			SSHTunnel.Instance.closeTunnel();
			SSHTunnel.Instance.openTunnel();
			log = log + SSHTunnel.Instance.sendCommand("ssh " + sshUser + "@core2." + consultHost + " tail -n "
				+ Integer.toString(Integer.valueOf(core2EndLineCount) - Integer.valueOf(core2StartLineCount)) + " /usr/share/tomcat/logs/phoenixServices.log");
			SSHTunnel.Instance.closeTunnel();
		}
		return log;
	}
}