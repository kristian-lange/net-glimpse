# net-glimps

net-glimps consists of two independent parts: 1) Streaming of header data from your network interfaces via WebSockets, and 2) Visualization of this network traffic.

### Using

* Pcap4J (https://github.com/kaitoy/pcap4j) to access network interfaces
* Play Framework 2.5
* Akka to distribute network interface data to multiple WebSockets
* Graphics with [p5js](https://p5js.org/) and physics with [toxiclibs](https://github.com/hapticdata/toxiclibsjs)

## How to run

1. [Download](https://github.com/kristian-lange/net-glimps/releases)

1. Unzip

1. To access network interfaces you have to start the program either with **root** or give java special capabilities, e.g. with `sudo setcap cap_net_raw,cap_net_admin=eip /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java`.

1. Run on Linux or Unix `./bin/net-glimps` or on Windows `.\bin\net-glimps.bat`
   
   You can specify IP and port with the parameters `-Dhttp.address` and `-Dhttp.port`. By default `localhost` and `9000` is used.

   Example:

   ```shell
   ./bin/net-glimps -Dhttp.address=172.23.1.81 -Dhttp.port=8080
   ```
   
## Visualization of network traffic

1. `/glimps?nif=myNetworkInterface` - shows both, Ethernet and Internet
   
2. `/ipglimps?nif=myNetworkInterface` - shows only Internet
   
3. `/etherglimps?nif=myNetworkInterface` - shows only Ethernet

E.g. `http://localhost:9000/glimps?nif=wlp3s0` shows a visualization of the Ethernet layer and the Internet layer of the network interface `wlp3s0`.

## Streaming of header data from your network interfaces via WebSockets

To get the header data you have to open a WebSocket with the URL `/netdata`. The network interface you want to intercept has to be specified in the query string with the parameter 'nif'.

E.g. in JavaScript to get traffic from the network interface 'wlp3s0' one could write

```javascript
var socket = new WebSocket(ws://myhost/netdata/?nif=wlp3s0);
```

or more general with secure WebSockets and assuming net-glimps runs on the same host as your JavaScript is served.

```javascript
var socket = new WebSocket(
      ((window.location.protocol === "https:") ? "wss://" : "ws://") +
      window.location.host + "/netdata/?nif=wlp3s0);
```

* It is possible to **stream different network interfaces in parallel**.
* It is also possible to **stream the same network interface to multiple destinations**.



![Schema](docs/schema.png)





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
