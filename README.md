#Zarinpal payment for Playframework/Scala


Add the following lines to /project/plugins.sbt

```scala

resolvers += Resolver.url("play-sbt-plugins",
                        url("https://dl.bintray.com/playframework/
                        sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
addSbtPlugin("com.typesafe.sbt" % "sbt-play-soap" % "1.1.4")

```

Add the following lines to /build.sbt

```scala

// zarinpal webservice
WsdlKeys.packageName := Some("com.zarinpal")
WsdlKeys.wsdlUrls in Compile += url("https://www.zarinpal.com/pg/
                        services/WebGate/wsdl")

```

Config your merchantId in /conf/application.conf

```scala

zarinpal {
    merchantId = "6245e35a-fd55-11e8-8eb2-f2801f1b9fd1"
    startpay.url = "https://www.zarinpal.com/pg/StartPay"
}

```

Use "sbt run" to run the project and open "http://localhost:9000/"
