Feature: Multithreaded server behavior

  Rule: Simple connection flows

    @Sync
    Scenario: [Sync] One client can connect, check the current client count, and disconnect
      Given the multithreaded server is running on port 8100
      When sync client "Nikita" connects on port 8100
      Then client "Nikita" sends "stats" and receives "Simultaneously connected clients: 1"
      And client "Nikita" disconnects with goodbye

    @Async
    Scenario: [Async] One client can connect, check the current client count, and disconnect
      Given the multithreaded server is running on port 8200
      When async client "Nikita" connects on port 8200
      Then client "Nikita" sends "stats" and receives "Simultaneously connected clients: 1"
      And client "Nikita" disconnects with goodbye

    @Sync
    Scenario: [Sync] Several clients see the correct number of connected users
      Given the multithreaded server is running on port 8105
      When sync client "Nikita" connects on port 8105
      And sync client "Ava" connects on port 8105
      And sync client "Alyssa" connects on port 8105
      Then client "Nikita" sends "stats" and receives "Simultaneously connected clients: 3"
      And client "Ava" sends "stats" and receives "Simultaneously connected clients: 3"
      And client "Alyssa" sends "stats" and receives "Simultaneously connected clients: 3"
      And client "Nikita" sends "Hello World!" and receives "Did you say 'Hello World!'?"
      And client "Ava" sends "Java 21" and receives "Did you say 'Java 21'?"
      And client "Alyssa" sends "mimimi" and receives "Did you say 'mimimi'?"
      And client "Nikita" disconnects with goodbye
      And client "Ava" disconnects with goodbye
      And client "Alyssa" disconnects with goodbye

    @Async
    Scenario: [Async] Several clients see the correct number of connected users
      Given the multithreaded server is running on port 8205
      When async client "Nikita" connects on port 8205
      And async client "Ava" connects on port 8205
      And async client "Alyssa" connects on port 8205
      Then client "Nikita" sends "stats" and receives "Simultaneously connected clients: 3"
      And client "Ava" sends "stats" and receives "Simultaneously connected clients: 3"
      And client "Alyssa" sends "stats" and receives "Simultaneously connected clients: 3"
      And client "Nikita" sends "Hello World!" and receives "Did you say 'Hello World!'?"
      And client "Ava" sends "Java 21" and receives "Did you say 'Java 21'?"
      And client "Alyssa" sends "mimimi" and receives "Did you say 'mimimi'?"
      And client "Nikita" disconnects with goodbye
      And client "Ava" disconnects with goodbye
      And client "Alyssa" disconnects with goodbye

    @Sync
    Scenario: [Sync] A client gets an echo response for regular text
      Given the multithreaded server is running on port 8110
      When sync client "Echo" connects on port 8110
      Then client "Echo" sends "hello world" and receives "Did you say 'hello world'?"
      And client "Echo" disconnects with goodbye

    @Async
    Scenario: [Async] A client gets an echo response for regular text
      Given the multithreaded server is running on port 8210
      When async client "Echo" connects on port 8210
      Then client "Echo" sends "hello world" and receives "Did you say 'hello world'?"
      And client "Echo" disconnects with goodbye

    @Sync
    Scenario: [Sync] One client can send several messages on the same connection
      Given the multithreaded server is running on port 8115
      When sync client "Repeat" connects on port 8115
      Then client "Repeat" sends "first" and receives "Did you say 'first'?"
      And client "Repeat" sends "StAtS" and receives "Simultaneously connected clients: 1"
      And client "Repeat" sends "second" and receives "Did you say 'second'?"
      And client "Repeat" disconnects with goodbye

    @Async
    Scenario: [Async] One client can send several messages on the same connection
      Given the multithreaded server is running on port 8215
      When async client "Repeat" connects on port 8215
      Then client "Repeat" sends "first" and receives "Did you say 'first'?"
      And client "Repeat" sends "StAtS" and receives "Simultaneously connected clients: 1"
      And client "Repeat" sends "second" and receives "Did you say 'second'?"
      And client "Repeat" disconnects with goodbye

    @Sync
    Scenario: [Sync] The connected client count drops after one client leaves
      Given the multithreaded server is running on port 8120
      When sync client "Primary" connects on port 8120
      And sync client "Secondary" connects on port 8120
      Then client "Primary" sends "stats" and receives "Simultaneously connected clients: 2"
      And client "Secondary" sends "BYE" and receives "Have a good day!"
      And client "Primary" sends "stats" and receives "Simultaneously connected clients: 1"
      And client "Secondary" socket is closed
      And client "Primary" disconnects with goodbye

    @Async
    Scenario: [Async] The connected client count drops after one client leaves
      Given the multithreaded server is running on port 8220
      When async client "Primary" connects on port 8220
      And async client "Secondary" connects on port 8220
      Then client "Primary" sends "stats" and receives "Simultaneously connected clients: 2"
      And client "Secondary" sends "BYE" and receives "Have a good day!"
      And client "Primary" sends "stats" and receives "Simultaneously connected clients: 1"
      And client "Secondary" socket is closed
      And client "Primary" disconnects with goodbye

    @Sync
    Scenario: [Sync] A client socket closes after goodbye
      Given the multithreaded server is running on port 8122
      When sync client "Closer" connects on port 8122
      Then client "Closer" sends "bye" and receives "Have a good day!"
      And client "Closer" socket is closed

    @Async
    Scenario: [Async] A client socket closes after goodbye
      Given the multithreaded server is running on port 8222
      When async client "Closer" connects on port 8222
      Then client "Closer" sends "bye" and receives "Have a good day!"
      And client "Closer" socket is closed

    @Sync
    Scenario: [Sync] The connected client count recovers after a client says bye
      Given the multithreaded server is running on port 8125
      When sync client "First" connects on port 8125
      And sync client "Second" connects on port 8125
      Then client "First" sends "stats" and receives "Simultaneously connected clients: 2"
      And client "Second" sends "bye" and receives "Have a good day!"
      When sync client "Replacement" connects on port 8125
      Then client "Replacement" sends "stats" and receives "Simultaneously connected clients: 2"
      And client "Second" socket is closed
      And client "First" disconnects with goodbye
      And client "Replacement" disconnects with goodbye

    @Async
    Scenario: [Async] The connected client count recovers after a client says bye
      Given the multithreaded server is running on port 8225
      When async client "First" connects on port 8225
      And async client "Second" connects on port 8225
      Then client "First" sends "stats" and receives "Simultaneously connected clients: 2"
      And client "Second" sends "bye" and receives "Have a good day!"
      When async client "Replacement" connects on port 8225
      Then client "Replacement" sends "stats" and receives "Simultaneously connected clients: 2"
      And client "Second" socket is closed
      And client "First" disconnects with goodbye
      And client "Replacement" disconnects with goodbye

  Rule: Edge case and message format behavior

    @Sync
    Scenario: [Sync] An empty message gets guidance
      Given the multithreaded server is running on port 8130
      When sync client "Empty" connects on port 8130
      Then client "Empty" sends "" and receives "Please type something."
      And client "Empty" disconnects with goodbye

    @Async
    Scenario: [Async] An empty message gets guidance
      Given the multithreaded server is running on port 8230
      When async client "Empty" connects on port 8230
      Then client "Empty" sends "" and receives "Please type something."
      And client "Empty" disconnects with goodbye

    @Sync
    Scenario: [Sync] A client can continue after sending an empty message
      Given the multithreaded server is running on port 8135
      When sync client "Recover" connects on port 8135
      Then client "Recover" sends "" and receives "Please type something."
      And client "Recover" sends "stats" and receives "Simultaneously connected clients: 1"
      And client "Recover" sends "still here" and receives "Did you say 'still here'?"
      And client "Recover" disconnects with goodbye

    @Async
    Scenario: [Async] A client can continue after sending an empty message
      Given the multithreaded server is running on port 8235
      When async client "Recover" connects on port 8235
      Then client "Recover" sends "" and receives "Please type something."
      And client "Recover" sends "stats" and receives "Simultaneously connected clients: 1"
      And client "Recover" sends "still here" and receives "Did you say 'still here'?"
      And client "Recover" disconnects with goodbye

    @Sync
    Scenario: [Sync] Surrounding spaces are preserved while line endings are trimmed
      Given the multithreaded server is running on port 8140
      When sync client "Spaces" connects on port 8140
      Then client "Spaces" sends "  keep surrounding spaces  " and receives "Did you say '  keep surrounding spaces  '?"
      And client "Spaces" disconnects with goodbye

    @Async
    Scenario: [Async] Surrounding spaces are preserved while line endings are trimmed
      Given the multithreaded server is running on port 8240
      When async client "Spaces" connects on port 8240
      Then client "Spaces" sends "  keep surrounding spaces  " and receives "Did you say '  keep surrounding spaces  '?"
      And client "Spaces" disconnects with goodbye

    @Sync
    Scenario: [Sync] Tabs and backslashes are preserved in the echo response
      Given the multithreaded server is running on port 8145
      When sync client "Escapes" connects on port 8145
      Then client "Escapes" sends escaped message "left\tright\\tail" and receives escaped response "Did you say 'left\tright\\tail'?"
      And client "Escapes" disconnects with goodbye

    @Async
    Scenario: [Async] Tabs and backslashes are preserved in the echo response
      Given the multithreaded server is running on port 8245
      When async client "Escapes" connects on port 8245
      Then client "Escapes" sends escaped message "left\tright\\tail" and receives escaped response "Did you say 'left\tright\\tail'?"
      And client "Escapes" disconnects with goodbye

  Rule: High load behavior

    @Sync
    Scenario: [Sync] The server handles 100 clients sending 10,000 messages from separate threads
      Given the multithreaded server is running on port 8150
      When 100 sync clients with prefix "SyncLoad" connect on port 8150
      Then client "SyncLoad-001" sends "stats" and receives "Simultaneously connected clients: 100"
      When 100 clients with prefix "SyncLoad" each send 100 echo messages from their own thread with a random delay between 10 and 50 milliseconds
      Then 100 clients with prefix "SyncLoad" disconnect with goodbye

    @Async
    Scenario: [Async] The server handles 100 clients sending 10,000 messages from separate threads
      Given the multithreaded server is running on port 8250
      When 100 async clients with prefix "AsyncLoad" connect on port 8250
      Then client "AsyncLoad-001" sends "stats" and receives "Simultaneously connected clients: 100"
      When 100 clients with prefix "AsyncLoad" each send 100 echo messages from their own thread with a random delay between 10 and 50 milliseconds
      Then 100 clients with prefix "AsyncLoad" disconnect with goodbye
