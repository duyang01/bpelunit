package org.bpelunit.framework.control.datasource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bpelunit.framework.control.ext.IDataSource;
import org.bpelunit.framework.control.util.ExtensionRegistry;
import org.bpelunit.framework.exception.DataSourceException;
import org.bpelunit.framework.exception.SpecificationException;
import org.bpelunit.framework.xml.suite.XMLDataSource;
import org.bpelunit.framework.xml.suite.XMLProperty;
import org.bpelunit.framework.xml.suite.XMLTestCase;
import org.bpelunit.framework.xml.suite.XMLTestSuite;

/**
 * Utility methods for working with data sources.
 * 
 * @author Antonio García Domínguez
 * @version 1.1
 */
public class DataSourceUtil {

	/**
	 * Creates the effective IDataSource for a test case inside a test suite. It
	 * is a convenience wrapper for
	 * {@link #createDataSource(String, InputStream, Map)}.
	 * 
	 * @param xmlTestSuite
	 *            XMLBeans object for the <testSuite> element.
	 * @param xmlTestCase
	 *            XMLBeans object for the <testCase> element.
	 * @return <code>null</code> if no data source is available. Otherwise, an
	 *         instance of the appropriate data source will be returned, with
	 *         its contents initialized from its source according to
	 *         {@link #getStreamForDataSource(XMLDataSource)} and all specified
	 *         properties set.
	 * @throws DataSourceException
	 *             The requested data source type is not known, or there was a
	 *             problem while initializing its contents from the XML element.
	 */
	public static IDataSource createDataSource(XMLTestSuite xmlTestSuite,
			XMLTestCase xmlTestCase) throws DataSourceException {
		XMLDataSource xmlDataSource = null;
		if (xmlTestCase.isSetSetUp()
				&& xmlTestCase.getSetUp().isSetDataSource()) {
			xmlDataSource = xmlTestCase.getSetUp().getDataSource();
		} else if (xmlTestSuite.isSetSetUp()
				&& xmlTestSuite.getSetUp().isSetDataSource()) {
			xmlDataSource = xmlTestSuite.getSetUp().getDataSource();
		}

		if (xmlDataSource != null) {
			final String type = xmlDataSource.getType();
			final InputStream istream = getStreamForDataSource(xmlDataSource);
			Map<String, String> properties = createPropertyMap(xmlDataSource
					.getPropertyList());
			return createDataSource(type, istream, properties);
		} else {
			return null;
		}
	}

	/**
	 * Creates and initializes an IDataSource instance.
	 * 
	 * @param type
	 *            Type of the data source instance, according to the extension
	 *            registry in <code>conf/extensions.xml</code>.
	 * @param istream
	 *            InputStream with the contents to be loaded, as created by
	 *            {@link #getStreamForDataSource(String, String)} or its
	 *            wrappers.
	 * @param properties
	 *            Key-value map for the properties to be set on the data source.
	 * @return IDataSource instance with its contents initialized from the
	 *         InputStream and all properties set.
	 * @throws DataSourceException
	 *             There was a problem while creating the data source instance,
	 *             loading its contents or setting a property.
	 */
	public static IDataSource createDataSource(final String type,
			final InputStream istream, Map<String, String> properties)
			throws DataSourceException {
		IDataSource dataSource = null;

		try {
			dataSource = ExtensionRegistry.createNewDataSourceForType(type);
		} catch (SpecificationException e) {
			throw new DataSourceException(
					"Could not create data source instance for type " + type, e);
		}

		for (Entry<String, String> prop : properties.entrySet()) {
			dataSource.setProperty(prop.getKey(), prop.getValue());
		}

		// Contents should be loaded after setting the properties, as these might
		// have information that is required to correctly interpret the input
		// stream, such as text encoding, for instance.
		dataSource.loadFromStream(istream);

		return dataSource;
	}

	/**
	 * Returns an InputStream for the contents referenced in the data source. It
	 * is a convenience wrapper for
	 * {@link #getStreamForDataSource(String, String)}.
	 * 
	 * @param xmlDataSource
	 *            XMLBeans object for the <dataSource> element.
	 * @throws DataSourceException
	 *             Unknown content source type.
	 */
	public static InputStream getStreamForDataSource(XMLDataSource xmlDataSource)
			throws DataSourceException {
		return getStreamForDataSource(xmlDataSource.getContents(),
				xmlDataSource.getSrc());
	}

	/**
	 * Returns an InputStream for the contents referenced in the data source.
	 * Currently, only inline content through the nested <contents> element is
	 * supported.
	 * 
	 * TODO paths, file:// and http:// URLs.
	 * 
	 * @param contents
	 *            Inline contents of the data source. If <code>null</code>, the
	 *            data source has no inline contents.
	 * @param externalReference
	 *            Value of the external reference. If <code>null</code> the data
	 *            source has no external reference.
	 * @throws DataSourceException
	 *             Either the external reference is of an unknown type, or
	 *             neither an external reference nor inline contents are
	 *             available.
	 */
	public static InputStream getStreamForDataSource(String contents,
			String externalReference) throws DataSourceException {
		if (contents != null) {
			if (externalReference != null) {
				throw new DataSourceException("Inline content and external " +
						"references cannot be used at the same time in a " +
						"data source: it is ambiguous");
			}
			return new ByteArrayInputStream(contents.getBytes());
		} else if (externalReference != null) {
			throw new DataSourceException(String.format(
					"Unknown external reference '%s'", externalReference));
		} else {
			throw new DataSourceException(
					"No inline contents and no external reference are available");
		}
	}

	/************* PRIVATE METHODS ****************/

	private static Map<String, String> createPropertyMap(
			List<XMLProperty> propertyList) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (XMLProperty prop : propertyList) {
			map.put(prop.getName(), prop.getStringValue());
		}
		return map;
	}
}