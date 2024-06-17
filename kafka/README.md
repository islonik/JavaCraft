# Kafka simple application

## Install Kafka

### Install with Zookeper

https://kafka.apache.org/quickstart

1. Download Kafka
2. Unpack Kafka
3. Start up Zookeper
```bash
# Start the ZooKeeper service
$ bin/zookeeper-server-start.sh config/zookeeper.properties
```

4. Start up Kafka
```bash
# Start the Kafka broker service
$ bin/kafka-server-start.sh config/server.properties
```

### Install in Kraft configuration
https://github.com/bitnami/containers/tree/main/bitnami/kafka#apache-kafka-kraft-mode-configuration

1. <b>Step 1: Get the latest kafka</b>
```bash
docker pull bitnami/kafka:latest
```
2. <b>Step 2: Create a network</b>
```bash
docker network create app-tier --driver bridge
```
3. <b>Step 3: Launch the Apache Kafka server instance</b>
   Use the --network app-tier argument to the docker run command to attach the Apache Kafka container to the app-tier network.
```bash
docker run -d --name kafka-server --hostname kafka-server \
    --network app-tier \
    -e KAFKA_CFG_NODE_ID=0 \
    -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
    -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
    -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
    -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka-server:9093 \
    -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
    bitnami/kafka:latest
```