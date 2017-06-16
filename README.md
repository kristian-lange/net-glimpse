# EtherVisuWeb

Streaming of data from local network interfaces via WebSockets. The WebSockets can be consumed by e.g. a browser.

![Schema](docs/schema.png)

### Developed with
* Pcap4J (https://github.com/kaitoy/pcap4j)
* Play Framework 2.5
* Akka

### Run with sbt

To access network interfaces you have to start the program either with root or give java special capabilities, e.g. with `sudo setcap cap_net_raw,cap_net_admin=eip /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java`.

`sbt -DNIF=wlp2s0 -Dhttp.address=172.23.1.81 -Dhttp.port=9000 run`
All `-D` parameters are optional. `-DNIF` specifies the default network interface. 

EtherVisuWeb comes with a basic example web page. Add the URL query parameter `nif` to specify the network interface.
* Use default network interface specified with `-DNIF`: `http://172.23.1.81:9000/`
* Specify network interface: `http://172.23.1.81:9000/?nif=enp0s25`

