package com.liferay.osb.testray.invoker;

import java.util.Objects;
import java.util.Arrays;

public class TestrayInvoker {

	/**
	 * Accepts arguments:
	 * -h host -- which server to access, defaults to https://testray.liferay.com/web/guest/home
	 * -s -- don't output the filename and duration for each imported file
	 * -u user:pass -- the user credentials
	 * -v -- print out the response for each result added/updated
	 */
	public static void main(String[] args) {
		String creds = getCreds(args);
		String host = getHost(args);
		boolean silent = isSilent(args);
		boolean verbose = isVerbose(args);

		boolean fail = false;

		String[] filenames = getFilenames(args);

		if (filenames.length == 0) {
			try {
				filenames = StringUtil.toString(System.in).split("\\s");
			}
			catch (Exception e) {
				throw new Error("Failed to read from std in", e);
			}
		}

		for (String filename : filenames) {
			long start = System.currentTimeMillis();

			PoshiXMLObject xml = new PoshiXMLObject(filename);

			try {
				TestrayAutomationUtil.doImport(xml, host, creds, verbose);

				long end = System.currentTimeMillis();

				System.out.printf(
					"Imported %1s in %2fs\n", filename,
					((end - start) / 1000.0));
			}
			catch (Exception e) {
				e.printStackTrace();

				fail = true;
			}
		}

		if (fail) {
			System.exit(1);
		}
	}

	protected static String getCreds(String[] args) {
		int i = _indexOf(args, "-u");

		return (i >= 0) ? args[i + 1] : null;
	}

	protected static String getHost(String[] args) {
		int i = _indexOf(args, "-h");

		return (i >= 0) ? args[i + 1] :
			"https://testray.liferay.com/web/guest/home";
	}

	protected static String[] getFilenames(String[] args) {
		int credsIdx = _indexOf(args, "-u");
		int hostIdx = _indexOf(args, "-h");
		int silentIdx = _indexOf(args, "-s");
		int verboseIdx = _indexOf(args, "-v");

		int credsValueIdx = (credsIdx < 0) ? -1 : credsIdx + 1;
		int hostValueIdx = (hostIdx < 0) ? -1 : hostIdx + 1;

		return _copyOfExcludingIndexes(
			args, credsIdx, credsValueIdx, hostIdx, hostValueIdx, silentIdx,
			verboseIdx);
	}

	protected static boolean isSilent(String[] args) {
		return _indexOf(args, "-s") >= 0;
	}

	protected static boolean isVerbose(String[] args) {
		return _indexOf(args, "-v") >= 0;
	}

	private static boolean _contains(int[] arr, int val) {
		for (int i : arr) {
			if (i == val) return true;
		}

		return false;
	}

	private static String[] _copyOfExcludingIndexes(String[] arr, int... idxs) {
		int[] excluding = Arrays.stream(idxs).filter(i -> i != -1).toArray();

		String[] copy = new String[arr.length - excluding.length];

		for (int i = 0, j = 0; i < arr.length; i++) {
			if (_contains(excluding, i)) continue;

			copy[j++] = arr[i];
		}

		return copy;
	}

	private static int _indexOf(String[] arr, String val) {
		for (int i = 0; i < arr.length; i++) {
			if (Objects.equals(arr[i], val)) return i;
		}

		return -1;
	}

}