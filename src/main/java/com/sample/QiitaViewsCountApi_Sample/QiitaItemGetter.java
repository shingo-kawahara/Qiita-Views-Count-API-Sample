package com.sample.QiitaViewsCountApi_Sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;

class QiitaItemGetter {

	private QiitaItemGetter() {
	}

	static int getUserItemsCount(
			OkHttpClient client,
			String token) throws IOException {
		int page = 1;
		int perPage = 1;
		String url = "https://qiita.com/api/v2/authenticated_user/items?page="
		+ page + "&" + "per_page=" + perPage;

		String jsonText = RestCaller.callRestApi(client, url, token);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(jsonText).findValue("user");

		return root.get("items_count").intValue();
	}

	static ArrayList<String> getItemIdList(
			OkHttpClient client,
			String token,
			int loopCount) throws IOException {
		int page = 1;
		int perPage = 100;
		String url = "https://qiita.com/api/v2/authenticated_user/items?page="
		+ page + "&" + "per_page=" + perPage;
		String jsonText = "";
		ArrayList<String> list = new ArrayList<>();

		for (int i = 0; i < loopCount; i++) {
			jsonText = RestCaller.callRestApi(client, url, token);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(jsonText);

			for (JsonNode node : root) {
				list.add(node.findValue("id").asText());
			}
			page = page + 1;
			url = "https://qiita.com/api/v2/authenticated_user/items?page="
					+ page + "&" + "per_page=" + perPage;
		}
		return list;
	}

	static String getItemDetails(
			OkHttpClient client,
			String token,
			ArrayList<String> list) throws IOException {

		String url = "";
		String jsonText = "";
		StringBuilder sb = new StringBuilder();
		Map<String, Integer> map = new TreeMap<>();

		for (int i = 0, length = list.size(); i < length; i++) {
			url = "https://qiita.com/api/v2/items/" + list.get(i);
			jsonText = RestCaller.callRestApi(client, url, token);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(jsonText);

			map.put(root.get("title").textValue(),root.get("page_views_count").intValue());
		}

		List<Entry<String, Integer>> listEntry = new ArrayList<Entry<String, Integer>>(map.entrySet());
		Collections.sort(listEntry, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> obj1, Entry<String, Integer> obj2) {
				return obj2.getValue().compareTo(obj1.getValue());
			}
		});

		for(Entry<String, Integer> entry : listEntry) {
			sb.append(entry.getValue() + "," + entry.getKey());
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
