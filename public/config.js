// If not stated otherwise numbers are in px
var configEther = {
  'width': 0, // Will be set dynamically
  'height': 0, // Will be set dynamically
  'margin': 20, // Canvas margin (so nodes keep away from the borders)
  'textSize': 14,
  'physicsDrag': 0.5,
  'physicsNodeRepulsion': 0.012, // Repulsion between two nodes
  'physicsSpringRestLength': 0.2, // Spring length in rest between two nodes
  'physicsSpringStrength': 0.1, // Spring strength between two nodes
  'backgroundColor': 255, // Only shades of gray
  'cleaningIntervall': 1000, // Removing old nodes and edges every x ms
  'edgeWidth': 4, // Minmum edge line width
  'edgeTickWidth': 10, // Edge width after package sent 
  'edgeArrowLength': 30, // Arrow length of each edge
  'edgeArrowWidth': 7, // Arrow width of each node
  'nodeWidth': 20, // Minumum node circle width
  'nodeBorder': 2, // Node border line thickness
  'nodeAge': 3000 // After how many ms will a node be removed if it haven't had a package
};

var configIp = Object.assign({}, configEther);

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
etherTypeColor["unknown"] = [0, 0, 0];

var ipProtocolColor = {};
ipProtocolColor["TCP"] = [0, 20, 155];
ipProtocolColor["UDP"] = [150, 0, 20];
ipProtocolColor["unknown"] = [0, 0, 0];
