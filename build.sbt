
name := "zarinpal"
 
version := "1.0" 
      
lazy val `zarinpal` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )



//unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

// zarinpal webservice
WsdlKeys.packageName := Some("com.zarinpal")
WsdlKeys.wsdlUrls in Compile += url("https://www.zarinpal.com/pg/services/WebGate/wsdl")