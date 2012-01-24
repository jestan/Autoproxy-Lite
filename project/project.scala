import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.scala.incubator"
  val buildVersion      = "2.9.1"
  val buildScalaVersion = "2.9.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt  := ShellPrompt.buildShellPrompt
  )
}

object ShellPrompt {
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }
  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
  )

  val buildShellPrompt = { 
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

object Resolvers {
  val scalaToolsSnapshots = "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

  val allResolvers = Seq(scalaToolsSnapshots)
}

object PluginBuild extends Build {
  import Resolvers._
  import BuildSettings._

  lazy val root = Project(
    id = "autoproxy-lite",
    base = file(".")
  ) aggregate(annotation, plugin, examples)

  lazy val annotation = Project(
    "annotation",
    file("annotation"),
    settings = buildSettings
  )

  lazy val plugin = Project(
    "plugin",
    file("plugin"),
    settings = buildSettings ++ Seq(
      resolvers := allResolvers,
      libraryDependencies <++= scalaVersion { sv => Seq(
        "org.scala-lang" % "scala-compiler" % sv,
        "org.scala-lang" % "scala-library" % sv,
        "ch.qos.logback" % "logback-classic" % "0.9.26",
        "junit" % "junit" % "4.8.2" % "test->default",
        "org.specs2" %% "specs2" % "1.7.1" % "test->default"
      )}
    )
  ) dependsOn (annotation)

  lazy val examples = Project(
    id = "examples",
    base = file("examples")
  ) aggregate(simpleExamples)
  
  lazy val simpleExamples = Project(
    "simpleExamples",
    file("examples/simple"),
    settings = buildSettings ++ Seq(
      resolvers := allResolvers,
      libraryDependencies += "org.specs2" %% "specs2" % "1.7.1" % "test->default",
      scalacOptions <+= (packagedArtifact in Compile in plugin in packageBin) map
        (jar => "-Xplugin:%s" format jar._2.getAbsolutePath),
      scalacOptions += "-Xplugin-require:autoproxy"
//        "-verbose",
//        "-usejavacp",
//        "-nobootcp",
//        "-Xplugin:plugin/src/test/stub-jar/dynamic-mixin-stub.jar",
//        "-Xprint:generatesynthetics",
//        "-Xprint:lazyvals",
//        "-Ylog:generatesynthetics",
//        "-Ylog:lambdalift",
//        "-Ydebug",
//        "-Yshow-syms"
//        "-Ycheck:generatesynthetics"
//        "-Ycheck:lazyvals"
//        "-Ybrowse:lazyvals"
//        "-Yshow-trees"
//        "-Xplugin-list"
//        "-Xshow-phases"        
    )
  ) dependsOn(annotation, plugin)
}


//TODO: Rewrite to use SBT 0.10 publishing

  ///////////////
  // Publishing
  
//  override def managedStyle = ManagedStyle.Maven
//  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
//  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
//  override def publishAction = super.publishAction && publishCurrentNotes
//  override def extraTags = "scalaj" :: super.extraTags

