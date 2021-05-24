/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.core.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.tenio.common.configuration.CommonConfiguration;
import com.tenio.common.utilities.XMLUtility;
import com.tenio.core.configuration.defines.CoreConfigurationType;
import com.tenio.core.network.defines.RestMethod;
import com.tenio.core.network.defines.TransportType;
import com.tenio.core.network.defines.data.HttpConfig;
import com.tenio.core.network.defines.data.PathConfig;
import com.tenio.core.network.defines.data.SocketConfig;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * TenIOConfig.example.xml. You can also extend this file to create your own
 * configuration values.
 * 
 * @see CoreConfigurationType
 * 
 * @author kong
 */
// FIXME: Fix me
public abstract class CoreConfiguration extends CommonConfiguration {

	/**
	 * All ports in sockets zone
	 */
	private final List<SocketConfig> __socketPorts;

	/**
	 * All ports in http zone
	 */
	private final List<HttpConfig> __httpPorts;

	/**
	 * The constructor
	 * 
	 * @param file The name of your configuration file and this file needs to be put
	 *             in same folder with your application
	 */
	public CoreConfiguration(String file) {
		__socketPorts = new ArrayList<SocketConfig>();
		__httpPorts = new ArrayList<HttpConfig>();

		try {
			__load(file);
		} catch (Exception e) {
			error(e, "file: ", file);
		}
	}

	/**
	 * Read file content and convert it to configuration values.
	 * 
	 * @param file The name of your configuration file and this file needs to be put
	 *             in same folder with your application
	 * @throws Exception some exceptions, which can be occurred in reading or
	 *                   parsing the file
	 */
	private void __load(String file) throws Exception {

		Document xDoc = XMLUtility.parseFile(new File(file));
		Node root = xDoc.getFirstChild();

		// Server Properties
		var attrServerProperties = XMLUtility.getNodeList(root, "//Server/Properties/Property");
		for (int j = 0; j < attrServerProperties.getLength(); j++) {
			var dataNode = attrServerProperties.item(j);
			var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
			__push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
		}

		// Network Properties
		var attrNetworkProperties = XMLUtility.getNodeList(root, "//Server/Network/Properties/Property");
		for (int j = 0; j < attrNetworkProperties.getLength(); j++) {
			var dataNode = attrNetworkProperties.item(j);
			var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
			__push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
		}
		// Network Sockets
		var attrNetworkSockets = XMLUtility.getNodeList(root, "//Server/Network/Sockets/Port");
		for (int j = 0; j < attrNetworkSockets.getLength(); j++) {
			var dataNode = attrNetworkSockets.item(j);
			var port = new SocketConfig(dataNode.getAttributes().getNamedItem("name").getTextContent(),
					TransportType.getByValue(dataNode.getAttributes().getNamedItem("type").getTextContent()),
					Integer.parseInt(dataNode.getTextContent()));

			__socketPorts.add(port);
		}
		__push(CoreConfigurationType.SOCKET_CONFIGS, __socketPorts);
		// Network HTTPs
		var attrNetworkHttps = XMLUtility.getNodeList(root, "//Server/Network/Http/Port");
		for (int i = 0; i < attrNetworkHttps.getLength(); i++) {
			var pPortNode = attrNetworkHttps.item(i);
			var port = new HttpConfig(pPortNode.getAttributes().getNamedItem("name").getTextContent(),
					Integer.parseInt(pPortNode.getAttributes().getNamedItem("value").getTextContent()));

			var attrHttpPaths = XMLUtility.getNodeList(attrNetworkHttps.item(i), "//Path");
			for (int j = 0; j < attrHttpPaths.getLength(); j++) {
				var pPathNode = attrHttpPaths.item(j);
				var path = new PathConfig(pPathNode.getAttributes().getNamedItem("name").getTextContent(),
						RestMethod.getByValue(pPathNode.getAttributes().getNamedItem("method").getTextContent()),
						pPathNode.getTextContent(), pPathNode.getAttributes().getNamedItem("desc").getTextContent(),
						Integer.parseInt(pPathNode.getAttributes().getNamedItem("version").getTextContent()));

				port.addPath(path);
			}

			__httpPorts.add(port);
		}
		__push(CoreConfigurationType.HTTP_CONFIGS, __httpPorts);

		// Implemented Classes
		var attrImplementedClasses = XMLUtility.getNodeList(root, "//Server/Implements/Class");
		for (int j = 0; j < attrImplementedClasses.getLength(); j++) {
			var dataNode = attrImplementedClasses.item(j);
			var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
			__push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
		}

		// Configured Workers
		var attrConfigurationWorkers = XMLUtility.getNodeList(root, "//Server/Configuration/Workers/Worker");
		for (int j = 0; j < attrConfigurationWorkers.getLength(); j++) {
			var dataNode = attrConfigurationWorkers.item(j);
			var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
			__push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
		}
		// Configured Schedules
		var attrConfigurationSchedules = XMLUtility.getNodeList(root, "//Server/Configuration/Schedules/Task");
		for (int j = 0; j < attrConfigurationSchedules.getLength(); j++) {
			var dataNode = attrConfigurationSchedules.item(j);
			var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
			__push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
		}
		// Configured Properties
		var attrConfigurationProperties = XMLUtility.getNodeList(root, "//Server/Configuration/Properties/Property");
		for (int j = 0; j < attrConfigurationProperties.getLength(); j++) {
			var dataNode = attrConfigurationProperties.item(j);
			var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
			__push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
		}

		// Extension Properties
		var attrExtensionProperties = XMLUtility.getNodeList(root, "//Server/Extension/Properties/Property");
		var extProperties = new HashMap<String, String>();
		for (int j = 0; j < attrExtensionProperties.getLength(); j++) {
			var dataNode = attrExtensionProperties.item(j);
			var key = dataNode.getAttributes().getNamedItem("name").getTextContent();
			var value = dataNode.getTextContent();
			extProperties.put(key, value);
		}

		__extend(extProperties);
	}

}
