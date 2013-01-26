/**
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package ch.ledcom.maven.sitespeed;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "sitespeed")
public class SiteSpeedMojo extends AbstractMojo {

	@Parameter(property = "siteSpeed.siteSpeePath", required = true)
	private File siteSpeedPath;

	@Parameter(property = "siteSpeed.phantomJSPath", required = true)
	private File phantomJSPath;

	@Parameter(property = "siteSpeed.url", required = true)
	private URL url;

	@Parameter(property = "siteSpeed.outputDir", required = false, defaultValue = "target/sitespeed-result")
	private File outputDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("siteSpeedPath=[" + siteSpeedPath + "]");
		getLog().info("phantomJSPath=[" + phantomJSPath + "]");
		getLog().info("url=[" + url.toExternalForm() + "]");
		getLog().info("outputDir=[" + outputDir + "]");
		try {
			ProcessBuilder pb = new ProcessBuilder(constructCmdarray());
			pb.directory(siteSpeedPath);
			pb.environment().put("PATH",
					getPhantomJSBinDir() + ":" + pb.environment().get("PATH"));
			pb.redirectErrorStream(true);
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				getLog().info(line);
			}
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<String> constructCmdarray() {
		List<String> cmd = new ArrayList<String>();
		cmd.add(getSiteSpeedBin().toString());
		cmd.add("-u");
		cmd.add(url.toExternalForm());
		cmd.add("-r");
		cmd.add(outputDir.toString());
		return cmd;
	}

	private File getPhantomJSBinDir() {
		return new File(phantomJSPath, "bin");
	}

	private File getSiteSpeedBin() {
		return new File(siteSpeedPath, "sitespeed.io");
	}
}
