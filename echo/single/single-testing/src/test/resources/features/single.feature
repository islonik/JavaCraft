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
    Then close the connection to the client 'Nikita'
    Then close the connection to the client 'Ava'
    Then close the connection to the client 'Alyssa'
