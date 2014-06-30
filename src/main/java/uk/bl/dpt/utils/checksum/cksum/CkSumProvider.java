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

package uk.bl.dpt.utils.checksum.cksum;

import java.security.Provider;
import java.security.Security;

/**
 * Security provider for cksum checksum
 * 
 * A static convenience method for registering this provider is included
 * 
 * @author wpalmer
 *
 */
public class CkSumProvider extends Provider {
	
	private static final long serialVersionUID = 8346620205051247027L;
	
	private static boolean gRegistered = false;
	
	protected CkSumProvider(String name, double version, String info) {
		super(name, version, info);
		super.put("MessageDigest.cksum", CkSumDigest.class.getCanonicalName());
	}
	
	/**
	 * Static call to register the provider (and ensure this happends only once)
	 */
	public static void register() {
		if(gRegistered) return;
		Security.addProvider(new CkSumProvider("cksum provider", 0.1, "provider for cksum compatible messagedigest only"));
		gRegistered = true;
	}
	
}