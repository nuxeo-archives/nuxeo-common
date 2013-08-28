package org.nuxeo.common.xmap;

import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

public class TestDeferClasses {

	@XObject("contrib")
	public static class Contribution {
		
		@XNode("@component")
		protected Class<?> componentType;
		
		protected Class<?> serviceType;

		@XNodeList(value = "extension", type = Extension[].class, componentType = Extension.class)
		protected Extension[] extensions;

		@XNode("service")
		public void setServiceType(String className) throws ClassNotFoundException {
			serviceType = Class.forName(className);
		}

		public Class<?> getServiceType() {
			return serviceType;
		}
	}

	@XObject("extension")
	public static class Extension {

		@XNode("@extend")
		protected Class<?> extension;
	}

	
	@Test
	public void testGeneric() {
		XValueFactory<String> factory = XValueFactory.STRING;
		Assert.assertEquals(String.class, factory.getType());
	}
	@Test
	public void classFieldsAreNotDeferred() throws IOException {
		XMap xmap = new XMap();
		Contribution contrib = loadContrib(xmap);
		checkNotNull(contrib);
	}

	@Test
	public void classFieldAreDeffered() throws IOException {
		XMap xmap = new XMap();
		xmap.deferClassLoading();
		Contribution contrib = loadContrib(xmap);
		checkNull(contrib);
		xmap.flushDeferred();
		checkNotNull(contrib);
	}

	private void checkNull(Contribution contrib) {
		Assert.assertNull(contrib.componentType);
		Assert.assertNull(contrib.serviceType);
		Assert.assertNotNull(contrib.extensions);
		Assert.assertNull(contrib.extensions[0].extension);
	}

	private void checkNotNull(Contribution contrib) {
		Assert.assertNotNull(contrib.componentType);
		Assert.assertNotNull(contrib.serviceType);
		Assert.assertNotNull(contrib.extensions);
		Assert.assertNotNull(contrib.extensions[0].extension);
	}

	private Contribution loadContrib(XMap xmap) throws IOException {
		xmap.register(Contribution.class);
		xmap.register(Extension.class);
		URL contribURL = TestDeferClasses.class
				.getResource("/class-contrib.xml");
		Object[] contribs = xmap.loadAll(new Context(), contribURL);
		Assert.assertEquals(1, contribs.length);
		return (Contribution) contribs[0];
	}

}
