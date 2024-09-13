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
 
   package com.eup.codeopsstudio.common.util;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;

public class LanguageInfoBuilder {

  private String grammar, name, scopeName, languageConfiguration;
  // configuration
  private HashMap<String, Object> languages = new HashMap<>();
  private ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();
  //  scope
  private HashMap<String, Object> hashMap = new HashMap<>();
  private HashMap<String, Object> innerMap = new HashMap<>();
  private ArrayList<String> extensionList = new ArrayList<>();
  private String extensionInput = "";

  public LanguageInfoBuilder(
      String grammar, String name, String scopeName, String languageConfiguration) {
    this.grammar = grammar;
    this.name = name;
    this.scopeName = scopeName;
    this.languageConfiguration = languageConfiguration;
  }

  public String getGrammar() {
    return this.grammar;
  }

  public void setGrammar(String grammar) {
    this.grammar = grammar;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getScopeName() {
    return this.scopeName;
  }

  public void setScopeName(String scopeName) {
    this.scopeName = scopeName;
  }

  public String getLanguageConfiguration() {
    return this.languageConfiguration;
  }

  public void setLanguageConfiguration(String languageConfiguration) {
    this.languageConfiguration = languageConfiguration;
  }

  public String createConfiguration() {
    languages = new HashMap<>();
    languages.put("grammar", getGrammar());
    languages.put("name", getName());
    languages.put("scopeName", getScopeName());
    languages.put("languageConfiguration", getLanguageConfiguration());
    listMap.add(languages);
    languages = new HashMap<>();
    languages.put("languages", listMap);
    return new Gson().toJson(languages);
  }
 
    
  public String createScopes(String extensionInput,String scopeName, String languageName) {
    innerMap.put("scope", scopeName);
    String separator = ",";
    String[] extensions = extensionInput.split(separator);
    for (String extension : extensions) {
      String trimmedExtension = extension.trim();
      if (!trimmedExtension.isEmpty()) {
        extensionList.add(trimmedExtension);
      }
    }

    innerMap.put("extension", extensionList);
    hashMap.put(languageName, innerMap);
    return new Gson().toJson(hashMap);
  }
}
