# EtherVisuWeb

Streaming of data from your local network interfaces via WebSockets. The WebSockets can be consumed by e.g. a browser.
* It is possible to stream _different_ network interfaces in parallel.
* It is also possible to stream the _same_ network interface to multiple destinations.

![Schema](docs/schema.png)

### Developed with
* Pcap4J (https://github.com/kaitoy/pcap4j)
* Play Framework 2.5
* Akka

### Run with sbt

To access network interfaces you have to start the program either with **root** or give java special capabilities, e.g. with `sudo setcap cap_net_raw,cap_net_admin=eip /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java`.

Start EtherVisuWeb with

    sbt -DNIF=wlp2s0 -Dhttp.address=172.23.1.81 -Dhttp.port=9000 run

All `-D` parameters are optional. `-DNIF` specifies the default network interface. 

### Try out in your browser

EtherVisuWeb comes with a basic example web page. Add the URL query parameter `nif` to specify the network interface.

* Use default network interface specified with `-DNIF`: `http://172.23.1.81:9000/`
* Specify network interface: `http://172.23.1.81:9000/?nif=enp0s25`

### Use it in your stuff

The endpoint to open a WebSocket to get the network data is `/ether`. Add the URL query parameter `nif` to specify the network interface, e.g. `http://172.23.1.81:9000/ether?nif=enp0s25`.

