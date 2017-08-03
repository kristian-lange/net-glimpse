package controllers

import java.io.File
import javax.inject.Inject

import play.Environment
import play.api.Logger
import play.api.mvc.{Action, Controller}

/**
  * Controller for loading of external files (outside of jar)
  *
  * Created by Kristian Lange in 2017.
  *
  */
class ExtAssets @Inject()(environment: Environment) extends Controller {

  private val logger: Logger = Logger(this.getClass)

  /**
    * Generates an `Action` that serves a static resource from within the
    * application's folder or if the application was started from the bin/
    * folder it uses the parent (important for Windows)
    *
    * @param filePath the file path
    */
  def at(filePath: String) = Action { _ =>
    var rootPath = environment.rootPath.getAbsolutePath
    if (rootPath.endsWith("bin") || rootPath.endsWith("bin/"))
      rootPath = environment.rootPath.getParent

    val fileToServe = new File(rootPath + filePath)
    if (fileToServe.exists) {
      logger.info("Loading external asset file " + fileToServe.getAbsolutePath)
      Ok.sendFile(fileToServe, inline = true)
    } else {
      logger.info("Couldn't find external asset file " + fileToServe.getAbsolutePath)
      NotFound
    }
  }

}
