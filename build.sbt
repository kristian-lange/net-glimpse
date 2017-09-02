name := """net-glimpse"""

version := "1.7"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-json" % "2.6.0",
  "org.pcap4j" % "pcap4j-core" % "1.7.1",
  "org.pcap4j" % "pcap4j-packetfactory-static" % "1.7.1"
)

// No source docs in distribution
sources in (Compile, doc) := Seq.empty

// No source docs in distribution
publishArtifact in (Compile, packageDoc) := false

// Don't include Java docs to distribution
mappings in Universal := (mappings in Universal).value filter {
  case (file, path) => !path.contains("share/doc")
}