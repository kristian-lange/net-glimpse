
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object index_Scope0 {
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import java.lang._
import java.util._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.data._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._

class index extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.4*/("""

"""),_display_(/*3.2*/main("EtherVisu")/*3.19*/ {_display_(Seq[Any](format.raw/*3.21*/("""
  """),format.raw/*4.3*/("""<ul></ul>

<script>
  var socket = new WebSocket(
			((window.location.protocol === "https:") ? "wss://" : "ws://") +
			window.location.host + "/socket");

  socket.onmessage = function (event) """),format.raw/*11.39*/("""{"""),format.raw/*11.40*/("""
    """),format.raw/*12.5*/("""var str = JSON.stringify(JSON.parse(event.data), null, 4);
    $('ul').append("<li><code><pre>" + str + "</pre></code></li>");
  """),format.raw/*14.3*/("""}"""),format.raw/*14.4*/(""";
</script>
""")))}),format.raw/*16.2*/("""
"""))
      }
    }
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}


}

/**/
object index extends index_Scope0.index
              /*
                  -- GENERATED --
                  DATE: Sun May 14 16:50:06 CEST 2017
                  SOURCE: /home/madsen/idea-workspace/play-java-seed/app/views/index.scala.html
                  HASH: 6683ed8a8d1d76ab18585dd2de39678ed51d341f
                  MATRIX: 738->1|834->3|862->6|887->23|926->25|955->28|1178->223|1207->224|1239->229|1395->358|1423->359|1466->372
                  LINES: 27->1|32->1|34->3|34->3|34->3|35->4|42->11|42->11|43->12|45->14|45->14|47->16
                  -- GENERATED --
              */
          