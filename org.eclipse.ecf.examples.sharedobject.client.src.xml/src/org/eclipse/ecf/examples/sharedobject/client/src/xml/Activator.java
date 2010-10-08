package org.eclipse.ecf.examples.sharedobject.client.src.xml;

import java.io.IOException;


import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.core.sharedobject.ISharedObjectManager;
import org.eclipse.ecf.core.sharedobject.SharedObjectAddException;
import org.eclipse.ecf.examples.sharedobject.objects.IMessageReceiver;
import org.eclipse.ecf.examples.sharedobject.objects.MyMessagingSharedObject;
import org.eclipse.ecf.examples.sharedobject.objects.xstream.XStreamSOMessageSerializer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator
{
	public static final String GENERIC_SERVER_ID = "ecftcp://localhost:4280/mygroup";

	public static final String GENERIC_CLIENT_CONTAINER = "ecf.generic.client";

	public static final String OBJECT_ID = "foo";

	public static final String OBJECT_NAME = "pavel";

	public static final String OBJECT_PROPERTY_ID = "my-id";

	public static final int OBJECT_PROPERTY_A = 2;

	public static final float OBJECT_PROPERTY_B = 2.3f;

	public static final String MESSAGE = "My message";

	private IContainer _client;

	private MyMessagingSharedObject _object;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		createClient();
		connectClient();
		addSharedObjectToClient();
		sleep(1000);
		sendMessage();
	}

	private void createClient() throws Exception
	{
		_client = ContainerFactory.getDefault().createContainer(GENERIC_CLIENT_CONTAINER);
		((ISharedObjectContainer) _client).setSharedObjectMessageSerializer(new XStreamSOMessageSerializer());
	}

	private ISharedObjectManager getClientSOManager()
	{
		return ((ISharedObjectContainer) _client).getSharedObjectManager();
	}

	private void connectClient() throws Exception
	{
		connectClient(_client, createServerID(), null);
	}

	private ID createServerID() throws Exception
	{
		return createNewID(GENERIC_SERVER_ID);
	}

	private void connectClient(IContainer containerToConnect, ID connectID, IConnectContext context)
			throws ContainerConnectException
	{
		containerToConnect.connect(connectID, context);
	}

	private ID createNewID(String id)
	{
		return IDFactory.getDefault().createStringID(id);
	}

	private ID addSharedObjectToClient() throws SharedObjectAddException
	{
		ISharedObjectManager manager = getClientSOManager();
		ID id = manager.addSharedObject(createNewID(OBJECT_ID),
				new MyMessagingSharedObject(OBJECT_NAME, new IMessageReceiver()
					{
						public void handleMessage(ID fromID, Object message)
						{
							System.out.println("received fromId = " + fromID + " message = " + message);
						}
					}), null);
		System.out.println("Added new SharedObject with ID = " + id.getName());

		_object = (MyMessagingSharedObject) manager.getSharedObject(id);

		return id;
	}

	private void sendMessage() throws IOException
	{
		_object.sendMessage(null, MESSAGE);
	}

	private void sleep(int pause)
	{
		try
		{
			Thread.sleep(pause);
		}
		catch (InterruptedException e) {}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		if (_client != null)
			_client.dispose();
	}
}
