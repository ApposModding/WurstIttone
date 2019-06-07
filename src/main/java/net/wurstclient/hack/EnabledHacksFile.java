/*
 * Copyright (C) 2014 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hack;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.google.gson.JsonArray;

import net.wurstclient.utils.JsonException;
import net.wurstclient.utils.JsonUtils;
import net.wurstclient.utils.WsonArray;

public final class EnabledHacksFile
{
	private final Path path;
	private boolean disableSaving;
	
	public EnabledHacksFile(Path path)
	{
		this.path = path;
	}
	
	public void load(HackList hackList)
	{
		try
		{
			WsonArray wson = JsonUtils.parseWsonArray(path);
			enableHacks(hackList, wson);
			
		}catch(NoSuchFileException e)
		{
			// The file doesn't exist yet. No problem, we'll create it later.
			
		}catch(IOException | JsonException e)
		{
			System.out.println("Couldn't load " + path.getFileName());
			e.printStackTrace();
		}
		
		save(hackList);
	}
	
	private void enableHacks(HackList hackList, WsonArray wson)
	{
		try
		{
			disableSaving = true;
			
			for(String name : wson.getAllStrings())
			{
				Hack hack = hackList.getHackByName(name);
				if(hack == null || !hack.isStateSaved())
					continue;
				
				hack.setEnabled(true);
			}
			
		}finally
		{
			disableSaving = false;
		}
	}
	
	public void save(HackList hax)
	{
		if(disableSaving)
			return;
		
		JsonArray json = createJson(hax);
		
		try
		{
			JsonUtils.toJson(json, path);
			
		}catch(IOException | JsonException e)
		{
			System.out.println("Couldn't save " + path.getFileName());
			e.printStackTrace();
		}
	}
	
	private JsonArray createJson(HackList hax)
	{
		Stream<Hack> enabledHax = hax.getAllHax().stream()
			.filter(Hack::isEnabled).filter(Hack::isStateSaved);
		
		JsonArray json = new JsonArray();
		enabledHax.map(Hack::getName).forEach(name -> json.add(name));
		
		return json;
	}
}
