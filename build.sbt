import sbt.Keys._

val akkaVersion = "2.4.14"
val amqpClientVersion = "4.0.0"

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "objektwerks",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.8",
  ivyScala := ivyScala.value map {
    _.copy(overrideScalaVersion = true)
  },
  javaOptions in compile += "-Xss1m -Xmx2g",
  javaOptions in run += "-Xss1m -Xmx2g",
  scalacOptions ++= Seq(
    "-language:postfixOps",
    "-language:reflectiveCalls",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-feature",
    "-Ywarn-unused-import",
    "-Ywarn-unused",
    "-Ywarn-dead-code",
    "-unchecked",
    "-deprecation",
    "-Xfatal-warnings",
    "-Xlint:missing-interpolator",
    "-Xlint"
  ),
  fork in run := true
)

lazy val integrationTestSettings = Defaults.itSettings ++ Seq(
  compile in IntegrationTest <<= (compile in IntegrationTest) triggeredBy (compile in Test),
  parallelExecution in IntegrationTest := false,
  fork in IntegrationTest := true
)

lazy val testDependencies = {
  Seq(
    "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion % "provided, test",
    "com.typesafe.akka" % "akka-remote_2.11" % akkaVersion % "provided, test",
    "com.typesafe.akka" % "akka-cluster_2.11" % akkaVersion % "provided, test",
    "com.typesafe.akka" % "akka-cluster-metrics_2.11" % akkaVersion % "provided, test",
    "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion % "provided, test",
    "com.rabbitmq" % "amqp-client" % amqpClientVersion % "provided",
    "net.ceedubs" % "ficus_2.11" % "1.1.2" % "provided",
    "com.typesafe.play" % "play-json_2.11" % "2.5.3" % "provided",
    "org.slf4j" % "slf4j-api" % "1.7.21" % "test, it",
    "ch.qos.logback" % "logback-classic" % "1.1.7" % "test, it",
    "org.scalatest" % "scalatest_2.11" % "3.0.1" % "test, it"
  )
}

lazy val clusterDependencies = {
  Seq(
    "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion % "provided",
    "com.typesafe.akka" % "akka-cluster_2.11" % akkaVersion % "provided",
    "com.typesafe.akka" % "akka-cluster-metrics_2.11" % akkaVersion % "provided"
  )
}

lazy val akkaDependencies = {
  Seq(
    "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-remote_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-cluster-metrics_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion,
    "com.rabbitmq" % "amqp-client" % amqpClientVersion,
    "net.ceedubs" % "ficus_2.11" % "1.1.2",
    "com.typesafe.play" % "play-json_2.11" % "2.5.3",
    "com.esotericsoftware.kryo" % "kryo" % "2.24.0",
    "tv.cntt" % "chill-akka_2.11" % "1.1",
    "io.kamon" % "sigar-loader" % "1.6.6-rev002",
    "org.slf4j" % "slf4j-api" % "1.7.21",
    "ch.qos.logback" % "logback-classic" % "1.1.7"
  )
}

lazy val akkaDependencyOverrides = {
  Set(
    "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion
  )
}

lazy val rootSettings = Seq (
  packagedArtifacts := Map.empty,
  publishLocal := {},
  publish := {}
)

lazy val clusterSettings = commonSettings ++ Seq(
  libraryDependencies ++= clusterDependencies
)

lazy val coreSettings = commonSettings ++ integrationTestSettings ++ Seq(
  libraryDependencies ++= testDependencies
)

lazy val seedNodeSettings = commonSettings ++ packAutoSettings ++ Seq(
  libraryDependencies ++=  akkaDependencies,
  dependencyOverrides ++= akkaDependencyOverrides,
  packCopyDependenciesUseSymbolicLinks := false,
  packJvmOpts := Map("seed-node" -> Seq("-server", "-Xms256m", "-Xmx1g"))
)

lazy val masterNodeSettings = commonSettings ++ packAutoSettings ++ Seq(
  libraryDependencies ++=  akkaDependencies,
  dependencyOverrides ++= akkaDependencyOverrides,
  packCopyDependenciesUseSymbolicLinks := false,
  packJvmOpts := Map("master-node" -> Seq("-server", "-Xss1m", "-Xms512m", "-Xmx2g"))
)

lazy val workerNodeSettings = commonSettings ++ packAutoSettings ++ Seq(
  libraryDependencies ++=  akkaDependencies,
  dependencyOverrides ++= akkaDependencyOverrides,
  packCopyDependenciesUseSymbolicLinks := false,
  packJvmOpts := Map("worker-node" -> Seq("-server", "-Xss1m", "-Xms1g", "-Xmx32g"))
)

lazy val root = (project in file(".")).
  settings(rootSettings: _*).
  aggregate(cluster, core, seednode, masternode, workernode)
lazy val cluster = project.
  settings(clusterSettings: _*)
lazy val core = project.
  settings(coreSettings: _*).
  configs(IntegrationTest)
lazy val seednode = project.
  settings(seedNodeSettings: _*).
  dependsOn(cluster)
lazy val masternode = project.
  settings(masterNodeSettings: _*).
  dependsOn(cluster, core)
lazy val workernode = project.
  settings(workerNodeSettings: _*).
  dependsOn(cluster, core)