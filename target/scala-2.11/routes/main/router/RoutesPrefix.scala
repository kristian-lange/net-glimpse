
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/madsen/idea-workspace/ether-visu-web/conf/routes
// @DATE:Sun May 14 18:49:08 CEST 2017


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
