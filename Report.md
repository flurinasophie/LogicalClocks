Assignment 4
------------

# Team Members
Keanu Belo da Silva
Flurina Baumbach
# GitHub link to your (forked) repository (if submitting through GitHub)

https://github.com/flurinasophie/LogicalClocks.git

# Task 2

1. Why did message D have to be buffered and can we now always guarantee that all clients
   display the same message order?

   Message D had to be buffered because it violated causal ordering when it arrived. Looking at the scrambled history, 
   message D has the vector clock [2, 1, 1, 0] from Client 1, which indicates that Client 1 had already processed one message from 
   Client 3 (the third component is 1). However, when Client 4 received message D, it had not yet received message C from 
   Client 3 with clock [1, 1, 1, 0]. 
   According to the causal delivery condition, a message can only be delivered if all causally preceding messages have been delivered. 
   Since message C causally precedes D, message D had to be buffered until C arrived.
   Regarding whether we can always guarantee the same message order: With our vector clock implementation using causal delivery, 
   we can guarantee that all clients will respect causal dependencies, meaning if message A causally precedes message B, 
   then A will be delivered before B at all clients. However, we cannot guarantee that all clients display exactly the same total 
   order for all messages. Concurrent messages (messages that are causally independent) may be delivered in different orders at different clients 
   and this is acceptable. Vector clocks only enforce causal ordering, not total ordering.

   2. Note that the chat application uses UDP. What could be an issue with this design choice—and
      how would you fix it?
   
   The main issues with using UDP are:
   - UDP packets can be lost during transmission. A client might never receive a message (no delivery guarantee), 
   which would cause all causally dependent messages to be buffered indefinitely.
   - Packets can arrive out of order (which we handle with buffering).
   - UDP doesn't implement flow control, which could lead to network congestion.

   To fix these issues, we could:
   - The server could require ACKs from clients and retransmit lost messages. Each message could have a sequence number.
   - If a message is buffered for too long, we could request retransmission of the missing messages (time out)
   - As we learned last semester in Computer Networks, TCP provides reliable, ordered delivery automatically, 
   though at the cost of higher latency.
   - Periodically check if clients are still connected and if they're missing messages.

# Task 3

1. What is potential causality in Distributed Systems, and how can you model it? Why
   “potential causality” and not just “causality”?

   Potential causality ("happened-before" relation, denoted as ->) is a partial ordering of events in a distributed system that captures 
   which events could have potentially influenced other events based on the flow of information.
   An event a potentially causally precedes event b (a -> b) if:

   - a and b occur in the same process and a occurs before b (program order)
   - a is a send event and b is the corresponding receive event
   - There exists an event c such that a -> c and c -> b (transitivity)

   We model it using logical clocks (Lamport clocks) or vector clocks.
   It's called "potential" causality (not just "causality") because:

   - Just because event a happened before event b doesn't mean a actually influenced b - a might have had no actual causal effect
   - The system only tracks information flow, not actual dependencies
   - We cannot observe the actual causal relationships, only the potential for causality based on message passing
   - It's a conservative approximation: if a -> b, then a might have influenced b, but we don't know for certain

   2. If you look at your implementation of Task 2.1, can you think of one limitation of Vector Clocks? How would you overcome the limitation?

   A major limitation of vector clocks is scalability: Each process must maintain a counter for every other process in the system. 
   If we have n processes, each vector clock requires O(n) space and comparing two vector clocks requires O(n) time. 
   This becomes impractical for large-scale distributed systems with thousands or millions of processes.
   How to overcome this limitation:

   - Only track processes that have actually interacted, using a sparse representation (e.g., hash maps) instead of fixed-size arrays 
      -> Dynamic Vector Clocks 
   - Each process maintains a matrix to track what each process knows about others, though this increases space to O(n²)
   - Similar to vector clocks (bullet point above) but optimized for specific use cases like distributed databases -> version vectors
   - Distinguish between events and their context, reducing the size (Dotted Version Vectors)
   - Limit the number of entries and use heuristics to approximate causality (Bounded Vector Clocks)

3. Figure 4 shows an example of enforcing causal communication using Vector Clocks. You can find a detailed explanation of this example and the broadcast algorithm being used in
   the Distributed Systems book by van Steen and Tannenbaum (see Chapter 5.2.2, page 270). Would you achieve the same result if you used the same broadcast algorithm but replaced
   Vector Clocks with Lamport Clocks? If not, why not? Explain briefly. 

   No, you would not achieve the same result with Lamport clocks. The key difference is:

   - Vector clocks provide causal ordering: 
   If VC(a) < VC(b), then a causally precedes b. More importantly, vector clocks can detect when events are concurrent (causally independent).
   - Lamport clocks only provide partial ordering: 
   If LC(a) < LC(b), then a might causally precede b, but the converse is not true - if LC(a) < LC(b), we cannot conclude that a -> b.

   In Figure 4's example, the broadcast algorithm delays messages until all causally preceding messages have been delivered. 
   With Lamport clocks, we lose the ability to determine causal dependencies accurately:

   - We cannot detect concurrent events
   - We cannot reliably determine if message m2 depends on message m1 just by comparing their Lamport timestamps 
   - We would need additional information (like message sequence numbers per process) to reconstruct causality

   Therefore, using Lamport clocks alone would not be sufficient to implement the causal broadcast algorithm shown in Figure 4.
   We would deliver messages in the wrong order or block unnecessarily, violating the causal delivery property.
