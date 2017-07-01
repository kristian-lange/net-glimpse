function createNewGraph() {

  var graph = {};
  graph.nodes = {}; // Maps addr to node

  window.setInterval(removeOldNodesAndEdges, 1000);

  graph.fill = function (srcAddr, dstAddr, etherType) {
    var dateNow = Date.now();
    var srcNode = getOrAddNode(srcAddr);
    var dstNode = getOrAddNode(dstAddr);
    srcNode.tick(dateNow);
    dstNode.tick(dateNow);

    var edge = getOrAddEdge(srcNode, dstNode);
    var edgeColor = getColorFromEtherType(etherType);
    edge.tick(edgeColor);
  }

  function getOrAddNode(addr) {
    var node = graph.nodes[addr];
    if (!node) {
      var node = new Node(addr);
      graph.nodes[addr] = node;
    }
    return node;
  }

  // Nodes have an address and an arbitrary number of directed edges
  function Node(addr) {

    this.addr = addr;
    this.color = getColorFromMac(addr);
    this.width = 50;
    this.edges = {}; // Outgoing edges: maps edge's dst addr to edge
    this.incomingEdges = {}; // Maps edge's src addr to edge
    this.lastSeen = {};

    this.particle = new VerletParticle2D(Math.random(), Math.random());
    physics.addParticle(this.particle);
    this.behavior = new AttractionBehavior(
      this.particle,
      0.2, // radius
      -0.012, // force
      0 // jitter
    );
    physics.addBehavior(this.behavior);

    this.tick = function (dateNow) {
      this.lastSeen = dateNow;
      this.width = 50;
    }

    this.removePhysics = function () {
      physics.removeBehavior(this.behavior);
      physics.removeParticle(this.particle);
    }
  }

  function getOrAddEdge(srcNode, dstNode) {
    var edge = srcNode.edges[dstNode.addr];
    if (!edge) {
      edge = new Edge(srcNode, dstNode);
      srcNode.edges[dstNode.addr] = edge;
      dstNode.incomingEdges[srcNode.addr] = edge;
    }
    return edge;
  }

  // Edges are directed from srcNode to dstNode
  function Edge(srcNode, dstNode) {

    this.dstNode = dstNode;
    this.color = [0, 0, 0];
    this.width = 15;
    this.weight = 1;

    this.spring = new VerletSpring2D(
      srcNode.particle,
      dstNode.particle,
      0.2, // restLength
      0.1 // strenght
    );
    physics.addSpring(this.spring);

    this.tick = function (edgeColor) {
      this.color = edgeColor;
      this.width = 15;
      if (this.weight < 10) {
        this.weight += 0.05;
      }
    }

    this.removePhysics = function () {
      physics.removeSpring(this.spring);
    }
  }

  function removeOldNodesAndEdges() {
    var dateNow = Date.now();

    var oldNodes = [];
    Object.keys(graph.nodes).forEach(function (addr) {
      var node = graph.nodes[addr];
      if ((dateNow - node.lastSeen) > 3000) {
        oldNodes.push(node);
      }
    });

    removeNodes(oldNodes);
  }

  // Remove nodes from graph, nodes' physics, and edges and their phyics
  function removeNodes(nodes) {
    for (i = 0, len = nodes.length; i < len; i++) {
      var oldNode = nodes[i];

      // Delete all outgoing edges (only physics - object will be deleted with node)
      Object.keys(oldNode.edges).forEach(function (dstAddr) {
        var edge = oldNode.edges[dstAddr];
        edge.removePhysics();
      });

      // Delete all edges from other nodes that might target this old node
      Object.keys(oldNode.incomingEdges).forEach(function (srcAddr) {
        var otherNode = graph.nodes[srcAddr];
        if (!otherNode) {
          return;
        }
        var edge = otherNode.edges[oldNode.addr];
        if (!edge) {
          return;
        }
        edge.removePhysics();
        delete otherNode.edges[oldNode.addr];
      });

      // Delete old node
      oldNode.removePhysics();
      delete graph.nodes[oldNode.addr];
    }
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

  return graph;
}