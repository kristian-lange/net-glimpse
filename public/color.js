var unknownColor = [0, 0, 0];

var etherTypeColor = {};
etherTypeColor["IPv4"] = [178, 7, 7];
etherTypeColor["IPv6"] = [0, 233, 39];
etherTypeColor["ARP"] = [44, 166, 255];
etherTypeColor["RARP"] = [0, 22, 167];
etherTypeColor["PPP"] = [50, 50, 50];
etherTypeColor["PPPoE Discovery Stage"] = [100, 100, 100];
etherTypeColor["PPPoE Session Stage"] = [150, 150, 150];
etherTypeColor["MPLS"] = [222, 100, 0];
etherTypeColor["IEEE 802.1Q VLAN-tagged frames"] = [80, 0, 80];
etherTypeColor["Appletalk"] = [230, 255, 10];
etherTypeColor["unknown"] = unknownColor;
setColorOfElement("box-IPv4", etherTypeColor["IPv4"]);
setColorOfElement("box-IPv6", etherTypeColor["IPv6"]);
setColorOfElement("box-ARP", etherTypeColor["ARP"]);
setColorOfElement("box-RARP", etherTypeColor["RARP"]);
setColorOfElement("box-PPP", etherTypeColor["PPP"]);
setColorOfElement("box-PPPoE-Discovery", etherTypeColor["PPPoE Discovery Stage"]);
setColorOfElement("box-PPPoE-Session", etherTypeColor["PPPoE Session Stage"]);
setColorOfElement("box-MPLS", etherTypeColor["MPLS"]);
setColorOfElement("box-VLAN-tagged-frames", etherTypeColor["IEEE 802.1Q VLAN-tagged frames"]);
setColorOfElement("box-Appletalk", etherTypeColor["Appletalk"]);
setColorOfElement("box-Ether-unknown", etherTypeColor["unknown"]);

var ipProtocolColor = {};
ipProtocolColor["TCP"] = [0, 20, 155];
ipProtocolColor["UDP"] = [150, 0, 20];
ipProtocolColor["unknown"] = unknownColor;
setColorOfElement("box-TCP", ipProtocolColor["TCP"]);
setColorOfElement("box-UDP", ipProtocolColor["UDP"]);
setColorOfElement("box-IP-unknown", ipProtocolColor["unknown"]);

function setColorOfElement(id, color) {
  document.getElementById(id).style.backgroundColor =
    "rgb(" + color[0] + "," + color[1] + "," + color[2] + ")";
}

function getRandomColor() {
  return Math.floor((Math.random() * 200) + 100);
}

function getColorFromMac(macAddr) {
  var addrArray = macAddr.split(":");
  var color = [];
  color[0] = (parseInt(addrArray[0], 16) + parseInt(addrArray[3], 16)) / 2;
  color[1] = (parseInt(addrArray[1], 16) + parseInt(addrArray[4], 16)) / 2;
  color[2] = (parseInt(addrArray[2], 16) + parseInt(addrArray[5], 16)) / 2;
  color[3] = 150;
  return color;
}

function getColorFromEtherType(etherType) {
  var edgeColor = etherTypeColor[etherType];
  return edgeColor ? edgeColor : etherTypeColor["unknown"];
}

function getColorFromIPv4(addr) {
  var addrArray = addr.split(".");
  var color = [];
  color[0] = (parseInt(addrArray[0]) + parseInt(addrArray[1])) / 2;
  color[1] = (parseInt(addrArray[1]) + parseInt(addrArray[2])) / 2;
  color[2] = (parseInt(addrArray[2]) + parseInt(addrArray[3])) / 2;
  color[3] = 150;
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

function intToColor(i){
    var color = [];
    color[0] = ((i>>24)&0xFF);
		color[1] = ((i>>16)&0xFF);
    color[2] = ((i>>8)&0xFF);
    color[3] = 150;
    return color;
}

function getColorFromIpProtocol(protocol) {
  var edgeColor = ipProtocolColor[protocol];
  return edgeColor ? edgeColor : ipProtocolColor["unknown"];
}