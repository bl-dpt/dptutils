/*
 * Copyright 2014 The British Library/SCAPE Project Consortium
 * Author: Alecs Geuder (Alecs.Geuder@bl.uk)
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

import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class InvertedDict extends LinkedHashMap<String, Integer> {

	public void update(String s) {
		this.put(s, (this.get(s) == null) ? 1 : this.get(s) + 1);
	}

}
