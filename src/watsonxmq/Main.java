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

import java.util.Scanner;

/**
 * This class is the main entry point for this program. It handles setting up
 * the necessary objects and threads before starting.
 */
public class Main {
	/**
	 * Main entry point. Expects 2 arguments which are the apikey used for
	 * connecting
	 * to IBM Watsonx and the Project ID to use.
	 * 
	 * @param args Program arguments.
	 */
	public static void main(String[] args) {
		Main m = new Main();
		if (args.length < 1) {
			System.err.println("You must supply an apikey!");
			System.exit(1);
		}
		if (args.length < 2) {
			System.err.println("You must supply a project ID!");
			System.exit(1);
		}
		try {
			m.go(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main execution function. This function will do the following:
	 * 1. Create a connection to a queue manager using static values.
	 * 2. Creates a Watsonx class which will convert the apikey into an access
	 * token.
	 * 3. Cretes the main execution loop using a static queue name of "STREAMED".
	 * 4. Starts the main execution loop in a thread.
	 * 5. Pause waiting for a enter key to signal a stop.
	 * 6. Request all threads started end gracefully.
	 * 7. Wait until all threads have ended.
	 * 
	 * This functionality uses harcoded values for the queue manager and queue. In
	 * the future these should be changed to be read from a
	 * configuration file.
	 * The program assumes a queue manager locally called "QM1" with a channel of
	 * "IN" and port of "1414".
	 * The queue this program consumes from is "STREAMED".
	 * 
	 * @throws Exception
	 */
	public void go(String apikey, String pid) throws Exception {
		MQI mq = new MQI("QM1", "localhost", 1414, "IN");
		Watsonxi wx = new Watsonxi(apikey, pid);
		mq.createConnection();

		Loop l = new Loop(mq, wx, "STREAMED");

		Thread t = new Thread(l);
		t.start();

		Scanner scanner = new Scanner(System.in);
		System.err.println("Press ENTER to stop...");
		scanner.nextLine();
		scanner.close();

		System.err.println("Ending all threads");
		l.signalStop();
		while (l.isActive())
			;
		System.err.println("Threads ended. Shutting down.");
	}

	public void obtainQMConnDetailsFromConfig() {
		// TODO we should add capability to get queue manager details from a file.
	}
}
