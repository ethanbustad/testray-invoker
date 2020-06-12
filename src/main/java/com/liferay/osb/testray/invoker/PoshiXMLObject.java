package com.liferay.osb.testray.invoker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class PoshiXMLObject {

	public PoshiXMLObject(File file) {
		try {
			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();

			_document = builder.parse(file);
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException("Unable to access file " + file);
		}
		catch (ParserConfigurationException|SAXException e) {
			throw new Error(e);
		}
	}

	public PoshiXMLObject(String filename) {
		this(new File(filename));
	}

	public String getEnvironmentValue(String property) {
		return getEnvironmentValues().get(property);
	}

	public Map<String, String> getEnvironmentValues() {
		if (_environmentValues == null) {
			Map<String, String> environmentValues = new HashMap<>();

			Element element = _document.getDocumentElement();

			Node environmentsElement =
				element.getElementsByTagName("environments").item(0);

			NodeList environments = environmentsElement.getChildNodes();

			for (int i = 0; i < environments.getLength(); i++) {
				Node environment = environments.item(i);

				if (!(environment instanceof Element)) {
					continue;
				}

				environmentValues.put(
					_getAttribute(environment, "type"),
					_getAttribute(environment, "option"));
			}

			_environmentValues = environmentValues;
		}

		return _environmentValues;
	}

	public Map<String, String> getGlobalProperties() {
		if (_globalProperties == null) {
			_globalProperties = _getProperties(_document.getDocumentElement());
		}

		return _globalProperties;
	}

	public String getGlobalProperty(String property) {
		return getGlobalProperties().get(property);
	}

	public List<Testcase> getTestcases() {
		if (_testcases == null) {
			List<Testcase> testcases = new ArrayList<>();

			Element element = _document.getDocumentElement();

			NodeList testcaseElements = element.getElementsByTagName("testcase");

			for (int i = 0; i < testcaseElements.getLength(); i++) {
				Node testcaseElement = testcaseElements.item(i);

				Map<String, String> attachments = new HashMap<>();

				NodeList attachmentElements =
					_getChildNode(testcaseElement, "attachments").getChildNodes();

				for (int j = 0; j < attachmentElements.getLength(); j++) {
					attachments.put(
						_getAttribute(attachmentElements.item(j), "name"),
						_getAttribute(attachmentElements.item(j), "value"));
				}

				Map<String, String> properties = new HashMap<>();

				NodeList propertyElements =
					_getChildNode(testcaseElement, "properties").getChildNodes();

				for (int j = 0; j < propertyElements.getLength(); j++) {
					properties.put(
						_getAttribute(propertyElements.item(j), "name"),
						_getAttribute(propertyElements.item(j), "value"));
				}

				String failureMessage = _getAttribute(
					_getChildNode(testcaseElement, "failure"), "message");

				testcases.add(
					new Testcase(attachments, properties, failureMessage));
			}

			_testcases = testcases;
		}

		return _testcases;
	}

	public class Testcase {

		public Testcase(
			Map<String, String> attachments, Map<String, String> properties,
			String failureMessage) {

			_attachments = attachments;
			_properties = properties;
			_failureMessage = failureMessage;
		}

		public Map<String, String> getAttachments() {
			return _attachments;
		}

		public String getFailureMessage() {
			return _failureMessage;
		}

		public Map<String, String> getProperties() {
			return _properties;
		}

		public String getProperty(String property) {
			return _properties.get(property);
		}

		private final Map<String, String> _attachments;
		private final String _failureMessage;
		private final Map<String, String> _properties;

	}

	private String _getAttribute(Node node, String attribute) {
		NamedNodeMap attributes = node.getAttributes();

		if (attributes == null) {
			return null;
		}

		Node attributeNode = attributes.getNamedItem(attribute);

		return (attributeNode == null) ? null : attributeNode.getNodeValue();
	}

	private Node _getChildNode(Node node, String childName) {
		NodeList children = node.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if (Objects.equals(child.getNodeName(), childName)) {
				return child;
			}
		}

		return null;
	}

	private Map<String, String> _getProperties(Element element) {
		Map<String, String> properties = new HashMap<>();

		Node propertiesElement =
			element.getElementsByTagName("properties").item(0);

		NodeList propertyElements = propertiesElement.getChildNodes();

		for (int i = 0; i < propertyElements.getLength(); i++) {
			Node property = propertyElements.item(i);

			properties.put(
				_getAttribute(property, "name"),
				_getAttribute(property, "value"));
		}

		return properties;
	}

	private final Document _document;
	private Map<String, String> _environmentValues;
	private Map<String, String> _globalProperties;
	private List<Testcase> _testcases;

}