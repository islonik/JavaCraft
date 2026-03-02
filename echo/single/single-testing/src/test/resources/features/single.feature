Feature: testing Single Thread Server

  Scenario: test a single connection
    Given socket server started up on port = '8040'
    When create a new client 'Nikita' for the server with the port = '8040'
    When use the client 'Nikita' to send 'stats' message and get '1 simultaneously connected clients.' response
    Then close the connection to the client 'Nikita'

  Scenario: test several connections
    Given socket server started up on port = '8045'
    When create a new client 'Nikita' for the server with the port = '8045'
    When use the client 'Nikita' to send 'stats' message and get '1 simultaneously connected clients.' response
    When create a new client 'Ava' for the server with the port = '8045'
    When use the client 'Ava' to send 'stats' message and get '2 simultaneously connected clients.' response
    When create a new client 'Alyssa' for the server with the port = '8045'
    When use the client 'Alyssa' to send 'stats' message and get '3 simultaneously connected clients.' response
    When use the client 'Nikita' to send 'Hello World!' message and get "Did you say 'Hello World!'?" response
    When use the client 'Ava' to send 'Java 21' message and get "Did you say 'Java 21'?" response
    When use the client 'Alyssa' to send 'mimimi' message and get "Did you say 'mimimi'?" response
    Then close the connection to the client 'Nikita'
    Then close the connection to the client 'Ava'
    Then close the connection to the client 'Alyssa'

  Scenario: test echo response
    Given socket server started up on port = '8050'
    When create a new client 'Echo' for the server with the port = '8050'
    When use the client 'Echo' to send 'hello world' message and get "Did you say 'hello world'?" response
    Then close the connection to the client 'Echo'

  Scenario: test multiple round trips on the same connection
    Given socket server started up on port = '8055'
    When create a new client 'Repeat' for the server with the port = '8055'
    When use the client 'Repeat' to send 'first' message and get "Did you say 'first'?" response
    When use the client 'Repeat' to send 'StAtS' message and get '1 simultaneously connected clients.' response
    When use the client 'Repeat' to send 'second' message and get "Did you say 'second'?" response
    Then close the connection to the client 'Repeat'

  Scenario: test connection count after one client disconnects
    Given socket server started up on port = '8060'
    When create a new client 'Primary' for the server with the port = '8060'
    When create a new client 'Secondary' for the server with the port = '8060'
    When use the client 'Primary' to send 'stats' message and get '2 simultaneously connected clients.' response
    When use the client 'Secondary' to send 'BYE' message and get 'Have a good day!' response
    When use the client 'Primary' to send 'stats' message and get '1 simultaneously connected clients.' response
    Then close the connection to the client 'Primary'

  Scenario: test empty message from line delimiters only
    Given socket server started up on port = '8065'
    When create a new client 'Empty' for the server with the port = '8065'
    When use the client 'Empty' to send escaped message '\r\n' and get escaped response 'Please type something.'
    Then close the connection to the client 'Empty'

  Scenario: test multiline echo preserves embedded line breaks
    Given socket server started up on port = '8070'
    When create a new client 'Multiline' for the server with the port = '8070'
    When use the client 'Multiline' to send escaped message 'hello\nworld\r\n' and get escaped response "Did you say 'hello\nworld'?"
    Then close the connection to the client 'Multiline'

  Scenario: test client can continue after an empty message
    Given socket server started up on port = '8075'
    When create a new client 'Recover' for the server with the port = '8075'
    When use the client 'Recover' to send escaped message '\r\n' and get escaped response 'Please type something.'
    When use the client 'Recover' to send 'stats' message and get '1 simultaneously connected clients.' response
    When use the client 'Recover' to send 'still here' message and get "Did you say 'still here'?" response
    Then close the connection to the client 'Recover'

  Scenario: test echo preserves surrounding spaces while trimming line delimiters
    Given socket server started up on port = '8080'
    When create a new client 'Spaces' for the server with the port = '8080'
    When use the client 'Spaces' to send escaped message '  keep surrounding spaces  \r\n' and get escaped response "Did you say '  keep surrounding spaces  '?"
    Then close the connection to the client 'Spaces'

  Scenario: test connection count recovers after a client says bye
    Given socket server started up on port = '8085'
    When create a new client 'First' for the server with the port = '8085'
    When create a new client 'Second' for the server with the port = '8085'
    When use the client 'First' to send 'stats' message and get '2 simultaneously connected clients.' response
    When use the client 'Second' to send 'bye' message and get 'Have a good day!' response
    When create a new client 'Replacement' for the server with the port = '8085'
    When use the client 'Replacement' to send 'stats' message and get '2 simultaneously connected clients.' response
    Then close the connection to the client 'First'
    Then close the connection to the client 'Replacement'

  Scenario: test echo preserves tabs and backslashes
    Given socket server started up on port = '8090'
    When create a new client 'Escapes' for the server with the port = '8090'
    When use the client 'Escapes' to send escaped message 'left\tright\\tail\r\n' and get escaped response "Did you say 'left\tright\\tail'?"
    Then close the connection to the client 'Escapes'
