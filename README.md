ZmqConfigServer
===============

ZMQ Based Central Configuration Manager.

The configuration is read from a [YAML](http://www.yaml.org/) file that can be 
specified on the command line. The parsed configuration is then made accessible 
on a zmq socket.

    zmqconf --config config.yaml --endpoint tcp://*:6000

The config server can be accessed by connecting a zmq-REQ socket.
Using the following protocol (inspired by [MDP Pattern](http://rfc.zeromq.org/spec:7)):

Request:
* Frame 0: Empty (inivisible to the REQ application)
* Frame 1: "ZCS01" (identifier)
* Frame 2: [COMAND] currently 'GET' only
* Frame 3: [KEY] requested key. No whitespace.
* Frame 4: Node information (optional)

Reply:
* Frame 0: Empty (invisble to the REP application)
* Frame 1: "ZCS01" (identifier)
* Frame 2: "OK" or "NOK" (depending on whether the request was successfull)
* Frame 3: Reply body (optional)


## Use Case Examples

The ConfigServer can be used to manage the addresses of the static ZMQ endpoints in a distributed environment.
E.g.

* Register a domain name `myservice.mydomain.com`
* Run `zmqconf` as a daemon service on the `myservice` machine on port `6000`
* Hardcode `tcp://myservice.mydomain.com:6000` into the individual nodes as configuration endpoint

