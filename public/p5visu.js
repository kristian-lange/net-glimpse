function Visu(graph, config) {

  this.s = function (p) {

    var paused = false;

    p.setup = function () {
      p.resizeCanvas(config.width, config.height);

      p.background(config.backgroundColor);
      p.textSize(config.textSize);
    }

    p.windowResized = function () {
      p.resizeCanvas(config.width, config.height);
    }

    p.keyTyped = function () {
      if (p.key === 'p') {
        paused = !paused;
      }
    }

    p.draw = function () {
      graph.update();

      if (paused) {
        return;
      }
      p.background(config.backgroundColor);
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
      var color = edge.color;
      if (edge.width > config.edgeWidth) {
        edge.width--;
      }
      if (edge.weight > 1) {
        edge.weight -= 0.0005;
      }
      p.strokeWeight(edge.width * edge.weight);
      p.stroke(color[0], color[1], color[2], 150);
      var x1 = srcNode.particle.x * p.width + config.margin;
      var y1 = srcNode.particle.y * p.height + config.margin;
      var x2 = dstNode.particle.x * p.width + config.margin;
      var y2 = dstNode.particle.y * p.height + config.margin;
      drawArrow(x1, y1, x2, y2);
    }

    function drawArrow(x1, y1, x2, y2) {
      p.line(x1, y1, x2, y2);
      p.push();
      p.translate(x2, y2);
      p.rotate(p.atan2(x1 - x2, y2 - y1));
      p.line(0, 0, -config.edgeArrowWidth, -config.edgeArrowLength);
      p.line(0, 0, config.edgeArrowWidth, -config.edgeArrowLength);
      p.pop();
    }

    function drawNodes() {
      p.strokeWeight(config.nodeBorder);
      Object.keys(graph.nodes).forEach(function (addr) {
        p.push();
        var node = graph.nodes[addr];
        p.translate(node.particle.x * p.width + config.margin, node.particle.y * p.height + config.margin);
        p.stroke(0, 150);
        p.fill(node.color[0], node.color[1], node.color[2], node.color[3]);

        if (node.width > config.nodeWidth) {
          node.width--;
        }
        p.ellipse(0, 0, node.width);
        p.stroke(0);
        p.textAlign(p.CENTER);
        p.text(node.addr, 0, 0);
        p.pop();
      });
    }
  }

}