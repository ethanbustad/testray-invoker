/**
 * SPDX-FileCopyrightText: © 2018 Liferay, Inc. <https://liferay.com>
 * SPDX-License-Identifier: MIT
 */

package com.liferay.osb.testray.invoker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlloyMVCHelper {

	public AlloyMVCHelper(
		String pageURL, String portlet, String creds, boolean verbose) {

		_baseURL = String.join("/", pageURL, "-", portlet);
		_creds = creds;
		_verbose = verbose;
	}

	public JSONObject get(
			String controller, String action, String id,
			Map<String, String> params)
		throws IOException {

		JSONObject response = _getResponse(controller, action, id, params);

		return (response == null) ? null : response.optJSONObject("data");
	}

	public JSONArray getArray(
			String controller, String action, String id,
			Map<String, String> params)
		throws IOException {

		JSONObject response = _getResponse(controller, action, id, params);

		return (response == null) ? null : response.optJSONArray("data");
	}

	public JSONObject post(
		String controller, String action, String id,
		Map<String, String> params) {

		String url = _buildURL(controller, action, id);

		try {
			String response = HttpUtil.post(url, params, _creds);

			JSONObject jsonObject = new JSONObject(response);

			return jsonObject.getJSONObject("data");
		}
		catch (JSONException|IOException e) {
			e.printStackTrace();

			return null;
		}
	}

	private String _append(String base, String addition) {
		if ((addition != null) && !addition.isEmpty()) {
			return base.concat("/").concat(addition);
		}
		else {
			return base;
		}
	}

	private JSONObject _getResponse(
			String controller, String action, String id,
			Map<String, String> params)
		throws IOException {

		String url = _buildURL(controller, action, id);

		long start = System.currentTimeMillis();

		try {
			String response = HttpUtil.get(url, params, _creds);

			return new JSONObject(response);
		}
		catch (JSONException jsone) {
			jsone.printStackTrace();

			return null;
		}
		finally {
			if (_verbose) {
				long end = System.currentTimeMillis();

				System.out.printf(
					"Completed call to %1s in %2fs\n", url,
					((end - start) / 1000.0));
			}
		}
	}

	private String _buildURL(String controller, String action, String id) {
		String url = _baseURL;

		url = _append(url, controller);

		try {
			url = _append(
				url, (id == null) ? null : URLEncoder.encode(id, "UTF-8"));
		}
		catch (UnsupportedEncodingException uee) {
			// utf-8 must be supported
		}

		url = _append(url, action);

		return url + ".json";
	}

	private final String _baseURL;
	private final String _creds;
	private final boolean _verbose;

}