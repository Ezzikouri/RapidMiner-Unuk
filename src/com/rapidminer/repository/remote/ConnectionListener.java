package com.rapidminer.repository.remote;

/** Listens to the connections of a RapidAnalytics repository.
 * 
 * @see RemoteRepository#isConnected()
 * @author Simon Fischer
 *
 */
public interface ConnectionListener {

	/** Called after disconnection. Guarantees that {@link RemoteRepository#isConnected()} returns false. */
	public void connectionLost(RemoteRepository rapidAnalytics);
	
	/** Called after connection was established. Guarantees that {@link RemoteRepository#isConnected()} returns true. */
	public void connectionEstablished(RemoteRepository rapidAnalytics);
}
