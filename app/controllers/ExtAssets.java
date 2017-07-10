package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;

public class ExtAssets extends Controller {

	public Result versioned(String filePath) {
		File file = new File(filePath);
		return ok(file, true);
	}
}
