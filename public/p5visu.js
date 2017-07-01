var canvas;

function setup() {
  var width;
  var height;
  if (windowWidth > windowHeight) {
    width = height = windowHeight;
  } else {
    width = height = windowWidth;
  }
  canvas = createCanvas(width, height);
  centerCanvas();

  background(200);
  textSize(14);
}

function centerCanvas() {
  var x = (windowWidth - width) / 2;
  var y = (windowHeight - height) / 2;
  canvas.position(x, y);
}

function windowResized() {
  if (windowWidth > windowHeight) {
    width = height = windowHeight;
  } else {
    width = height = windowWidth;
  }
  resizeCanvas(width, height);
  centerCanvas();
}

function draw() {
  physics.update();
  background(255);
  drawEdges();
  drawNodes();
}

function drawEdges() {
  Object.keys(graph.nodes).forEach(function (srcAddr) {
    var srcNode = graph.nodes[srcAddr];
    Object.keys(srcNode.edges).forEach(function (dstAddr) {
      var edge = srcNode.edges[dstAddr];
      var dstNode = edge.dstNode;
      drawEdge(edge, srcNode, dstNode);
    });
  });
}

function drawEdge(edge, srcNode, dstNode) {
  if (isBroadcast(dstNode.addr)) {
    return;
  }

  var color = edge.color;
  if (edge.width > 4) {
    edge.width--;
  }
  if (edge.weight > 1) {
    edge.weight -= 0.0005;
  }
  strokeWeight(edge.width * edge.weight);
  stroke(color[0], color[1], color[2], 150);
  var x1 = srcNode.particle.x * width;
  var y1 = srcNode.particle.y * height;
  var x2 = dstNode.particle.x * width;
  var y2 = dstNode.particle.y * height;
  drawArrow(x1, y1, x2, y2);
}

function drawArrow(x1, y1, x2, y2) {
  line(x1, y1, x2, y2);
  push();
  translate(x2, y2);
  rotate(atan2(x1 - x2, y2 - y1));
  line(0, 0, -5, -30);
  line(0, 0, 5, -30);
  pop();
}

function drawNodes() {
  stroke(0, 150);
  strokeWeight(2);
  Object.keys(graph.nodes).forEach(function (addr) {
    push();
    var node = graph.nodes[addr];
    translate(node.particle.x * width, node.particle.y * height);
    stroke(0, 150);
    fill(node.color[0], node.color[1], node.color[2], node.color[3]);

    if (node.width > 20) {
      node.width--;
    }
    ellipse(0, 0, node.width);
    stroke(0);
    textAlign(CENTER);
    text(node.addr, 0, 0);
    pop();
  });

  function isBroadcast(node) {
    return node.addr === "ff:ff:ff:ff:ff:ff" || node.addr === "FF:FF:FF:FF:FF:FF";
  }
}