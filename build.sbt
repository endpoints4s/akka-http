import xerial.sbt.Sonatype.GitHubHosting
import com.lightbend.paradox.markdown.Writer

val algebraTestkit = "org.endpoints4s" %% "algebra-testkit" % "4.1.0"
val algebraCirceTestkit = "org.endpoints4s" %% "algebra-circe-testkit" % "4.1.0"
val jsonSchemaGeneric = "org.endpoints4s" %% "json-schema-generic" % "1.10.0"
val openapi = "org.endpoints4s" %% "openapi" % "4.4.0"

val akkaActorVersion = "2.6.17"
val akkaHttpVersion = "10.2.6"

inThisBuild(
  List(
    versionPolicyIntention := Compatibility.BinaryAndSourceCompatible,
    organization := "org.endpoints4s",
    sonatypeProjectHosting := Some(
      GitHubHosting("endpoints4s", "akka-http", "julien@richard-foy.fr")
    ),
    homepage := Some(sonatypeProjectHosting.value.get.scmInfo.browseUrl),
    licenses := Seq(
      "MIT License" -> url("http://opensource.org/licenses/mit-license.php")
    ),
    developers := List(
      Developer(
        "julienrf",
        "Julien Richard-Foy",
        "julien@richard-foy.fr",
        url("http://julien.richard-foy.fr")
      )
    ),
    scalaVersion := "2.13.10",
    crossScalaVersions := Seq("2.13.10", "3.1.3", "2.12.13"),
    versionPolicyIgnoredInternalDependencyVersions := Some("^\\d+\\.\\d+\\.\\d+\\+\\d+".r)
  )
)

val `akka-http-client` =
  project
    .in(file("client"))
    .settings(
      name := "akka-http-client",
      publish / skip := scalaBinaryVersion.value.startsWith("3"),
      libraryDependencies ++= Seq(
        openapi.cross(
          CrossVersion.for3Use2_13
        ),
        (algebraTestkit % Test).cross(
          CrossVersion.for3Use2_13
        ),
        (algebraCirceTestkit % Test).cross(
          CrossVersion.for3Use2_13
        ),
        (jsonSchemaGeneric % Test).cross(
          CrossVersion.for3Use2_13
        ),
        ("com.typesafe.akka" %% "akka-stream" % akkaActorVersion % Provided).cross(
          CrossVersion.for3Use2_13
        ),
        ("com.typesafe.akka" %% "akka-http" % akkaHttpVersion).cross(CrossVersion.for3Use2_13),
        ("com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test).cross(
          CrossVersion.for3Use2_13
        ),
        ("com.typesafe.akka" %% "akka-stream-testkit" % akkaActorVersion % Test).cross(
          CrossVersion.for3Use2_13
        )
      ),
    )

val `akka-http-server` =
  project
    .in(file("server"))
    .settings(
      name := "akka-http-server",
      publish / skip := scalaBinaryVersion.value.startsWith("3"),
      libraryDependencies ++= Seq(
        openapi.cross(
          CrossVersion.for3Use2_13
        ),
        (algebraTestkit % Test).cross(
          CrossVersion.for3Use2_13
        ),
        (algebraCirceTestkit % Test).cross(
          CrossVersion.for3Use2_13
        ),
        (jsonSchemaGeneric % Test).cross(
          CrossVersion.for3Use2_13
        ),
        ("com.typesafe.akka" %% "akka-http" % akkaHttpVersion).cross(CrossVersion.for3Use2_13),
        ("com.typesafe.akka" %% "akka-stream" % akkaActorVersion % Provided).cross(
          CrossVersion.for3Use2_13
        ),
        ("com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test).cross(
          CrossVersion.for3Use2_13
        ),
        ("com.typesafe.akka" %% "akka-stream-testkit" % akkaActorVersion % Test).cross(
          CrossVersion.for3Use2_13
        ),
        ("com.typesafe.akka" %% "akka-testkit" % akkaActorVersion % Test).cross(
          CrossVersion.for3Use2_13
        )
      ),
      versionPolicyIgnored ++= Seq(
        // Was removed from akka-http https://github.com/akka/akka-http/pull/3849
        "com.twitter" % "hpack"
      )
    )

val documentation =
  project.in(file("documentation"))
    .enablePlugins(ParadoxMaterialThemePlugin, ParadoxPlugin, ParadoxSitePlugin, ScalaUnidocPlugin, SitePreviewPlugin)
    .settings(
      publish / skip := true,
      coverageEnabled := false,
      autoAPIMappings := true,
      Compile / paradoxMaterialTheme := {
        val theme = (Compile / paradoxMaterialTheme).value
        val repository =
          (ThisBuild / sonatypeProjectHosting).value.get.scmInfo.browseUrl.toURI
        theme
          .withRepository(repository)
          .withSocial(repository)
          .withCustomStylesheet("snippets.css")
      },
      paradoxProperties ++= Map(
        "version" -> version.value,
        "scaladoc.base_url" -> s".../${(packageDoc / siteSubdirName).value}",
        "github.base_url" -> s"${homepage.value.get}/blob/v${version.value}"
      ),
      paradoxDirectives += ((_: Writer.Context) =>
        org.endpoints4s.paradox.coordinates.CoordinatesDirective
        ),
      ScalaUnidoc / unidoc / scalacOptions ++= Seq(
        "-implicits",
        "-diagrams",
        "-groups",
        "-doc-source-url",
        s"${homepage.value.get}/blob/v${version.value}€{FILE_PATH}.scala",
        "-sourcepath",
        (ThisBuild / baseDirectory).value.absolutePath
      ),
      ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(
        `akka-http-server`, `akka-http-client`
      ),
      packageDoc / siteSubdirName := "api",
      addMappingsToSiteDir(
        ScalaUnidoc / packageDoc / mappings,
        packageDoc / siteSubdirName
      )
    )

val `akka-http` =
  project
    .in(file("."))
    .aggregate(
      `akka-http-server`,
      `akka-http-client`
    )
    .settings(
      publish / skip := true
    )

Global / onChangedBuildSource := ReloadOnSourceChanges
