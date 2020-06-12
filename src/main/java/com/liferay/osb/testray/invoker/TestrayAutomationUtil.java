package com.liferay.osb.testray.invoker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestrayAutomationUtil {

	public static void doImport(
			PoshiXMLObject xml, String host, String creds, boolean verbose)
		throws IOException {

		AlloyMVCHelper helper = new AlloyMVCHelper(
			host, "testray", creds, verbose);

		JSONObject build = fetchOrAddTestrayBuild(helper, xml);

		if (verbose) {
			System.out.println(build.toString(2));
		}

		JSONObject run = fetchOrAddTestrayRun(helper, xml, build);

		if (verbose) {
			System.out.println(run.toString(2));
		}

		for (PoshiXMLObject.Testcase testcase : xml.getTestcases()) {
			JSONObject result = updateOrAddTestrayCaseResult(
				helper, build.getString("testrayProjectId"),
				run.getString("testrayRunId"), testcase);

			if (verbose) {
				System.out.println(result.toString(2));
			}
		}
	}

	protected static JSONObject fetchOrAddTestrayBuild(
			AlloyMVCHelper helper, PoshiXMLObject xml)
		throws IOException {

		// check if build exists by ID

		String testrayBuildId = xml.getGlobalProperty("testray.build.id");

		if ((testrayBuildId != null) && !testrayBuildId.isEmpty()) {
			return helper.get("builds", "view", testrayBuildId, null);
		}

		Map<String, String> params = new HashMap<>();

		// project

		String projectName = xml.getGlobalProperty("testray.project.name");

		JSONObject project;

		try {
			project = helper.get("projects", "view", "_" + projectName, null);
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException(
				"The project " + projectName + " doesn't exist", ioe);
		}

		params.put("testrayProjectId", project.getString("testrayProjectId"));

		// routine

		String routineName = xml.getGlobalProperty("testray.build.type");

		JSONObject routine;

		try {
			routine = helper.get("routines", "view", "_" + routineName, params);
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException(
				"The routine " + routineName + " doesn't exist", ioe);
		}

		params.put("testrayRoutineId", routine.getString("testrayRoutineId"));

		// check if build exists by name

		String name = xml.getGlobalProperty("testray.build.name");

		JSONObject build;

		try {
			build = helper.get("builds", "view", "_" + name, params);
		}
		catch (IOException ioe) {
			// product version

			String productVersionName = xml.getGlobalProperty(
				"testray.product.version");

			JSONObject productVersion = helper.get(
				"product_versions", "view", "_" + productVersionName, params);

			if (productVersion == null) {
				params.put("name", productVersionName);

				productVersion = helper.get(
					"product_versions", "add", null, params);
			}

			// add build

			params.put(
				"testrayProductVersionId",
				productVersion.getString("testrayProductVersionId"));

			params.put("name", name);

			build = helper.get("builds", "add", null, params);
		}

		build.put("testrayProjectId", project.getString("testrayProjectId"));

		build.put(
			"defaultTestrayFactors",
			routine.getJSONArray("defaultTestrayFactors"));

		return build;
	}

	protected static JSONObject fetchOrAddTestrayRun(
			AlloyMVCHelper helper, PoshiXMLObject xml, JSONObject build)
		throws IOException {

		Map<String, String> environmentValues = xml.getEnvironmentValues();

		Map<String, String> params = new HashMap<>();

		// check all runs in the build

		params.put("testrayBuildId", build.getString("testrayBuildId"));

		JSONArray runs = helper.getArray("runs", "index", null, params);

		for (int i = 0; i < runs.length(); i++) {
			JSONObject run = runs.getJSONObject(i);

			JSONArray runFactorsJSONArray = run.getJSONArray("testrayFactors");

			if (_matches(environmentValues, runFactorsJSONArray)) {
				return run;
			}
		}

		// add run

		JSONArray environmentFactors = new JSONArray();

		for (Map.Entry<String, String> environmentEntry :
				environmentValues.entrySet()) {

			JSONObject environmentFactor = new JSONObject();

			environmentFactor.put(
				"testrayFactorCategoryName", environmentEntry.getKey());
			environmentFactor.put(
				"testrayFactorOptionName", environmentEntry.getValue());

			environmentFactors.put(environmentFactor);
		}

		params.put("testrayFactors", environmentFactors.toString());

		try {
			return helper.get("runs", "add", null, params);
		}
		catch (IOException ioe) {
			JSONArray defaultTestrayFactors = build.getJSONArray(
				"defaultTestrayFactors");

			if ((defaultTestrayFactors != null) &&
				(defaultTestrayFactors.length() > 0)) {

				throw ioe;
			}

			Map<String, String> routineParams = new HashMap<>();

			routineParams.put(
				"defaultTestrayFactors", defaultTestrayFactors.toString());

			helper.get(
				"routines", "update", build.getString("testrayRoutineId"),
				routineParams);

			return helper.get("runs", "add", null, params);
		}
	}

	protected static JSONObject updateOrAddTestrayCase(
			AlloyMVCHelper helper, String testrayProjectId,
			PoshiXMLObject.Testcase testcase)
		throws IOException {

		Map<String, String> params = new HashMap<>();

		params.put("testrayProjectId", testrayProjectId);

		// case type

		String caseTypeName = testcase.getProperty(
			"testray.case.type.name");

		JSONObject caseType;

		try {
			caseType = helper.get(
				"case_types", "view", "_" + caseTypeName, null);
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException(
				"The case type " + caseTypeName + " doesn't exist", ioe);
		}

		params.put(
			"testrayCaseTypeId", caseType.getString("testrayCaseTypeId"));

		// component

		JSONObject component = updateOrAddTestrayComponent(
			helper, testrayProjectId, testcase);

		params.put(
			"testrayComponentId", component.getString("testrayComponentId"));

		// case

		String caseName = testcase.getProperty("testray.testcase.name");

		JSONObject case_;

		try {
			case_ = helper.get("cases", "view", "_" + caseName, params);
		}
		catch (IOException ioe) {
			params.put(
				"priority", testcase.getProperty("testray.testcase.priority"));

			params.put("name", caseName);

			return helper.get("cases", "add", null, params);
		}

		if (!Objects.equals(
				case_.getString("testrayCaseTypeId"),
				caseType.getString("testrayCaseTypeId")) ||
			!Objects.equals(
				case_.getJSONObject("mainComponent").getString("testrayComponentId"),
				component.getString("testrayComponentId"))) {

			return helper.get(
				"cases", "update", case_.getString("testrayCaseId"), params);
		}
		else {
			return case_;
		}
	}

	protected static JSONObject updateOrAddTestrayCaseResult(
			AlloyMVCHelper helper, String testrayProjectId, String testrayRunId,
			PoshiXMLObject.Testcase testcase)
		throws IOException {

		Map<String, String> params = new HashMap<>();

		params.put("testrayProjectId", testrayProjectId);
		params.put("testrayRunId", testrayRunId);

		// case

		String caseName = testcase.getProperty("testray.testcase.name");

		JSONObject case_ = updateOrAddTestrayCase(
			helper, testrayProjectId, testcase);

		params.put("testrayCaseId", case_.getString("testrayCaseId"));

		// check if it exists

		JSONObject result = null;

		try {
			result = helper.get("case_results", "view", null, params);
		}
		catch (IOException ioe) {
			// then we'll create a new one below
		}

		try {
			JSONObject attachments = new JSONObject(testcase.getAttachments());

			params.put("attachments", attachments.toString());
		}
		catch (JSONException jsone) {
			jsone.printStackTrace();
		}

		params.put("errors", testcase.getFailureMessage());

		params.put(
			"statusLabel", testcase.getProperty("testray.testcase.status"));

		params.put(
			"warnings", testcase.getProperty("testray.testcase.warnings"));

		if (result != null) {
			return helper.get(
				"case_results", "update",
				result.getString("testrayCaseResultId"), params);
		}
		else {
			return helper.get("case_results", "add", null, params);
		}
	}

	protected static JSONObject updateOrAddTestrayComponent(
			AlloyMVCHelper helper, String testrayProjectId,
			PoshiXMLObject.Testcase testcase)
		throws IOException {

		Map<String, String> params = new HashMap<>();

		params.put("testrayProjectId", testrayProjectId);

		// team

		String teamName = testcase.getProperty("testray.team.name");

		JSONObject team;

		try {
			team = helper.get("teams", "view", "_" + teamName, params);
		}
		catch (IOException ioe) {
			params.put("name", teamName);

			team = helper.get("teams", "add", null, params);
		}

		params.put("testrayTeamId", team.getString("testrayTeamId"));

		// component

		String componentName = testcase.getProperty(
			"testray.main.component.name");

		params.put("name", componentName);

		JSONObject component;

		try {
			component = helper.get(
				"components", "view", "_" + componentName, params);
		}
		catch (IOException ioe) {
			// add
			return helper.get("components", "add", null, params);
		}

		if (!Objects.equals(
				component.getString("testrayTeamId"),
				team.getString("testrayTeamId"))) {

			// update
			return helper.get(
				"components", "update",
				component.getString("testrayComponentId"), params);
		}
		else {
			return component;
		}
	}

	private static boolean _matches(
		Map<String, String> environmentValues, JSONArray runFactorsJSONArray) {

		Map<String, String> runEnvironmentValues = new HashMap<>();

		for (int i = 0; i < runFactorsJSONArray.length(); i++) {
			JSONObject runFactor = runFactorsJSONArray.getJSONObject(i);

			runEnvironmentValues.put(
				runFactor.getString("testrayFactorCategoryName"),
				runFactor.getString("testrayFactorOptionName"));
		}

		return runEnvironmentValues.equals(environmentValues);
	}

}