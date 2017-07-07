function Visu(graph, config) {

  this.s = function (p) {

    var paused = false;

    p.setup = function () {
      p.resizeCanvas(config.canvas.width, config.canvas.height);

      p.background(config.canvas.backgroundColor);
      p.textAlign(p.CENTER);
    }

    p.windowResized = function () {
      p.resizeCanvas(config.canvas.width, config.canvas.height);
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
      p.background(config.canvas.backgroundColor);
      drawEdges();
      drawNodes();
    }

    function drawEdges() {
      p.textSize(config.edge.textSize);
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
      if (edge.width > config.edge.width) {
        edge.width--;
      }
      if (edge.weight > 1) {
        edge.weight -= 0.005;
      }
      if (edge.color[3] > config.edge.transparency) {
        edge.color[3] -= 5;
      }
      p.strokeWeight(edge.width * edge.weight);
      p.stroke(edge.color[0], edge.color[1], edge.color[2], edge.color[3]);
      var x1 = calcX(srcNode.particle.x);
      var y1 = calcY(srcNode.particle.y);
      var x2 = calcX(dstNode.particle.x);
      var y2 = calcY(dstNode.particle.y);
      drawArrow(x1, y1, x2, y2);
      if (config.edge.showText && edge.text !== null) {
        drawEdgeText(x1, y1, x2, y2, edge.text, edge.color);
      }
    }

    function drawArrow(x1, y1, x2, y2) {
      p.line(x1, y1, x2, y2);
      p.push();
      p.translate(x2, y2);
      p.rotate(p.atan2(x1 - x2, y2 - y1));
      p.line(0, 0, -config.edge.arrowWidth, -config.edge.arrowLength);
      p.line(0, 0, config.edge.arrowWidth, -config.edge.arrowLength);
      p.pop();
    }

    function drawEdgeText(x1, y1, x2, y2, text, color) {
      var xMiddle = x1 + ((x2 - x1) / 2);
      var yMiddle = y1 + ((y2 - y1) / 2);
      p.fill(0, 0, 0, color[3]);
      p.strokeWeight(0);
      p.text(text, xMiddle, yMiddle);
    }

    function drawNodes() {
      p.textSize(config.node.textSize);
      p.strokeWeight(config.node.border);
      Object.keys(graph.nodes).forEach(function (addr) {
        p.push();
        var node = graph.nodes[addr];
        p.translate(calcY(node.particle.x), calcY(node.particle.y));
        p.stroke(0, 0, 0, node.color[3]);
        if (node.color[3] > config.node.transparency) {
          node.color[3] -= 5;
        }
        p.fill(node.color[0], node.color[1], node.color[2], node.color[3]);
        if (node.width > config.node.width) {
          node.width--;
        }
        p.ellipse(0, 0, node.width);
        if (config.node.showText) {
          p.stroke(0, 0, 0, node.color[3]);
          p.text(node.addr, 0, 0);
        }
        p.pop();
      });
    }

    function calcX(x) {
      return x * p.width + config.canvas.margin;
    }

    function calcY(y) {
      return y * p.height + config.canvas.margin;
    }

  }

}