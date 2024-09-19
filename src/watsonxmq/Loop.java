/*
Copyright (c) Rob Parker 2024

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at:

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 Contributors:
   Rob Parker - Initial Contribution
*/
package watsonxmq;

import watsonxmq.MQI;

/**
 * This class contains the main loop responsible for contacting both IBM MQ and
 * IBM Watsonx.
 */
public class Loop implements Runnable {

	private static final int MAX_FAILS = 0;

	private MQI mqi;
	private String q;
	Watsonxi wx;

	public Loop(MQI mqi, Watsonxi wx, String q) {
		this.mqi = mqi;
		this.q = q;
		this.wx = wx;
	}

	/**
	 * This is the main execution loop designed to be started as part of a Thread.
	 * It will perform the following actions in a loop until it is signalled to stop
	 * or encounters too many errors:
	 * 1. Receive a message from the destination queue.
	 * 2. Call to IBM Watsonx with the message written into a prompt.
	 * 3. Print out the first line of the response from IBM Watsonx to standard out.
	 */
	@Override
	public void run() {
		int failC = 0;
		active = true;
		while (stop == false) {
			try {
				// We connect to and get a message
				String swiftmessage = mqi.receiveMessage(q);
				if (swiftmessage == null || swiftmessage.equals("")) {
					// no message in timeout so loop
					continue;
				}

				// Send the message to watsonx.
				String summary = wx.sendToWX(swiftmessage);

				if (swiftmessage == null || swiftmessage.equals("")) {
					// no summary, mark a failure and continue;
					failC++;
					if (failC > MAX_FAILS) {
						System.err.println("Failed too many times. Quitting");
						stop = true;
					}
					continue;
				}

				// Output the result summary.
				System.out.println(summary);

			} catch (Exception e) {
				failC++;
				if (failC > MAX_FAILS) {
					System.err.println("Failed too many times. Quitting");
					stop = true;
				}
				e.printStackTrace();
				continue;
			}
		}
		active = false;
	}

	private boolean stop = false;
	private boolean active = false;

	/**
	 * Signal the exection loop to stop gracefully.
	 */
	public void signalStop() {
		stop = true;
	}

	/**
	 * Returns whether the execution loop for this class is curentlying running.
	 * 
	 * @return True if the execution loop is active.
	 */
	public boolean isActive() {
		return active;
	}

}
