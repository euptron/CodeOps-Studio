/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024 EUP
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/
 *
 * If you have more questions, feel free to message EUP if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 *************************************************************************/
 
   package com.eup.codeopsstudio.editor.langs.textmate.provider;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
* Provides language-scope mapping functionality for the code editor.
* @author EUP
*/
public class JsonLanguageInfoProvider implements LanguageInfoProvider {
	
	private static JsonLanguageInfoProvider instance;
	private final Map<String, String> scopeMap;
	
	/**
	* Private constructor to initialize the JsonLanguageInfoProvider.
	* Loads language-scope data from a provided InputStream.
	*
	* @param context         The available context.
	* @param jsonInputStream InputStream containing JSON data.
	*/
	private JsonLanguageInfoProvider(Context context, InputStream jsonInputStream) {
		scopeMap = new HashMap<>();
		scopeMap.putAll(loadConfig(context, jsonInputStream));
	}
	
	/**
	* Retrieves an instance of JsonLanguageInfoProvider.
	*
	* @param context The available context.
	* @param inputstream The configuration file inputstream
	* @return The LanguageScopeProvider instance.
	*/
	public static JsonLanguageInfoProvider getInstance(Context context, InputStream inputStream) {
		return new JsonLanguageInfoProvider(context, inputStream);
	}
	
	/**
	* Loads language-scope data from assets or a provided InputStream.
	*
	* @param context         The available context.
	* @param jsonInputStream InputStream containing JSON data (if not from assets).
	* @return The loaded language-scope data.
	*/
	private Map<String, String> loadConfig(Context context, InputStream inputStream) {
		if (scopeMap != null && !scopeMap.isEmpty()) {
			// If data is already cached, return it
			return scopeMap;
		}
		
		try {
			Map<String, String> loadedScopeMap = new Gson().fromJson(readInputStream(inputStream),
			new TypeToken<Map<String, String>>() {
			}.getType());
			return loadedScopeMap;
			} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String getScope(String extensionEntry) {
		return scopeMap.get(extensionEntry);
	}
	
	@Override
	public String getLanguageExtension(String scopeEntry) {
		for (Map.Entry<String, String> entry : scopeMap.entrySet()) {
			if (entry.getValue().equals(scopeEntry)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public String readInputStream(InputStream inputStream) throws IOException {
		String data = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String dataRow = "";
		while ((dataRow = reader.readLine()) != null) {
			data += dataRow + "\n";
		}
		reader.close();
		return data;
	}
}
