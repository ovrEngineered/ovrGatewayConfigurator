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

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.fazecast.jSerialComm.SerialPort;

/**
 * @author Christopher Armenio
 *
 */
public class SerialPortRenderer extends JLabel implements ListCellRenderer<SerialPort>
{

	@Override
	public Component getListCellRendererComponent(JList<? extends SerialPort> list, SerialPort value, int index, boolean isSelected, boolean cellHasFocus)
	{
		if( value == null ) return this;
		
		this.setText(value.getSystemPortName());
		
		if (isSelected)
		{
			this.setBackground(list.getSelectionBackground());
			this.setForeground(list.getSelectionForeground());
		} 
		else
		{
			this.setBackground(list.getBackground());
			this.setForeground(list.getForeground());
		}
		
		return this;
	}
}
