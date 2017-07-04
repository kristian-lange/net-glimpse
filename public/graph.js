var VerletPhysics2D = toxi.physics2d.VerletPhysics2D,
  VerletParticle2D = toxi.physics2d.VerletParticle2D,
  VerletSpring2D = toxi.physics2d.VerletSpring2D,
  VerletMinDistanceSpring2D = toxi.physics2d.VerletMinDistanceSpring2D,
  AttractionBehavior = toxi.physics2d.behaviors.AttractionBehavior,
  Vec2D = toxi.geom.Vec2D,
  Rect = toxi.geom.Rect;

function Graph(config) {

  var physics = new VerletPhysics2D();
  physics.setDrag(config.physicsDrag);
  physics.setWorldBounds(new Rect(0, 0, config.margin / config.width, config.margin / config.height));

  this.nodes = {}; // Maps addr to node
  setCleaning(this, config);

  this.fill = function (srcAddr, dstAddr, srcNodeColor, dstNodeColor, edgeColor) {
    var srcNode = getOrAddNode(this, srcAddr, config);
    var dstNode = getOrAddNode(this, dstAddr, config);
    var edge = getOrAddEdge(srcNode, dstNode, config);

    var dateNow = Date.now();
    srcNode.tick(dateNow, srcNodeColor);
    dstNode.tick(dateNow, dstNodeColor);
    edge.tick(edgeColor);
  }

  this.update = function () {
    physics.update();
  }

  var getOrAddNode = function (graph, addr, config) {
    var node = graph.nodes[addr];
    if (!node) {
      var node = new Node(addr, physics, config);
      graph.nodes[addr] = node;
    }
    return node;
  }

  var getOrAddEdge = function (srcNode, dstNode) {
    var edge = srcNode.edges[dstNode.addr];
    if (!edge) {
      edge = new Edge(srcNode, dstNode, physics, config);
      srcNode.edges[dstNode.addr] = edge;
      dstNode.incomingEdges[srcNode.addr] = edge;
    }
    return edge;
  }

  function setCleaning(graph, config) {
    window.setInterval(function () {
      graph.removeOldNodesAndEdges();
    }, config.cleaningIntervall);
  }

  this.removeOldNodesAndEdges = function() {
    var dateNow = Date.now();

    var allNodes = this.nodes;
    var oldNodes = [];
    Object.keys(allNodes).forEach(function (addr) {
      var node = allNodes[addr];
      if ((dateNow - node.lastSeen) > config.nodeAge) {
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
function Node(addr, physics, config) {

  this.addr = addr;
  this.color = [0, 0, 0]; // Set anew in every tick
  this.width = config.nodeWidth; // Set anew in every tick
  this.edges = {}; // Outgoing edges: maps edge's dst addr to edge
  this.incomingEdges = {}; // Maps edge's src addr to edge
  this.lastSeen = {};
  this.particle = new VerletParticle2D(getFloatAroundCenter(), getFloatAroundCenter());
  physics.addParticle(this.particle);
  this.behavior = new AttractionBehavior(
    this.particle,
    0.2, // radius
    -config.physicsNodeRepulsion,
    0 // jitter
  );
  physics.addBehavior(this.behavior);

  this.tick = function (dateNow, color) {
    this.lastSeen = dateNow;
    this.color = color;
    this.width = config.nodeWidth;
  }

  this.removePhysics = function () {
    physics.removeBehavior(this.behavior);
    physics.removeParticle(this.particle);
  }
}

function getFloatAroundCenter() {
  return ((Math.random() - 0.5) * 0.1) + 0.5;
}

// Edges are directed from srcNode to dstNode
function Edge(srcNode, dstNode, physics, config) {

  this.dstNode = dstNode;
  this.color = [0, 0, 0];
  this.width = config.edgeWidth;
  this.weight = 1;

  this.spring = new VerletSpring2D(
    srcNode.particle,
    dstNode.particle,
    config.physicsSpringRestLength,
    config.physicsSpringStrength
  );
  physics.addSpring(this.spring);

  this.tick = function (edgeColor) {
    this.color = edgeColor;
    this.width = config.edgeWidth;
    if (this.weight < 10) {
      this.weight += 0.05;
    }
  }

  this.removePhysics = function () {
    physics.removeSpring(this.spring);
  }
}