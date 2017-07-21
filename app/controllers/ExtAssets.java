package controllers;

import play.Environment;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * Controller for loading of external files (outside of jar)
 * <p>
 * Created by Kristian Lange on 2017.
 */
@Singleton
public class ExtAssets extends Controller {

    private static final Logger.ALogger LOGGER = Logger.of(ExtAssets.class);

    private final Environment environment;

    @Inject
    public ExtAssets(Environment environment) {
        this.environment = environment;
    }

    public Result at(String filePath) {
        String rootPath = environment.rootPath().getAbsolutePath();
        if (rootPath.endsWith("bin") || rootPath.endsWith("bin/")) {
            rootPath = environment.rootPath().getParent();
        }
        File file = new java.io.File(rootPath + filePath);
        LOGGER.info("Loading external asset file " + file.getAbsolutePath());
        return ok(file);
    }
}
