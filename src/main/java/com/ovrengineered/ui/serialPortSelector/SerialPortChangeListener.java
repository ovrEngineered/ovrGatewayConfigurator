/**
 * Copyright 2013 opencxa.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovrengineered.ui.serialPortSelector;

import com.fazecast.jSerialComm.SerialPort;

/**
 * This is a listener that contains functions that are notified about
 * changes with the serial port selector
 * 
 * @author Christopher Armenio
 */
public interface SerialPortChangeListener
{
	/**
	 * Called when the serial selector opens a port
	 * 
	 * @param spIn the newly opened serial port
	 */
	public abstract void onOpen(SerialPort spIn);
	
	/**
	 * Called when the serial selector closes a port
	 */
	public abstract void onClose();
}
