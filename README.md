# MQ-Watsonx-bridge
A very simple bridge program that consumes messages from IBM MQ and sends them to a Watsonx prompt lab.
It was created for a demo published in the blog post [Link TBC](#copyright).

It could be adapted for other uses.

Currently it connects to a queue manager called `QM1` running on `localhost:1414` via channel `IN` and consumes messages from the queue `STREAMED`. It wraps them in a prompt:
```
The following is a SWIFT MT103 message. Summarize it telling me the name of the sending account holder, receiving account holder, money sent, transaction date and currency. Do not provide additional information. \n\n<...SNIP 3 examples...>\n\nInput: <message string> \nOutput:
```
and then sends it to the IBM watsonx.ai prompt lab to be processed with the model `ibm/granite-13b-chat-v2`.
The response is trimmed to only the first line and printed to standard out.

The program will loop until the enter key is pressed.

## Building the demo program
To build the java program you must have access to the IBM MQ java libraries. It is then built using:
```
$ cd src\watsonxmq
$ javac *.java
```

## IBM MQ configuration
The demo program makes an assumption about the IBM MQ queue manager configuration. It will connect to a queue manager running on `localhost:1414` using channel `IN`. It will not provide a TLS connection or user credentials. Once connected it will access the following queue. `STREAMED`.

## Demo program usage
The demo program is ran by the following command, you must have access to the IBM MQ java libraries:
```
$ cd src 
$ java -cp ".;<path to IBM MQ install>\java\lib\com.ibm.mq.allclient.jar" watsonxmq.Main
```

## Example output
Unfortunately i ran out of tokens have have not captured output. I will return next month once my tokens allowance resets.


## Health Warning
These programs are provided as-is with no guarantees of support or updates. There are
also no guarantees of compatibility with any future versions of IBM MQ of IBM Watsonx.

## Issues
For feedback and issues relating specifically to this package, please use the [GitHub issue tracker](https://github.com/parrobe/MQ-watsonx-demo/issues).

## Copyright

Copyright Rob Parker 2024
