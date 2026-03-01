Feature: testing Netty Server

  Scenario: test a single connection
    Given socket server started up on port = '8030'
    When create a new client 'Nikita' for the server with the port = '8030'
    When use the client 'Nikita' to send 'stats' message and get 'Simultaneously connected clients: 1' response
    Then close the connection to the client 'Nikita'

  Scenario: test several connections
    Given socket server started up on port = '8035'
    When create a new client 'Nikita' for the server with the port = '8035'
    When use the client 'Nikita' to send 'stats' message and get 'Simultaneously connected clients: 1' response
    When create a new client 'Ava' for the server with the port = '8035'
    When use the client 'Ava' to send 'stats' message and get 'Simultaneously connected clients: 2' response
    When create a new client 'Alyssa' for the server with the port = '8035'
    When use the client 'Alyssa' to send 'stats' message and get 'Simultaneously connected clients: 3' response
    Then close the connection to the client 'Nikita'
    Then close the connection to the client 'Ava'
    Then close the connection to the client 'Alyssa'

  Scenario: test echo response
    Given socket server started up on port = '8038'
    When create a new client 'Echo' for the server with the port = '8038'
    When use the client 'Echo' to send 'hello world' message and get "Did you say 'hello world'?" response
    Then close the connection to the client 'Echo'

  Scenario: test empty message response
    Given socket server started up on port = '8040'
    When create a new client 'Empty' for the server with the port = '8040'
    When use the client 'Empty' to send '' message and get 'Please type something.' response
    Then close the connection to the client 'Empty'
