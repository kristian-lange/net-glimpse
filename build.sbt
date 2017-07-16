name := """net-glimpse"""

version := "1.3"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  javaCore,
  "org.pcap4j" % "pcap4j-core" % "1.7.0",
  "org.pcap4j" % "pcap4j-packetfactory-static" % "1.7.0"
)

// No source docs in distribution
sources in (Compile, doc) := Seq.empty

// No source docs in distribution
publishArtifact in (Compile, packageDoc) := false

// Don't include Java docs to distribution
mappings in Universal := (mappings in Universal).value filter {
  case (file, path) => !path.contains("share/doc")
}