/**
 * SPDX-FileCopyrightText: © 2018 Liferay, Inc. <https://liferay.com>
 * SPDX-License-Identifier: MIT
 */

package com.liferay.osb.testray.invoker;

import java.io.DataOutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {

	public static String get(String urlString) throws IOException {
		return get(urlString, Collections.emptyMap());
	}

	public static String get(String urlString, Map<String, String> params)
		throws IOException {

		return get(urlString, Collections.emptyMap(), params);
	}

	public static String get(String urlString, Map<String, String> params,
		String creds)
		throws IOException {

		Map<String, String> headers = new HashMap<String, String>();

		String base64Credentials =
			Base64.getEncoder().encodeToString(creds.getBytes());

		headers.put("Authorization", "Basic " + base64Credentials);

		return get(urlString, headers, params);
	}

	public static String get(String urlString, String creds)
		throws IOException {

		return get(urlString, null, creds);
	}

	public static String post(String urlString, Map<String, String> data)
		throws IOException {

		throw new UnsupportedOperationException();
	}

	public static String post(
			String urlString, Map<String, String> data, String creds)
		throws IOException {

		throw new UnsupportedOperationException();
	}

	public static String post(
			String urlString, String json)
		throws IOException {

		try {
			URL url = new URL(urlString);

			HttpURLConnection connection =
				(HttpURLConnection)url.openConnection();

			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty(
				"Content-Type", "application/json");

			DataOutputStream dos = new DataOutputStream(
				connection.getOutputStream());

			dos.writeBytes(json);

			dos.flush();
			dos.close();

			connection.connect();

			try {
				return StringUtil.toString(connection.getInputStream());
			}
			catch (IOException ioe) {
				throw new IOException(
					StringUtil.toString(connection.getErrorStream()), ioe);
			}
		}
		catch (Exception e) {
			throw new IOException("Hitting URL " + urlString + " failed", e);
		}
	}

	public static String post(
			String urlString, String json, String creds)
		throws IOException {

		throw new UnsupportedOperationException();
	}

	protected static String append(
		String urlString, Map<String, String> params) {

		if ((params == null) || params.isEmpty()) {
			return urlString;
		}

		String queryString = encode(params);

		if (urlString.contains(QUERY_SEPARATOR)) {
			return urlString + PARAM_SEPARATOR + queryString;
		}

		return urlString + QUERY_SEPARATOR + queryString;
	}

	protected static String encode(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();

		try {
			for (Map.Entry<String, String> paramEntry : params.entrySet()) {
				if (paramEntry.getValue() == null) {
					continue;
				}

				sb.append(
					URLEncoder.encode(paramEntry.getKey(), DEFAULT_ENCODING));
				sb.append(KEY_VALUE_SEPARATOR);
				sb.append(
					URLEncoder.encode(paramEntry.getValue(), DEFAULT_ENCODING));
				sb.append(PARAM_SEPARATOR);
			}
		}
		catch (IOException ioe) {
			throw new RuntimeException(
				"The encoding " + DEFAULT_ENCODING + " is not supported", ioe);
		}

		sb.setLength(sb.length() - 1);

		return sb.toString();
	}

	protected static String get(
			String urlString, Map<String, String> headers,
			Map<String, String> params)
		throws IOException {

		String fullURLString = append(urlString, params);

		try {
			URL url = new URL(fullURLString);

			HttpURLConnection connection =
				(HttpURLConnection)url.openConnection();

			for (Map.Entry<String, String> header : headers.entrySet()) {
				connection.setRequestProperty(
					header.getKey(), header.getValue());
			}

			connection.connect();

			try {
				return StringUtil.toString(connection.getInputStream());
			}
			catch (IOException ioe) {
				throw new IOException(
					StringUtil.toString(connection.getErrorStream()), ioe);
			}
		}
		catch (Exception e) {
			throw new IOException(
				"Hitting URL " + fullURLString + " failed", e);
		}
	}

	protected static final String DEFAULT_ENCODING = "UTF-8";

	protected static final String KEY_VALUE_SEPARATOR = "=";

	protected static final String PARAM_SEPARATOR = "&";

	protected static final String QUERY_SEPARATOR = "?";

}