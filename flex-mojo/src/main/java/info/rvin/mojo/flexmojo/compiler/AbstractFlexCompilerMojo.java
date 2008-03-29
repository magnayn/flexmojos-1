/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package info.rvin.mojo.flexmojo.compiler;

import info.rvin.mojo.flexmojo.AbstractIrvinMojo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import flex2.tools.oem.Builder;
import flex2.tools.oem.Configuration;
import flex2.tools.oem.Report;

public abstract class AbstractFlexCompilerMojo<E extends Builder> extends
		AbstractIrvinMojo {

	/**
	 * output performance benchmark
	 * 
	 * @parameter default-value="false"
	 */
	private boolean benchmark;

	/**
	 * Turn on generation of accessible SWFs.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean accessible;

	/**
	 * Sets the locales that the compiler uses to replace <code>{locale}</code>
	 * tokens that appear in some configuration values. This is equivalent to
	 * using the <code>compiler.locale</code> option of the mxmlc or compc
	 * compilers.
	 * 
	 * @parameter
	 */
	private String[] locales;

	/**
	 * List of path elements that form the roots of ActionScript class
	 * hierarchies.
	 * 
	 * @parameter
	 */
	protected File[] sourcePaths;

	/**
	 * Allow the source-path to have path-elements which contain other
	 * path-elements
	 * 
	 * @parameter default-value="false"
	 */
	private boolean allowSourcePathOverlap;

	/**
	 * Run the AS3 compiler in a mode that detects legal but potentially
	 * incorrect code
	 * 
	 * @parameter default-value="true"
	 */
	private boolean showWarnings;

	/**
	 * Enables checking of the following ActionScript warnings:
	 * 
	 * <pre>
	 * --compiler.warn-array-tostring-changes
	 * --compiler.warn-assignment-within-conditional
	 * --compiler.warn-bad-array-cast
	 * --compiler.warn-bad-bool-assignment
	 * --compiler.warn-bad-date-cast
	 * --compiler.warn-bad-es3-type-method
	 * --compiler.warn-bad-es3-type-prop
	 * --compiler.warn-bad-nan-comparison
	 * --compiler.warn-bad-null-assignment
	 * --compiler.warn-bad-null-comparison
	 * --compiler.warn-bad-undefined-comparison
	 * --compiler.warn-boolean-constructor-with-no-args
	 * --compiler.warn-changes-in-resolve
	 * --compiler.warn-class-is-sealed
	 * --compiler.warn-const-not-initialized
	 * --compiler.warn-constructor-returns-value
	 * --compiler.warn-deprecated-event-handler-error
	 * --compiler.warn-deprecated-function-error
	 * --compiler.warn-deprecated-property-error
	 * --compiler.warn-duplicate-argument-names
	 * --compiler.warn-duplicate-variable-def
	 * --compiler.warn-for-var-in-changes
	 * --compiler.warn-import-hides-class
	 * --compiler.warn-instance-of-changes
	 * --compiler.warn-internal-error
	 * --compiler.warn-level-not-supported
	 * --compiler.warn-missing-namespace-decl
	 * --compiler.warn-negative-uint-literal
	 * --compiler.warn-no-constructor
	 * --compiler.warn-no-explicit-super-call-in-constructor
	 * --compiler.warn-no-type-decl
	 * --compiler.warn-number-from-string-changes
	 * --compiler.warn-scoping-change-in-this
	 * --compiler.warn-slow-text-field-addition
	 * --compiler.warn-unlikely-function-value
	 * --compiler.warn-xml-class-has-changed
	 * </pre>
	 * 
	 * @see Warning
	 */
	private Warning warnigs;

	/**
	 * Turn on generation of debuggable SWFs. False by default for mxmlc, but
	 * true by default for compc.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean debug;

	/**
	 * A password that is embedded in the application
	 * 
	 * @parameter
	 */
	private String debugPassword;

	/**
	 * Turn on writing of generated/*.as files to disk. These files are
	 * generated by the compiler during mxml translation and are helpful with
	 * understanding and debugging Flex applications.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean keepGeneratedActionscript;

	/**
	 * Specify a URI to associate with a manifest of components for use as MXML
	 * elements.
	 * 
	 * @parameter
	 */
	private Namespace[] namespaces;

	/**
	 * Enable post-link SWF optimization.
	 * 
	 * @parameter default-value="true"
	 */
	private boolean optimize;

	/**
	 * If the <code>incremental</code> input argument is <code>false</code>,
	 * this method recompiles all parts of the object. If the
	 * <code>incremental</code> input argument is <code>true</code>, this
	 * method compiles only the parts of the object that have changed since the
	 * last compilation.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean incremental;

	/**
	 * Keep the following AS3 metadata in the bytecodes.
	 * 
	 * @parameter
	 */
	private String[] keepAs3Metadatas;

	/**
	 * Run the AS3 compiler in strict error checking mode.
	 * 
	 * @parameter default-value="true"
	 */
	private boolean strict;

	/**
	 * Use the ActionScript 3 class based object model for greater performance
	 * and better error reporting. In the class based object model most built-in
	 * functions are implemented as fixed methods of classes (-strict is
	 * recommended, but not required, for earlier errors)
	 * 
	 * @parameter default-value="true"
	 */
	private boolean as3;

	/**
	 * Use the ECMAScript edition 3 prototype based object model to allow
	 * dynamic overriding of prototype properties. In the prototype based object
	 * model built-in functions are implemented as dynamic properties of
	 * prototype objects (-strict is allowed, but may result in compiler errors
	 * for references to dynamic properties)
	 * 
	 * @parameter default-value="false"
	 */
	private boolean es;

	/**
	 * Turns on the display of stack traces for uncaught runtime errors.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean verboseStacktraces;

	/**
	 * FIXME need javadoc
	 * 
	 * @parameter
	 */
	private Font fonts;

	/**
	 * Enables SWFs to access the network.
	 * 
	 * @parameter default-value="true"
	 */
	private boolean useNetwork;

	/**
	 * licenses: specifies a list of product and serial number pairs.
	 * 
	 * @parameter
	 */
	private Map<String, String> licenses;

	/**
	 * Sets the context root path so that the compiler can replace
	 * <code>{context.root}</code> tokens for service channel endpoints.
	 * 
	 * @parameter default-value=""
	 */
	private String contextRoot;

	/**
	 * Uses the default compiler options as base
	 * 
	 * @parameter default-value="false"
	 */
	private boolean linkReport;

	/**
	 * Sets a list of artifacts to omit from linking when building an
	 * application. This is equivalent to using the <code>load-externs</code>
	 * option of the mxmlc or compc compilers.
	 * 
	 * @parameter
	 */
	private MavenArtifact[] loadExterns;

	/**
	 * Prints a list of resource bundles to a file for input to the compc
	 * compiler to create a resource bundle SWC file.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean listResourceBundle;

	/**
	 * Load a file containing configuration options
	 * 
	 * @parameter
	 */
	private File configFile;

	/**
	 * The filename of the SWF movie to create
	 * 
	 * @parameter
	 */
	private String output;

	/**
	 * specifies the version of the player the application is targeting.
	 * Features requiring a later version will not be compiled into the
	 * application. The minimum value supported is "9.0.0".
	 * 
	 * @parameter
	 */
	private String targetPlayer;

	/**
	 * Sets the metadata section of the application SWF. This is equivalent to
	 * the <code>raw-metadata</code> option of the mxmlc or compc compilers.
	 * 
	 * Need a well-formed XML fragment
	 * 
	 * @parameter
	 */
	private String metadata;

	/**
	 * Define the base path to all RSL libraries.
	 * 
	 * Accept some special tokens:
	 * 
	 * <pre>
	 * {contextRoot}		- replace by defined context root
	 * {groupId}			- replace by library groupId
	 * {artifactId}			- replace by library artifactId
	 * {version}			- replace by library version
	 * </pre>
	 * 
	 * For example:
	 * 
	 * <pre>
	 * 	${contextRoot}/rsl/${artifactId}.swf
	 * 	http://www.mydomain.com/myflexapplication/${artifactId}.swf
	 * </pre>
	 * 
	 * @parameter default-value="/{contextRoot}/rsl/{artifactId}-{version}.swf"
	 * 
	 */
	protected String rslPath;

	/**
	 * Previous compilation data, used to incremental builds
	 */
	private File compilationData;

	/**
	 * TODO
	 */
	protected E builder;

	/**
	 * Compiled file
	 */
	protected File outputFile;

	/**
	 * Flex OEM compiler configurations
	 */
	protected Configuration configuration;

	public AbstractFlexCompilerMojo() {
		super();
	}

	@Override
	public void setUp() throws MojoExecutionException, MojoFailureException {
		if (sourcePaths == null) {
			getLog()
					.info(
							"sourcePaths CoC, using source directory plus resources directory!");
			List<File> files = new ArrayList<File>();

			File source = new File(build.getSourceDirectory());
			if (source.exists()) {
				files.add(source);
			}

			List<Resource> resources = getResources();
			for (Resource resource : resources) {
				File resourceFile = new File(resource.getDirectory());
				if (resourceFile.exists()) {
					files.add(resourceFile);
				}
			}

			sourcePaths = files.toArray(new File[files.size()]);
		}

		if (output == null) {
			outputFile = new File(build.getDirectory(), build.getFinalName()
					+ "." + project.getPackaging());
		} else {
			outputFile = new File(build.getDirectory(), output);
		}

		if (configFile == null) {
			List<Resource> resources = getResources();
			for (Resource resource : resources) {
				File cfg = new File(resource.getDirectory(),
						getConfigFileName());
				if (cfg.exists()) {
					configFile = cfg;
					break;
				}
			}
		}
		if (configFile == null) {
			URL url = getClass().getResource("/configs/" + getConfigFileName());
			configFile = new File(build.getDirectory(), getConfigFileName());
			urlToFile(url, configFile);
		}
		if (!configFile.exists()) {
			throw new MojoExecutionException("Unable to find " + configFile);
		}

		if (fonts == null) {
			fonts = new Font();
			String os = System.getProperty("os.name").toLowerCase();
			URL url;
			if (os.contains("mac")) {
				url = getClass().getResource("/fonts/macFonts.ser");
			} else {
				// And linux?!
				// if(os.contains("windows")) {
				url = getClass().getResource("/fonts/winFonts.ser");
			}
			File fontsSer = new File(build.getDirectory(), "fonts.ser");
			urlToFile(url, fontsSer);
			fonts.setLocalFontsSnapshot(fontsSer);
		}

		if (rslPath == null) {
			rslPath = contextRoot;
		}

		if (!rslPath.startsWith("/")) {
			rslPath += '/';
		}

		if (!rslPath.endsWith("/")) {
			rslPath += '/';
		}

		configuration = builder.getDefaultConfiguration();
		configure();

		compilationData = new File(build.getDirectory(), project
				.getArtifactId()
				+ "-" + project.getVersion() + ".incr");
	}

	protected String getConfigFileName() {
		return "flex-config.xml";
	}

	private void urlToFile(URL url, File file) throws MojoExecutionException {
		try {
			FileUtils.copyURLToFile(url, file);
		} catch (IOException e) {
			throw new MojoExecutionException("?!");
		}
		file.deleteOnExit();
	}

	public void run() throws MojoExecutionException, MojoFailureException {
		builder.setLogger(new CompileLogger(getLog()));

		builder.setConfiguration(configuration);

		long bytes;
		try {
			if (incremental && compilationData.exists()) {
				builder.load(loadCompilationData());
			}
			bytes = builder.build(incremental);
			if (incremental) {
				if (compilationData.exists()) {
					compilationData.delete();
					compilationData.createNewFile();
				}

				builder.save(saveCompilationData());
			}
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (MojoExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
		if (bytes == 0) {
			throw new MojoFailureException("Error compiling!");
		}
	}

	private OutputStream saveCompilationData() throws MojoExecutionException {
		try {
			return new BufferedOutputStream(new FileOutputStream(
					compilationData));
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Can't save compilation data.");
		}
	}

	private InputStream loadCompilationData() throws MojoExecutionException {
		try {
			return new BufferedInputStream(new FileInputStream(compilationData));
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException(
					"Previows compilation data not found.");
		}
	}

	protected void configure() throws MojoExecutionException {
		configuration.setExternalLibraryPath(getDependenciesPath("external"));

		configuration.includeLibraries(getDependenciesPath("internal"));

		configuration.setLibraryPath(getDependenciesPath("compile"));
		configuration.addLibraryPath(getDependenciesPath("merged"));
		configuration.addLibraryPath(getResourcesBundles());

		configuration
				.setRuntimeSharedLibraries(getRSLPaths(getDependencyArtifacts("runtime")));
		configuration.addExternalLibraryPath(getDependenciesPath("runtime"));

		configuration.setTheme(getDependenciesPath("theme"));

		configuration.enableAccessibility(accessible);
		configuration.allowSourcePathOverlap(allowSourcePathOverlap);
		configuration.useActionScript3(as3);
		configuration.enableDebugging(debug, debugPassword);
		configuration.useECMAScript(es);

		// Fonts
		if (fonts != null) {
			configuration.enableAdvancedAntiAliasing(fonts
					.isAdvancedAntiAliasing());
			configuration.setFontManagers(fonts.getManagers());
			configuration.setMaximumCachedFonts(fonts.getMaxCachedFonts());
			configuration.setMaximumGlyphsPerFace(fonts.getMaxGlyphsPerFace());
			// configuration.setFontLanguageRange(null, null);
			// FIXME how to use this
			configuration.setLocalFontSnapshot(fonts.getLocalFontsSnapshot());
		}

		configuration.setActionScriptMetadata(keepAs3Metadatas);
		configuration
				.keepCompilerGeneratedActionScript(keepGeneratedActionscript);

		if (licenses != null) {
			for (String licenseName : licenses.keySet()) {
				String key = licenses.get(licenseName);
				configuration.setLicense(licenseName, key);
			}
		}

		// When using the resource-bundle-list option, you must also set the
		// value of the locale option to an empty string.
		if (listResourceBundle) {
			configuration.setLocale(new String[0]);
		} else {
			configuration.setLocale(locales);
		}

		if (namespaces != null) {
			for (Namespace namespace : namespaces) {
				configuration.setComponentManifest(namespace.getUri(),
						namespace.getManifest());
			}
		}

		configuration.optimize(optimize);
		if (this.warnigs != null) {
			configureWarnings(configuration);
		}

		configuration.setSourcePath(sourcePaths);
		configuration.enableStrictChecking(strict);
		configuration.useNetwork(useNetwork);
		configuration.enableVerboseStacktraces(verboseStacktraces);

		if (contextRoot != null) {
			configuration.setContextRoot(contextRoot);
		}
		configuration.keepLinkReport(linkReport);
		configuration.setConfiguration(configFile);

		if (loadExterns != null) {
			List<File> externsFiles = new ArrayList<File>();

			for (MavenArtifact mvnArtifact : loadExterns) {
				Artifact artifact = artifactFactory
						.createArtifactWithClassifier(mvnArtifact.getGroupId(),
								mvnArtifact.getArtifactId(), mvnArtifact
										.getVersion(), "xml", "link-report");
				resolveArtifact(artifact);
				externsFiles.add(artifact.getFile());
			}
			configuration.setExterns(externsFiles.toArray(new File[externsFiles
					.size()]));

			if (metadata != null) {
				configuration.setSWFMetaData(metadata);
			}
		}

	}

	private String[] getRSLPaths(List<Artifact> artifacts) {
		List<String> rsls = new ArrayList<String>();

		for (Artifact artifact : artifacts) {
			String rsl = rslPath;
			rsl = rsl.replace("{contextRoot}", contextRoot);
			rsl = rsl.replace("{groupId}", artifact.getGroupId());
			rsl = rsl.replace("{artifactId}", artifact.getArtifactId());
			rsl = rsl.replace("{version}", artifact.getVersion());
			rsls.add(rsl);
		}

		return rsls.toArray(new String[rsls.size()]);
	}

	private File[] getResourcesBundles() throws MojoExecutionException {
		if (locales == null || locales.length == 0) {
			return new File[0];
		}

		List<File> resouceBundles = new ArrayList<File>();
		for (Artifact artifact : getDependencyArtifacts()) {
			for (String locale : locales) {
				Artifact rbArtifact = artifactFactory
						.createArtifactWithClassifier(artifact.getGroupId(),
								artifact.getArtifactId(),
								artifact.getVersion(), "rb", locale);
				try {
					resolveArtifact(rbArtifact);
					resouceBundles.add(rbArtifact.getFile());
				} catch (MojoExecutionException e) {
					// Dont found? SKIP =D
					continue;
				}
			}
		}
		return resouceBundles.toArray(new File[resouceBundles.size()]);
	}

	private File[] getDependenciesPath(String scope)
			throws MojoExecutionException {
		if (scope == null)
			return null;

		List<File> files = new ArrayList<File>();
		for (Artifact a : getDependencyArtifacts(scope)) {
			//https://bugs.adobe.com/jira/browse/SDK-15073
			//Workarround begin
			File dest = new File(build.getDirectory(),"libraries/"+ scope +"/" +a.getArtifactId() + ".swc");
			try {
				FileUtils.copyFile(a.getFile(), dest);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
			files.add(dest);
			//Workarround end
			//files.add(a.getFile());
		}
		return files.toArray(new File[files.size()]);
	}

	@Override
	protected void tearDown() throws MojoExecutionException,
			MojoFailureException {
		project.getArtifact().setFile(outputFile);
		if (linkReport) {
			writeLinkReport(builder.getReport());
		}
		if (listResourceBundle) {
			writeResourceBundleList(builder.getReport());
		}

	}

	private void configureWarnings(Configuration cfg) {
		cfg.showActionScriptWarnings(showWarnings);
		cfg.showBindingWarnings(warnigs.getBinding());
		cfg.showDeprecationWarnings(warnigs.getDeprecation());
		cfg.showShadowedDeviceFontWarnings(warnigs.getShadowedDeviceFont());
		cfg.showUnusedTypeSelectorWarnings(warnigs.getUnusedTypeSelector());
		cfg.checkActionScriptWarning(Configuration.WARN_ARRAY_TOSTRING_CHANGES,
				warnigs.getArrayTostringChanges());
		cfg.checkActionScriptWarning(
				Configuration.WARN_ASSIGNMENT_WITHIN_CONDITIONAL, warnigs
						.getAssignmentWithinConditional());
		cfg.checkActionScriptWarning(Configuration.WARN_BAD_ARRAY_CAST, warnigs
				.getBadArrayCast());
		cfg.checkActionScriptWarning(Configuration.WARN_BAD_BOOLEAN_ASSIGNMENT,
				warnigs.getBadBooleanAssignment());
		cfg.checkActionScriptWarning(Configuration.WARN_BAD_DATE_CAST, warnigs
				.getBadDateCast());
		cfg.checkActionScriptWarning(Configuration.WARN_BAD_ES3_TYPE_METHOD,
				warnigs.getBadEs3TypeMethod());
		cfg.checkActionScriptWarning(Configuration.WARN_BAD_ES3_TYPE_PROP,
				warnigs.getBadEs3TypeProp());
		cfg.checkActionScriptWarning(Configuration.WARN_BAD_NAN_COMPARISON,
				warnigs.getBadNanComparison());
		cfg.checkActionScriptWarning(Configuration.WARN_BAD_NULL_ASSIGNMENT,
				warnigs.getBadNullAssignment());
		cfg.checkActionScriptWarning(Configuration.WARN_BAD_NULL_COMPARISON,
				warnigs.getBadNullComparison());
		cfg.checkActionScriptWarning(
				Configuration.WARN_BAD_UNDEFINED_COMPARISON, warnigs
						.getBadUndefinedComparison());
		cfg.checkActionScriptWarning(
				Configuration.WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS, warnigs
						.getBooleanConstructorWithNoArgs());
		cfg.checkActionScriptWarning(Configuration.WARN_CHANGES_IN_RESOLVE,
				warnigs.getChangesInResolve());
		cfg.checkActionScriptWarning(Configuration.WARN_CLASS_IS_SEALED,
				warnigs.getClassIsSealed());
		cfg.checkActionScriptWarning(Configuration.WARN_CONST_NOT_INITIALIZED,
				warnigs.getConstNotInitialized());
		cfg.checkActionScriptWarning(
				Configuration.WARN_CONSTRUCTOR_RETURNS_VALUE, warnigs
						.getConstructorReturnsValue());
		cfg.checkActionScriptWarning(
				Configuration.WARN_DEPRECATED_EVENT_HANDLER_ERROR, warnigs
						.getDeprecatedEventHandlerError());
		cfg.checkActionScriptWarning(
				Configuration.WARN_DEPRECATED_FUNCTION_ERROR, warnigs
						.getDeprecatedFunctionError());
		cfg.checkActionScriptWarning(
				Configuration.WARN_DEPRECATED_PROPERTY_ERROR, warnigs
						.getDeprecatedPropertyError());
		cfg.checkActionScriptWarning(
				Configuration.WARN_DUPLICATE_ARGUMENT_NAMES, warnigs
						.getDuplicateArgumentNames());
		cfg.checkActionScriptWarning(Configuration.WARN_DUPLICATE_VARIABLE_DEF,
				warnigs.getDuplicateVariableDef());
		cfg.checkActionScriptWarning(Configuration.WARN_FOR_VAR_IN_CHANGES,
				warnigs.getForVarInChanges());
		cfg.checkActionScriptWarning(Configuration.WARN_IMPORT_HIDES_CLASS,
				warnigs.getImportHidesClass());
		cfg.checkActionScriptWarning(Configuration.WARN_INSTANCEOF_CHANGES,
				warnigs.getInstanceOfChanges());
		cfg.checkActionScriptWarning(Configuration.WARN_INTERNAL_ERROR, warnigs
				.getInternalError());
		cfg.checkActionScriptWarning(Configuration.WARN_LEVEL_NOT_SUPPORTED,
				warnigs.getLevelNotSupported());
		cfg.checkActionScriptWarning(Configuration.WARN_MISSING_NAMESPACE_DECL,
				warnigs.getMissingNamespaceDecl());
		cfg.checkActionScriptWarning(Configuration.WARN_NEGATIVE_UINT_LITERAL,
				warnigs.getNegativeUintLiteral());
		cfg.checkActionScriptWarning(Configuration.WARN_NO_CONSTRUCTOR, warnigs
				.getNoConstructor());
		cfg.checkActionScriptWarning(
				Configuration.WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR,
				warnigs.getNoExplicitSuperCallInConstructor());
		cfg.checkActionScriptWarning(Configuration.WARN_NO_TYPE_DECL, warnigs
				.getNoTypeDecl());
		cfg.checkActionScriptWarning(
				Configuration.WARN_NUMBER_FROM_STRING_CHANGES, warnigs
						.getNumberFromStringChanges());
		cfg.checkActionScriptWarning(Configuration.WARN_SCOPING_CHANGE_IN_THIS,
				warnigs.getScopingChangeInThis());
		cfg.checkActionScriptWarning(
				Configuration.WARN_SLOW_TEXTFIELD_ADDITION, warnigs
						.getSlowTextFieldAddition());
		cfg.checkActionScriptWarning(
				Configuration.WARN_UNLIKELY_FUNCTION_VALUE, warnigs
						.getUnlikelyFunctionValue());
		cfg.checkActionScriptWarning(Configuration.WARN_XML_CLASS_HAS_CHANGED,
				warnigs.getXmlClassHasChanged());
	}

	private void writeResourceBundleList(Report report)
			throws MojoExecutionException {
		File resourceBundle = new File(build.getDirectory(), project
				.getArtifactId()
				+ "-" + project.getVersion() + "-resourceBundle.properties");
		Writer writer;
		try {
			writer = new FileWriter(resourceBundle);
			String[] bundles = report.getResourceBundleNames();
			if (bundles != null) {
				for (String bundle : bundles) {
					writer.write(bundle);
					writer.write(' ');
				}
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new MojoExecutionException(
					"An error has ocurried while recording resourceBundle list",
					e);
		}
		projectHelper.attachArtifact(project, "properties", "resource-bundle",
				resourceBundle);
	}

	private void writeLinkReport(Report report) throws MojoExecutionException {
		File linkReport = new File(build.getDirectory(), project
				.getArtifactId()
				+ "-" + project.getVersion() + "-link-report.xml");

		Writer writer;
		try {
			writer = new FileWriter(linkReport);
			report.writeLinkReport(writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new MojoExecutionException(
					"An error has ocurried while recording link-report", e);
		}

		projectHelper.attachArtifact(project, "xml", "link-report", linkReport);
	}

}