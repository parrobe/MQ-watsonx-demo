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

// Assisted by WCA@IBM
// Latest GenAI contribution: ibm/granite-20b-code-instruct-v2
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class is responsible for connecting to IBM Watsonx to convert SWIFT
 * MT103 messages into a summary.
 */
public class Watsonxi {

	String accessToken;
	String apikey;
	String projectID;

	/* Controls for the AI prompt. */
	private static final String PROMPT = "The following is a SWIFT MT103 message. Summarize it telling me the name of the sending account holder,"
			+ " receiving account holder, money sent, transaction date and currency. Do not provide additional information. "
			+ "\n\nInput: {1:F01BANKBEBBAXXX1234567890}{2:O1031130050901BANKBEBBAXXX12345678900509011311N}{3:{108:MT103}}{4:"
			+ "\n:20:REFERENCE12345\n:23B:CRED\n:32A:230501EUR123456,78\n:50A:/12345678901234567890MR. JOHN DOE\n:59:/23456789012345678901MS."
			+
			" JANE SMITH\n:70:INVOICE 987654\n:71A:SHA\n-}\nOutput: On 01/05/23, MR. JOHN DOE with account number 12345678901234567890 sent 123456.78 "
			+
			"EUR to MS. JANE SMITH with account number 23456789012345678901. The reference was REFERENCE12345.\n\nInput: {1:F01BANKNICKZCDD7594000006}"
			+ "{2:I103BANKROBEXECFN1020}{3:{113:SEPA}{108:YCBK8YG4Z5IJ7E2T}}{4\n:20:GEOTOROB6\n:23B:CRED\n:32A:240916GBP429,00\n:50A:/51487815622711023840 "
			+ "George Lucas\n:59:/45557308524085670622 Rob Parker\n:70:INVOICE 000006\n:71A:SHA\n-}\nOutput: ON 16/09/24, George Lucas with account number "
			+ "51487815622711023840 sent 429.00 GBP to Rob Parker with account number 45557308524085670622. The reference was GEOTOROB6.\n\nInput: "
			+ "{1:F01BANKGRAHZBEB3739000008}{2:I103BANKNICKXCDDN1020}{3:{113:SEPA}{108:1B20UFNXCLS7CC7U}}{4\n:20:BILTOROB8\n:23B:CRED\n:32A:240712GBP152,00"
			+ "\n:50A:/82843786428070666022 Bill Gates\n:59:/22622140566055075773 Rob Parker\n:70:INVOICE 000008\n:71A:SHA\n-}\n{5:{CHK:a524492dc9399c33e4bebb6b457ccd56}}"
			+ "\nOutput: ON 12/07/24, Bill Gates with account number 82843786428070666022 sent 152.00 GBP to Rob Parker with account number 22622140566055075773. "
			+ "The reference was BILTOROB8.\n\nInput: ";

	private static final String AI_MODEL = "ibm/granite-13b-chat-v2";
	private static final String DECODE_METHOD = "greedy";
	private static final int MAX_NEW_TOKENS = 200;
	private static final int MIN_NEW_TOKENS = 0;
	private static final int REPETITION_PENALTY = 1;
	private static final String AI_URL = "https://eu-gb.ml.cloud.ibm.com/ml/v1/text/generation?version=2023-05-29";

	public Watsonxi(String apikey, String projectID) throws Exception {
		this.apikey = apikey;
		this.projectID = projectID;
		apiToAccesToken();
	}

	/**
	 * This function converts the supplied apikey into an access token that can be
	 * used to contact the IBM Watsonx service.
	 * 
	 * @throws Exception
	 */
	// Assisted by WCA@IBM
	// Latest GenAI contribution: ibm/granite-20b-code-instruct-v2
	public void apiToAccesToken() throws Exception {
		String url = "https://iam.cloud.ibm.com/identity/token";
		String data = "grant_type=urn:ibm:params:oauth:grant-type:apikey&apikey=" + apikey;

		// Create connection
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// Add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		// Send post request
		con.setDoOutput(true);
		con.getOutputStream().write(data.getBytes("UTF-8"));

		int responseCode = con.getResponseCode();
		if (responseCode != 200) {
			throw new Exception("Non-200 HTTP return code attempting to convert api key to access token. [" + responseCode +"]" + con.getResponseMessage());
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Extract the access token from the JSON Object.
		JSONObject jsonobj = new JSONObject(response.toString());
		accessToken = jsonobj.getString("access_token");

		if (accessToken == null || accessToken.equals("")) {
			throw new Exception("Unable to get access token from JSON response");
		}
	}

	/**
	 * This function takes the supplied SWIFT MT103 message, wraps it in a prompt
	 * and makes a request to the IBM Watsonx service to execute the prompt against
	 * the ibm/granite-13b-chat-v2 model. It then takes the response and trims it to
	 * just the first line.
	 * 
	 * @param swiftMessage The SWIFT MT103 message to send to IBM Watsonx.
	 * @return The one line summary response from IBM Watsonx.
	 * @throws Exception
	 */
	public String sendToWX(String swiftMessage) throws Exception {
		// Assisted by WCA@IBM
		// Latest GenAI contribution: ibm/granite-20b-code-instruct-v2
		URL url = new URL(AI_URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Authorization", "Bearer " + accessToken);

		String json = "{ \"input\": \"" + PROMPT + swiftMessage + "\nOutput:"
				+ "\", \"parameters\": { \"decoding_method\": \"" + DECODE_METHOD
				+ "\", \"max_new_tokens\": " + MAX_NEW_TOKENS + ", \"min_new_tokens\": " + MIN_NEW_TOKENS
				+ ", \"stop_sequences\": [], \"repetition_penalty\": " + REPETITION_PENALTY + " }, \"model_id\": \""
				+ AI_MODEL
				+ "\", \"project_id\": \"" + projectID
				+ "\", \"moderations\": { \"hap\": { \"input\": { \"enabled\": true, \"threshold\": 0.5, \"mask\": { \"remove_entity_value\": true } }, \"output\": { \"enabled\": true, \"threshold\": 0.5, \"mask\": { \"remove_entity_value\": true } } } } }";

		// The SWIFT MT103 messages have return carriage characters in them and new line
		// characters that need replacing to ensure the request doesn't fail.
		String tidiedjson = json.replaceAll("\r", "");
		tidiedjson = tidiedjson.replaceAll("\n", "\\\\n");

		connection.setDoOutput(true);
		connection.getOutputStream().write(tidiedjson.getBytes("UTF-8"));

		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			String back = connection.getResponseMessage();
			throw new Exception("Non-200 HTTP return code attempting to summarize message. [" + responseCode + "] " + back);
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Extract the access token from the JSON Object.
		JSONObject jsonobj = new JSONObject(response.toString());
		JSONObject resultsObj = jsonobj.getJSONArray("results").getJSONObject(0);
		String resultstr = resultsObj.getString("generated_text");

		return removeAfterNewLine(resultstr);
	}

	/**
	 * A small function that removes all text after the first newline character.
	 * 
	 * @param input The string to trim.
	 * @return The input with all characters after the first newline character
	 *         removed.
	 */
	// Assisted by WCA@IBM
	// Latest GenAI contribution: ibm/granite-20b-code-instruct-v2
	public String removeAfterNewLine(String input) {
		int index = input.indexOf("\n");
		if (index == -1) {
			return input;
		} else {
			return input.substring(0, index);
		}
	}
}
