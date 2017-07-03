var VerletPhysics2D = toxi.physics2d.VerletPhysics2D,
  VerletParticle2D = toxi.physics2d.VerletParticle2D,
  VerletSpring2D = toxi.physics2d.VerletSpring2D,
  VerletMinDistanceSpring2D = toxi.physics2d.VerletMinDistanceSpring2D,
  AttractionBehavior = toxi.physics2d.behaviors.AttractionBehavior,
  Vec2D = toxi.geom.Vec2D,
  Rect = toxi.geom.Rect;

function Graph() {

  var physics = new VerletPhysics2D();
  physics.setDrag(0.5);
  physics.setWorldBounds(new Rect(0, 0, 0.9, 0.9));

  this.nodes = {}; // Maps addr to node

  this.fill = function (srcAddr, dstAddr, srcNodeColor, dstNodeColor, edgeColor) {
    var srcNode = getOrAddNode(this, srcAddr);
    var dstNode = getOrAddNode(this, dstAddr);
    var edge = getOrAddEdge(srcNode, dstNode);

    var dateNow = Date.now();
    srcNode.tick(dateNow, srcNodeColor);
    dstNode.tick(dateNow, dstNodeColor);
    edge.tick(edgeColor);
  }

  this.update = function () {
    physics.update();
  }

  var getOrAddNode = function (graph, addr) {
    var node = graph.nodes[addr];
    if (!node) {
      var node = new Node(addr, physics);
      graph.nodes[addr] = node;
    }
    return node;
  }

  function getOrAddEdge(srcNode, dstNode) {
    var edge = srcNode.edges[dstNode.addr];
    if (!edge) {
      edge = new Edge(srcNode, dstNode, physics);
      srcNode.edges[dstNode.addr] = edge;
      dstNode.incomingEdges[srcNode.addr] = edge;
    }
    return edge;
  }

  this.removeOldNodesAndEdges = function () {
    var dateNow = Date.now();

    var allNodes = this.nodes;
    var oldNodes = [];
    Object.keys(allNodes).forEach(function (addr) {
      var node = allNodes[addr];
      if ((dateNow - node.lastSeen) > 3000) {
        oldNodes.push(node);
      }
    });

    removeNodes(allNodes, oldNodes);
  }

  // Remove nodes from graph, nodes' physics, and edges and their phyics
  function removeNodes(allNodes, oldNodes) {
    for (i = 0, len = oldNodes.length; i < len; i++) {
      var oldNode = oldNodes[i];

      // Delete all outgoing edges (only physics - object will be deleted with node)
      Object.keys(oldNode.edges).forEach(function (dstAddr) {
        var edge = oldNode.edges[dstAddr];
        edge.removePhysics();
      });

      // Delete all edges from other nodes that might target this old node
      Object.keys(oldNode.incomingEdges).forEach(function (srcAddr) {
        var otherNode = allNodes[srcAddr];
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
      delete allNodes[oldNode.addr];
    }
  }
}

// Nodes have an address and an arbitrary number of directed edges
function Node(addr, physics) {

  this.addr = addr;
  this.color = unknownColor;
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

  this.tick = function (dateNow, color) {
    this.lastSeen = dateNow;
    this.color = color;
    this.width = 50;
  }

  this.removePhysics = function () {
    physics.removeBehavior(this.behavior);
    physics.removeParticle(this.particle);
  }
}

// Edges are directed from srcNode to dstNode
function Edge(srcNode, dstNode, physics) {

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