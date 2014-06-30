/*
 * Copyright 2014 The British Library/SCAPE Project Consortium
 * Author: William Palmer (William.Palmer@bl.uk)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package uk.bl.dpt.utils.util;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.log4j.Logger;

/**
 * Utility methods for Sockets
 * @author wpalmer
 *
 */
public class SocketUtil {
	
	@SuppressWarnings("unused")
	private static Logger gLogger = Logger.getLogger(SocketUtil.class);
	
	private SocketUtil() {}
	
	/**
	 * Find a free port, starting from pport
	 * @param pPort starting port
	 * @return port not in use
	 */
	public static int getFreePort(int pPort) {
		int port = 0;
		ServerSocket s = null;
		boolean found = false;
		while(!found) {
			try {
				s = new ServerSocket(pPort);
				if(s.isBound()) {
					port = s.getLocalPort();
					found = true;
				}
			} catch (IOException e) {
				pPort++;
			} finally {
				if(s!=null) {
					try {
						s.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return port;
	}
	
	
}
