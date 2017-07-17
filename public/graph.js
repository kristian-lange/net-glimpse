var VerletPhysics2D = toxi.physics2d.VerletPhysics2D,
  VerletParticle2D = toxi.physics2d.VerletParticle2D,
  VerletSpring2D = toxi.physics2d.VerletSpring2D,
  VerletMinDistanceSpring2D = toxi.physics2d.VerletMinDistanceSpring2D,
  AttractionBehavior = toxi.physics2d.behaviors.AttractionBehavior,
  Vec2D = toxi.geom.Vec2D,
  Rect = toxi.geom.Rect;


function Graph(config) {

  var physics = new VerletPhysics2D();
  physics.setDrag(config.physics.drag);

  this.nodes = {}; // Maps addr to node
  setCleaning(this, config);

  this.fill = function (para) {
    var srcNode = getOrAddNode(this, para.srcAddr, config);
    var dstNode = getOrAddNode(this, para.dstAddr, config);
    var edge = getOrAddEdge(srcNode, dstNode, para.edgeText, config);

    var dateNow = Date.now();
    srcNode.tick(dateNow, para.srcNodeColor);
    dstNode.tick(dateNow, para.dstNodeColor);
    edge.tick(para.edgeColor, para.edgeText);
  }

  this.update = function () {
    physics.update();
  }

  this.getWorldBounds = function () {
    return physics.getWorldBounds();
  }

  this.setWorldBounds = function () {
    // Use the proper width to height ratio to prevent graphics stretching
    var width = config.canvas.width / config.canvas.height;
    var height = 1;
    physics.setWorldBounds(new Rect(0, 0, width, height));
  }

  var getOrAddNode = function (graph, addr, config) {
    var node = graph.nodes[addr];
    if (!node) {
      var node = new Node(addr, physics, config);
      graph.nodes[addr] = node;
    }
    return node;
  }

  var getOrAddEdge = function (srcNode, dstNode, edgeText, config) {
    var edge = srcNode.edges[dstNode.addr];
    if (!edge) {
      edge = new Edge(srcNode, dstNode, edgeText, physics, config);
      srcNode.edges[dstNode.addr] = edge;
      dstNode.incomingEdges[srcNode.addr] = edge;
    }
    return edge;
  }

  function setCleaning(graph, config) {
    window.setInterval(function () {
      graph.removeOldNodesAndEdges();
    }, config.graph.cleaningIntervall);
  }

  this.removeOldNodesAndEdges = function () {
    var dateNow = Date.now();

    var allNodes = this.nodes;
    var oldNodes = [];
    Object.keys(allNodes).forEach(function (addr) {
      var node = allNodes[addr];
      if ((dateNow - node.lastSeen) > config.graph.cleaningAge) {
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
  this.color = [0, 0, 0, 255]; // Set anew in every tick
  this.width = config.node.width; // Set anew in every tick
  this.edges = {}; // Outgoing edges: maps edge's dst addr to edge
  this.incomingEdges = {}; // Maps edge's src addr to edge
  this.lastSeen = {};
  this.particle = new VerletParticle2D(
    getFloatAroundCenter(physics.getWorldBounds().width),
    getFloatAroundCenter(physics.getWorldBounds().height));
  physics.addParticle(this.particle);
  this.behavior = new AttractionBehavior(
    this.particle,
    config.physics.nodeRepulsionRange,
    -config.physics.nodeRepulsion,
    0 // jitter
  );
  physics.addBehavior(this.behavior);

  // tick is called each time this node was either the src node dst node of a packet
  this.tick = function (dateNow, color) {
    this.lastSeen = dateNow;
    this.color[0] = color[0];
    this.color[1] = color[1];
    this.color[2] = color[2];
    this.color[3] = 255;
    this.width = config.node.tickWidth;
  }

  // update is called during each frame drawing
  this.update = function() {
    if (this.color[3] > config.node.transparency) {
      // Return to normal transparency after a tick
      this.color[3] -= config.node.transparencyTickStep;
    }
    if (this.width > config.node.width) {
      // Return to normal width after a tick
      this.width--;
    }
  }

  this.removePhysics = function () {
    physics.removeBehavior(this.behavior);
    physics.removeParticle(this.particle);
  }
}

function getFloatAroundCenter(size) {
  return Math.random() * 0.1 + size / 2;
}

// Edges are directed from srcNode to dstNode
function Edge(srcNode, dstNode, text, physics, config) {

  this.dstNode = dstNode;
  this.color = [0, 0, 0, 255];
  this.text = text;
  this.width = config.edge.width;
  this.weight = 1;

  this.spring = new VerletSpring2D(
    srcNode.particle,
    dstNode.particle,
    config.physics.springRestLength,
    config.physics.springStrength
  );
  physics.addSpring(this.spring);

  // tick is called if a packet was sent on this edge
  this.tick = function (color, text) {
    this.color[0] = color[0];
    this.color[1] = color[1];
    this.color[2] = color[2];
    this.color[3] = 255;
    this.text = text;
    this.width = config.edge.tickWidth;
    if (this.weight < config.edge.tickWeightMax) {
      this.weight += 3 * config.edge.tickWeightStep;
    }
  }

  // update is called during each frame drawing
  this.update = function() {
    if (this.width > config.edge.width) {
      // Return to normal width after a tick
      this.width -= config.edge.tickWidthStep;
    }
    if (this.weight > 1) {
      // Regress to normal width weight
      this.weight -= config.edge.tickWeightStep;
    }
    if (this.color[3] > config.edge.transparency) {
      // Return to normal transparency after a tick
      this.color[3] -= config.edge.transparencyTickStep;
    }
  }

  this.removePhysics = function () {
    physics.removeSpring(this.spring);
  }
}