// 1.7.3 would include proguard.txt of transitive deps, which we don't want for
// macroid-extras because it would keep lot of unneeded code
addSbtPlugin("org.scala-android" % "sbt-android" % "1.7.3")
addSbtPlugin("org.scala-android" % "sbt-android-protify" % "1.4.0")
