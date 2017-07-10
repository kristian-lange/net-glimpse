var netDataReceiver = {};

(function () {

  // Start WebSocket to get a stream of network package header data
  netDataReceiver.start = function () {
    var nif = getUrlQueryParameterByName("nif");
    var urlQueryStr = (nif) ? "?nif=" + nif : "";
    var socket = new WebSocket(
      ((window.location.protocol === "https:") ? "wss://" : "ws://") +
      window.location.host + "/netdata" + urlQueryStr);

    socket.onmessage = function (event) {
      var packet = JSON.parse(event.data);
      if (packet.ethernet && typeof graphEther !== 'undefined') {
        graphEther.fill(getParameterForEtherVisu(packet));
      }
      if (packet.ip && typeof graphIp !== 'undefined') {
        graphIp.fill(getParameterForIPVisu(packet));
      }
    }
  }

  function getParameterForEtherVisu(packet) {
    var para = {};
    para.srcAddr = packet.ethernet.macSrcAddr;
    para.dstAddr = packet.ethernet.macDstAddr;
    para.srcNodeColor = getColorFromMac(para.srcAddr);
    para.dstNodeColor = getColorFromMac(para.dstAddr);
    var etherType = packet.ethernet.etherType;
    if (etherTypeConfig[etherType]) {
      para.edgeColor = etherTypeConfig[etherType].color;
      para.edgeText = etherTypeConfig[etherType].name;
    } else {
      para.edgeColor = etherTypeConfig["unknown"].color;
      para.edgeText = null;
    }
    return para;
  }

  function getParameterForIPVisu(packet) {
    var para = {};

    para.srcAddr = packet.ip.srcAddr.substring(1);
    para.dstAddr = packet.ip.dstAddr.substring(1);

    if (packet.ip.version === "IPv4") {
      para.srcNodeColor = getColorFromIPv4(para.srcAddr);
      para.dstNodeColor = getColorFromIPv4(para.dstAddr);
    } else if (packet.ip.version === "IPv6") {
      para.srcNodeColor = getColorFromIPv6(para.srcAddr);
      para.dstNodeColor = getColorFromIPv6(para.dstAddr);
    }

    fillEdgeColorAndText(packet, para);
    return para;
  }

  function fillEdgeColorAndText(packet, para) {
    if (packet.tcp) {
      var srcPort = packet.tcp.srcPort;
      var dstPort = packet.tcp.dstPort;
      fillEdgeColorAndTextWithConfigData(srcPort, dstPort, para);
    } else if (packet.udp) {
      var srcPort = packet.udp.srcPort;
      var dstPort = packet.udp.dstPort;
      fillEdgeColorAndTextWithConfigData(srcPort, dstPort, para);
    }

    if (!para.edgeColor) {
      para.edgeColor = ipPacketConfig["unknown"].color;
    }
    if (!para.edgeText) {
      para.edgeText = null;
    }
  }

  function fillEdgeColorAndTextWithConfigData(srcPort, dstPort, para) {
    if (ipPacketConfig[srcPort]) {
      para.edgeColor = ipPacketConfig[srcPort].color;
      para.edgeText = ipPacketConfig[srcPort].name;
    } else if (ipPacketConfig[dstPort]) {
      para.edgeColor = ipPacketConfig[dstPort].color;
      para.edgeText = ipPacketConfig[dstPort].name;
    }
    // If no name was found show at least the port numbers if at least one of the  ports is < 1024 or
    // it's configured in configIp.showUnknownPorts
    if (!para.edgeText &&
      (srcPort < 1024 || dstPort < 1024 || configIp.showUnknownPorts)) {
      para.edgeText = srcPort + ":" + dstPort;
    }
  }

  function getColorFromMac(macAddr) {
    var addrArray = macAddr.split(":");
    var color = [];
    color[0] = (parseInt(addrArray[0], 16) + parseInt(addrArray[3], 16)) / 2;
    color[1] = (parseInt(addrArray[1], 16) + parseInt(addrArray[4], 16)) / 2;
    color[2] = (parseInt(addrArray[2], 16) + parseInt(addrArray[5], 16)) / 2;
    return color;
  }

  function getColorFromIPv4(addr) {
    var addrArray = addr.split(".");
    var color = [];
    color[0] = (parseInt(addrArray[0]) + parseInt(addrArray[1])) / 2;
    color[1] = (parseInt(addrArray[1]) + parseInt(addrArray[2])) / 2;
    color[2] = (parseInt(addrArray[2]) + parseInt(addrArray[3])) / 2;
    return color;
  }

  function getColorFromIPv6(addr) {
    return intToColor(hashCode(addr));
  }

  function hashCode(str) {
    var hash = 0;
    for (var i = 0; i < str.length; i++) {
      hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return hash;
  }

  function intToColor(i) {
    var color = [];
    color[0] = ((i >> 24) & 0xFF);
    color[1] = ((i >> 16) & 0xFF);
    color[2] = ((i >> 8) & 0xFF);
    return color;
  }

})();