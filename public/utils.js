function setSingleCanvasSize(config, graph) {
  if (isFullscreen()) {
    document.getElementById('canvasContainer').style.width = getWindowWidth() + "px";
    document.getElementById('canvasContainer').style.marginLeft = 0 + "px";
    document.getElementById('canvasContainer').style.marginTop = 0 + "px";
    document.getElementById('glossaryContainer').style.display = 'none';
    config.canvas.width = getWindowWidth();
    config.canvas.height = getWindowHeight() - 4; // -4 seems to be necessary to prevent scrollbars
  } else {
    var size = getSquaredCanvasSize();
    document.getElementById("canvasContainer").style.width = size + "px";
    document.getElementById('canvasContainer').style.marginLeft = ((getWindowWidth() - size) / 2) + "px";
    document.getElementById('glossaryContainer').style.display = 'block';
    document.getElementById("glossaryContainer").style.width = size + "px";
    document.getElementById('glossaryContainer').style.marginLeft = ((getWindowWidth() - size) / 2) + "px";
    config.canvas.width = config.canvas.height = size;
  }
  graph.setWorldBounds();
}

function setTwinCanvasSize(config1, config2, graph1, graph2) {
  var size = getFullScreenCanvasSize();
  if (isFullscreen()) {
    document.getElementById('canvasContainer').style.marginTop = ((getWindowHeight() - size) / 2) + "px";
    document.getElementById('glossaryContainer').style.display = 'none';
  } else {
    document.getElementById('canvasContainer').style.marginTop = 0 + "px";
    document.getElementById('glossaryContainer').style.display = 'block';
    document.getElementById("glossaryEther").style.width = size + "px";
    document.getElementById("glossaryIp").style.width = size + "px";
  }
  config1.canvas.width = config1.canvas.height = size;
  config2.canvas.width = config2.canvas.height = size;
  graph1.setWorldBounds();
  graph2.setWorldBounds();
}

function getFullScreenCanvasSize() {
  if (isFullscreen()) {
    return getWindowWidth() / 2 - 10;
  } else {
    return getWindowWidth() / 2 - 20;
  }
}

function getSquaredCanvasSize() {
  var width = getWindowWidth();
  var height = getWindowHeight();
  if (width > height) {
    return height;
  } else {
    return width;
  }
}

function getWindowWidth() {
  return window.innerWidth ||
    document.documentElement.clientWidth ||
    document.body.clientWidth;
}

function getWindowHeight() {
  return window.innerHeight ||
    document.documentElement.clientHeight ||
    document.body.clientHeight;
}

function isFullscreen() {
  // This checks for fullscreen. document.addEventListener with 'fullscreenchange' doesn't work yet.
  return window.outerWidth === screen.width && window.outerHeight === screen.height;
}

function fillGlossary(ul, config) {
  Object.keys(config).forEach(function (key) {
    var li = document.createElement("li");

    var div = document.createElement("div");
    div.className = "glossaryBox";
    var color = config[key].color;
    div.style.backgroundColor = "rgb(" + color[0] + "," + color[1] + "," + color[2] + ")";

    var span = document.createElement("span");
    span.innerText = config[key].desc;

    li.appendChild(div);
    li.appendChild(span);
    ul.appendChild(li);
  });
}

function getUrlQueryParameterByName(name) {
  var url = window.location.href;
  name = name.replace(/[\[\]]/g, "\\$&");
  var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
    results = regex.exec(url);
  if (!results) return null;
  if (!results[2]) return '';
  return decodeURIComponent(results[2].replace(/\+/g, " "));
}