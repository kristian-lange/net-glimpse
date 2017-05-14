
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/madsen/idea-workspace/play-java-seed/conf/routes
// @DATE:Sat May 13 18:49:53 CEST 2017


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
