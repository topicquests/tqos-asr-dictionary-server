/**
 * 
 */
package org.topicquests.asr.dictionary;

import java.net.Socket;
import java.util.Map;

import org.topicquests.support.config.ConfigPullParser;

/**
 * @author jackpark
 *
 */
public class Stopper {
	private static final String serverName = "localhost";
	private static int port;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Stopping Dictionary Server");
		ConfigPullParser p = new ConfigPullParser("config-props.xml");
		Map<String, Object> props = p.getProperties();
		String px = (String)props.get("StopperPort");
		port = Integer.parseInt(px);
		try {
			Socket s = new Socket(serverName, port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
