package com.ovrengineered.ovrGatewayConfigurator;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

public class OvrSettingsProvider extends DefaultSettingsProvider
{

	@Override
	public TextStyle getDefaultStyle()
	{
		return new TextStyle(TerminalColor.WHITE, TerminalColor.BLACK);
	}
	
}
