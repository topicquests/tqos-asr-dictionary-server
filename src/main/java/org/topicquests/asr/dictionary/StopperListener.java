/**
 * 
 */
package org.topicquests.asr.dictionary;

import java.net.ServerSocket;
import java.util.Map;

import org.topicquests.support.config.ConfigPullParser;

/**
 * @author jackpark
 *
 */
public class StopperListener {
	private DictionaryServerEnvironment environment;
	private final String serverName = "localhost";
	private final int port;

	/**
	 * 
	 */
	public StopperListener(DictionaryServerEnvironment env) {
		environment = env;
		String px = environment.getStringProperty("StopperPort");
		port = Integer.parseInt(px);
		new Worker().start();
	}
	class Worker extends Thread {
		
		public void run() {
			ServerSocket skt = null;
			try {
				skt = new ServerSocket(port);
				skt.accept();
				environment.shutDown();;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
