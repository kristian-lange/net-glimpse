name := """net-glimps"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  javaCore,
  "org.pcap4j" % "pcap4j-core" % "1.7.0",
  "org.pcap4j" % "pcap4j-packetfactory-static" % "1.7.0"
)
