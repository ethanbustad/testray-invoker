package com.liferay.osb.testray.invoker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Collection;

public class StringUtil {

	public static String join(Collection<?> collection) {
		return join(collection, DEFAULT_DELIMITER);
	}

	public static String join(Collection<?> collection, String delimiter) {
		StringBuilder sb = new StringBuilder();

		for (Object element : collection) {
			sb.append(element);
			sb.append(delimiter);
		}

		sb.setLength(sb.length() - 1);

		return sb.toString();
	}

	public static String toString(InputStream inputStream) throws IOException {
		StringBuilder response = new StringBuilder();

		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream))) {

			String inputLine;

			while ((inputLine = bufferedReader.readLine()) != null) {
				response.append(inputLine);
				response.append("\n");
			}
		}

		return response.toString();
	}

	private static final String DEFAULT_DELIMITER = ",";

}