package com.example.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonParse {
	public static List<Map<String, Object>> getListMap(String key, String jsonString) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
//			JSONTokener jsonParser = new JSONTokener(jsonString);
//			JSONObject jsonObject = (JSONObject)jsonParser.nextValue();
			JSONObject jsonObject = new JSONObject(jsonString);			
			JSONArray jsonArray = jsonObject.getJSONArray(key);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject2 = jsonArray.getJSONObject(i);
				Map<String, Object> map = new HashMap<String, Object>();
				@SuppressWarnings("unchecked")
				Iterator<String> iterator = jsonObject2.keys();
				
				while (iterator.hasNext()) {
					String json_key = iterator.next();
					Object json_value = jsonObject2.get(json_key);
					if(json_value == null) {
						json_value = "";
					}
					map.put(json_key, json_value);
				}
				list.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	// ½âÎöµ¥¸öJSON
	public static Map<String, Object> getMap(String jsonString) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			@SuppressWarnings("unchecked")
			Iterator<String> iterator = jsonObject.keys();
			while (iterator.hasNext()) {
				String json_key = iterator.next();
				Object json_value = jsonObject.get(json_key);
				if (json_value == null) {
					json_value = "";
				}
				map.put(json_key, json_value);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

}
