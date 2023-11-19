import xerial.sbt.Sonatype.GitHubHosting

val algebra = "org.endpoints4s" %% "algebra" % "1.10.0"
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
    scalaVersion := "2.13.8",
    crossScalaVersions := Seq("2.13.8", "3.0.2", "2.12.13"),
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
        algebra,
        openapi,
        algebraTestkit % Test,
        algebraCirceTestkit % Test,
        jsonSchemaGeneric % Test,
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
      excludeDependencies ++= {
        if (scalaBinaryVersion.value.startsWith("3")) {
          List(ExclusionRule("org.scala-lang.modules", "scala-collection-compat_2.13"))
        } else Nil
      }
    )

val `akka-http-server` =
  project
    .in(file("server"))
    .settings(
      name := "akka-http-server",
      publish / skip := scalaBinaryVersion.value.startsWith("3"),
      libraryDependencies ++= Seq(
        algebra,
        openapi,
        algebraTestkit % Test,
        algebraCirceTestkit % Test,
        jsonSchemaGeneric % Test,
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
      excludeDependencies ++= {
        if (scalaBinaryVersion.value.startsWith("3")) {
          List(
            ExclusionRule("org.scala-lang.modules", "scala-collection-compat_2.13")
          )
        } else Nil
      },
      versionPolicyIgnored ++= Seq(
        // Was removed from akka-http https://github.com/akka/akka-http/pull/3849
        "com.twitter" % "hpack"
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
