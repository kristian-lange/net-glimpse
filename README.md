# net-glimps

net-glimps consists of two independent parts: 1) Streaming of data from your network interfaces via WebSockets, and 2) Visualization of this network traffic (this, I call 'glimps').

## How to run

[Download](releases), unzip and start with ./bin/net-glimps (Linux or Unix) or ./bin/net-glimps.bat (Windows).


## Streaming of data from your network interfaces via WebSockets

Streaming of data from your local network interfaces via WebSockets. The WebSockets can be consumed by e.g. a browser.
* It is possible to stream _different_ network interfaces in parallel.
* It is also possible to stream the _same_ network interface to multiple destinations.

![Schema](docs/schema.png)

### Using
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

### License

EtherVisuWeb is distributed under the MIT license.

    Copyright (c) 2011-2015 Pcap4J.org

    Permission is hereby granted, free of charge, to any person obtaining a copy of
    this software and associated documentation files (the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
    and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
    subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
    NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
    IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
