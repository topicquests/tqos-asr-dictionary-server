/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
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
