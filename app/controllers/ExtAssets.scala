package controllers

import java.io.File
import javax.inject.Inject

import play.Environment
import play.api.mvc.{Action, AnyContent, Controller}

/**
  * Controller for loading of external files (outside of jar)
  *
  * It's an altered version of [[ExternalAssets]] that allows to serve files
  * even in production mode. Since this could be used to deliver ANY file from
  * the local file system it's important to specify the file's name in the
  * 'routes' file and NOT to use any '*'.
  *
  * All assets are served with max-age=3600 cache directive.
  *
  * You can use this controller in any application, just by declaring the appropriate route. For example:
  * {{{
  * GET     /assets/\uFEFF*file               controllers.ExternalAssets.at(path="/home/peter/myplayapp/external", file)
  * GET     /assets/\uFEFF*file               controllers.ExternalAssets.at(path="C:\external", file)
  * GET     /assets/\uFEFF*file               controllers.ExternalAssets.at(path="relativeToYourApp", file)
  * }}}
  */
class ExtAssets @Inject()(environment: Environment) extends Controller {

  val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r

  /**
    * Generates an `Action` that serves a static resource from an external folder
    *
    * @param rootPath the root folder for searching the static resource files such as `"/home/peter/public"`, `C:\external` or `relativeToYourApp`
    * @param file     the file part extracted from the URL
    */
  def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>
    val fileToServe = rootPath match {
      case AbsolutePath(_) => new File(rootPath, file)
      case _ => new File(environment.getFile(rootPath), file)
    }

    if (fileToServe.exists) {
      Ok.sendFile(fileToServe, inline = true).withHeaders(CACHE_CONTROL -> "max-age=3600")
    } else {
      NotFound
    }
  }


}
