/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.osgi.tests.bundles;

import java.net.URL;
import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.osgi.tests.OSGiTestsActivator;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.ExportedPackage;

public class PackageAdminBundleTests extends AbstractBundleTests {
	public class TestListener implements SynchronousBundleListener {
		ArrayList events = new ArrayList();

		public synchronized void bundleChanged(BundleEvent event) {
			events.add(event);
		}

		public synchronized BundleEvent[] getEvents() {
			try {
				return (BundleEvent[]) events.toArray(new BundleEvent[0]);
			} finally {
				events.clear();
			}
		}
	}

	public static Test suite() {
		return new TestSuite(PackageAdminBundleTests.class);
	}

	public void testBundleEvents01() throws Exception {
		Bundle chainTest = installer.installBundle("chain.test"); //$NON-NLS-1$
		Bundle chainTestA = installer.installBundle("chain.test.a"); //$NON-NLS-1$
		Bundle chainTestB = installer.installBundle("chain.test.b"); //$NON-NLS-1$
		Bundle chainTestC = installer.installBundle("chain.test.c"); //$NON-NLS-1$
		Bundle chainTestD = installer.installBundle("chain.test.d"); //$NON-NLS-1$
		Bundle[] resolveBundles = new Bundle[] {chainTestC, chainTestA, chainTestB, chainTest, chainTestD};
		Bundle[] dependencyOrder = new Bundle[] {chainTest, chainTestA, chainTestB, chainTestC, chainTestD};
		TestListener testListener = new TestListener();
		OSGiTestsActivator.getContext().addBundleListener(testListener);
		try {
			installer.resolveBundles(resolveBundles);
			BundleEvent[] events = testListener.getEvents();
			assertEquals("Event count", 10, events.length); //$NON-NLS-1$
			int j = 0;
			for (int i = dependencyOrder.length - 1; i >= 0; i--, j++) {
				assertTrue("Resolved Event Bundle: " + dependencyOrder[i].getSymbolicName(), dependencyOrder[i] == events[j].getBundle()); //$NON-NLS-1$
				assertEquals("Expecting Resolved event", BundleEvent.RESOLVED, events[j].getType()); //$NON-NLS-1$
			}
			j = 5;
			for (int i = dependencyOrder.length - 1; i >= 0; i--, j++) {
				assertTrue("Lazy Starting Bundle: " + dependencyOrder[i].getSymbolicName(), dependencyOrder[i] == events[j].getBundle()); //$NON-NLS-1$
				assertEquals("Expecting Lazy Starting event", BundleEvent.LAZY_ACTIVATION, events[j].getType()); //$NON-NLS-1$
			}
		} finally {
			OSGiTestsActivator.getContext().removeBundleListener(testListener);
		}
	}

	public void testBundleEvents02() throws Exception {
		Bundle chainTest = installer.installBundle("chain.test"); //$NON-NLS-1$
		Bundle chainTestA = installer.installBundle("chain.test.a"); //$NON-NLS-1$
		Bundle chainTestB = installer.installBundle("chain.test.b"); //$NON-NLS-1$
		Bundle chainTestC = installer.installBundle("chain.test.c"); //$NON-NLS-1$
		Bundle chainTestD = installer.installBundle("chain.test.d"); //$NON-NLS-1$
		Bundle[] resolveBundles = new Bundle[] {chainTestC, chainTestA, chainTestB, chainTest, chainTestD};
		Bundle[] dependencyOrder = new Bundle[] {chainTest, chainTestA, chainTestB, chainTestC, chainTestD};
		TestListener testListener = new TestListener();
		OSGiTestsActivator.getContext().addBundleListener(testListener);
		try {
			installer.resolveBundles(resolveBundles);
			BundleEvent[] events = testListener.getEvents();
			// throw away the events.  This was already tested
			installer.refreshPackages(resolveBundles);
			events = testListener.getEvents();
			assertEquals("Event count", 25, events.length); //$NON-NLS-1$
			int j = 0;
			for (int i = 0; i < dependencyOrder.length; i++, j += 2) {
				assertTrue("Stopping Event Bundle: " + dependencyOrder[i].getSymbolicName(), dependencyOrder[i] == events[j].getBundle()); //$NON-NLS-1$
				assertEquals("Expecting Stopping event", BundleEvent.STOPPING, events[j].getType()); //$NON-NLS-1$
				assertTrue("Stopped Event Bundle: " + dependencyOrder[i].getSymbolicName(), dependencyOrder[i] == events[j + 1].getBundle()); //$NON-NLS-1$
				assertEquals("Expecting Stopping event", BundleEvent.STOPPED, events[j + 1].getType()); //$NON-NLS-1$
			}
			j = 10;
			for (int i = 0; i < dependencyOrder.length; i++, j++) {
				assertTrue("Unresolved Event Bundle: " + dependencyOrder[i].getSymbolicName(), dependencyOrder[i] == events[j].getBundle()); //$NON-NLS-1$
				assertEquals("Expecting Unresolved event", BundleEvent.UNRESOLVED, events[j].getType()); //$NON-NLS-1$
			}
			j = 15;
			for (int i = dependencyOrder.length - 1; i >= 0; i--, j++) {
				assertTrue("Resolved Event Bundle: " + dependencyOrder[i].getSymbolicName(), dependencyOrder[i] == events[j].getBundle()); //$NON-NLS-1$
				assertEquals("Expecting Resolved event", BundleEvent.RESOLVED, events[j].getType()); //$NON-NLS-1$
			}
			j = 20;
			for (int i = dependencyOrder.length - 1; i >= 0; i--, j++) {
				assertTrue("Lazy Starting Event Bundle: " + dependencyOrder[i].getSymbolicName(), dependencyOrder[i] == events[j].getBundle()); //$NON-NLS-1$
				assertEquals("Expecting Lazy Starting event", BundleEvent.LAZY_ACTIVATION, events[j].getType()); //$NON-NLS-1$
			}

		} finally {
			OSGiTestsActivator.getContext().removeBundleListener(testListener);
		}
	}

	public void testBug259903() throws Exception {
		Bundle bug259903a = installer.installBundle("test.bug259903.a"); //$NON-NLS-1$
		Bundle bug259903b = installer.installBundle("test.bug259903.b"); //$NON-NLS-1$
		Bundle bug259903c = installer.installBundle("test.bug259903.c"); //$NON-NLS-1$

		try {
			installer.resolveBundles(new Bundle[] {bug259903a, bug259903b, bug259903c});
			bug259903c.start();
			bug259903a.uninstall();
			installer.installBundle("test.bug259903.a.update"); //$NON-NLS-1$
			installer.refreshPackages(new Bundle[] {bug259903a});
			Object[] expectedEvents = new Object[] {new BundleEvent(BundleEvent.STOPPED, bug259903c)};
			Object[] actualEvents = simpleResults.getResults(expectedEvents.length);
			compareResults(expectedEvents, actualEvents);
		} catch (Exception e) {
			fail("Unexpected exception", e); //$NON-NLS-1$
		}
	}

	public void testBug287636() throws Exception {
		Bundle bug287636a = installer.installBundle("test.bug287636.a1"); //$NON-NLS-1$
		Bundle bug287636b = installer.installBundle("test.bug287636.b"); //$NON-NLS-1$
		try {
			bug287636a.start();
			bug287636b.start();
			assertTrue("Bundles are not resolved", installer.resolveBundles(new Bundle[] {bug287636a, bug287636b})); //$NON-NLS-1$
			ExportedPackage ep = installer.getPackageAdmin().getExportedPackage("test.bug287636.a"); //$NON-NLS-1$
			assertNotNull("Could not find exported package", ep); //$NON-NLS-1$
			assertEquals("Wrong version", new Version(1, 0, 0), ep.getVersion()); //$NON-NLS-1$
			// update bundle to export new 1.1.0 version of the pacakge
			String updateLocation = installer.getBundleLocation("test.bug287636.a2"); //$NON-NLS-1$
			bug287636a.update(new URL(updateLocation).openStream());
			bug287636b.update();
			updateLocation = installer.getBundleLocation("test.bug287636.a1"); //$NON-NLS-1$
			bug287636a.update(new URL(updateLocation).openStream());
			bug287636b.update();
			updateLocation = installer.getBundleLocation("test.bug287636.a2"); //$NON-NLS-1$
			bug287636a.update(new URL(updateLocation).openStream());
			bug287636b.update();
			installer.refreshPackages(null);
			ep = installer.getPackageAdmin().getExportedPackage("test.bug287636.a"); //$NON-NLS-1$

			assertNotNull("Could not find exported package", ep); //$NON-NLS-1$
			assertEquals("Wrong version", new Version(1, 1, 0), ep.getVersion()); //$NON-NLS-1$
			ExportedPackage eps[] = installer.getPackageAdmin().getExportedPackages("test.bug287636.a"); //$NON-NLS-1$
			assertNotNull("Could not find exported package", eps); //$NON-NLS-1$
			assertEquals("Wrong number of exports", 1, eps.length); //$NON-NLS-1$
			assertEquals("Wrong version", new Version(1, 1, 0), eps[0].getVersion()); //$NON-NLS-1$
			eps = installer.getPackageAdmin().getExportedPackages(bug287636a);
			assertNotNull("Could not find exported package", eps); //$NON-NLS-1$
			assertEquals("Wrong number of exports", 1, eps.length); //$NON-NLS-1$
			assertEquals("Wrong version", new Version(1, 1, 0), eps[0].getVersion()); //$NON-NLS-1$
		} catch (Exception e) {
			fail("Unexpected exception", e); //$NON-NLS-1$
		}
	}

	public void testBug289719() throws Exception {
		Bundle bug259903a = installer.installBundle("test.bug259903.a"); //$NON-NLS-1$
		Bundle bug259903b = installer.installBundle("test.bug259903.b"); //$NON-NLS-1$
		Bundle bug259903c = installer.installBundle("test.bug259903.c"); //$NON-NLS-1$
		TestListener testListener = new TestListener();
		OSGiTestsActivator.getContext().addBundleListener(testListener);
		try {
			installer.resolveBundles(new Bundle[] {bug259903a, bug259903b, bug259903c});
			bug259903a.start();
			bug259903b.start();
			bug259903c.start();
			installer.getStartLevel().setBundleStartLevel(bug259903c, 2);
			installer.getStartLevel().setBundleStartLevel(bug259903b, 3);
			installer.getStartLevel().setBundleStartLevel(bug259903a, 4);

			testListener.getEvents(); // clear events
			installer.refreshPackages(new Bundle[] {bug259903a});
			Object[] expectedEvents = new Object[] {new BundleEvent(BundleEvent.STOPPING, bug259903a), new BundleEvent(BundleEvent.STOPPED, bug259903a), new BundleEvent(BundleEvent.STOPPING, bug259903b), new BundleEvent(BundleEvent.STOPPED, bug259903b), new BundleEvent(BundleEvent.STOPPING, bug259903c), new BundleEvent(BundleEvent.STOPPED, bug259903c), new BundleEvent(BundleEvent.UNRESOLVED, bug259903a), new BundleEvent(BundleEvent.UNRESOLVED, bug259903b), new BundleEvent(BundleEvent.UNRESOLVED, bug259903c), new BundleEvent(BundleEvent.RESOLVED, bug259903c), new BundleEvent(BundleEvent.RESOLVED, bug259903b), new BundleEvent(BundleEvent.RESOLVED, bug259903a), new BundleEvent(BundleEvent.STARTING, bug259903c), new BundleEvent(BundleEvent.STARTED, bug259903c),
					new BundleEvent(BundleEvent.STARTING, bug259903b), new BundleEvent(BundleEvent.STARTED, bug259903b), new BundleEvent(BundleEvent.STARTING, bug259903a), new BundleEvent(BundleEvent.STARTED, bug259903a),};
			Object[] actualEvents = testListener.getEvents();
			compareResults(expectedEvents, actualEvents);

			installer.getStartLevel().setBundleStartLevel(bug259903c, 4);
			installer.getStartLevel().setBundleStartLevel(bug259903b, 4);
			installer.getStartLevel().setBundleStartLevel(bug259903a, 4);
			installer.refreshPackages(new Bundle[] {bug259903a});
			expectedEvents = new Object[] {new BundleEvent(BundleEvent.STOPPING, bug259903c), new BundleEvent(BundleEvent.STOPPED, bug259903c), new BundleEvent(BundleEvent.STOPPING, bug259903b), new BundleEvent(BundleEvent.STOPPED, bug259903b), new BundleEvent(BundleEvent.STOPPING, bug259903a), new BundleEvent(BundleEvent.STOPPED, bug259903a), new BundleEvent(BundleEvent.UNRESOLVED, bug259903c), new BundleEvent(BundleEvent.UNRESOLVED, bug259903b), new BundleEvent(BundleEvent.UNRESOLVED, bug259903a), new BundleEvent(BundleEvent.RESOLVED, bug259903a), new BundleEvent(BundleEvent.RESOLVED, bug259903b), new BundleEvent(BundleEvent.RESOLVED, bug259903c), new BundleEvent(BundleEvent.STARTING, bug259903a), new BundleEvent(BundleEvent.STARTED, bug259903a),
					new BundleEvent(BundleEvent.STARTING, bug259903b), new BundleEvent(BundleEvent.STARTED, bug259903b), new BundleEvent(BundleEvent.STARTING, bug259903c), new BundleEvent(BundleEvent.STARTED, bug259903c),};
			actualEvents = testListener.getEvents();
			compareResults(expectedEvents, actualEvents);

		} catch (Exception e) {
			fail("Unexpected exception", e); //$NON-NLS-1$
		} finally {
			OSGiTestsActivator.getContext().removeBundleListener(testListener);
		}
	}
}