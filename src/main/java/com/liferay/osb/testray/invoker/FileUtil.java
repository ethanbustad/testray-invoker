/**
 * SPDX-FileCopyrightText: © 2018 Liferay, Inc. <https://liferay.com>
 * SPDX-License-Identifier: MIT
 */

package com.liferay.osb.testray.invoker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {

	public static String read(File file) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(
			file));

		StringBuilder sb = new StringBuilder();

		String line = null;

		while ((line = bufferedReader.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}

		bufferedReader.close();

		return sb.toString();
	}

	public static String read(String filename) throws IOException {
		return read(new File(filename));
	}

}