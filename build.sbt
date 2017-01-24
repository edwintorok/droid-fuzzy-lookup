scalaVersion := "2.11.8"

enablePlugins(AndroidApp)

versionCode := Some(1)
version := "0.1-SNAPSHOT"

instrumentTestRunner :=
  "android.support.test.runner.AndroidJUnitRunner"

platformTarget := "android-25"

javacOptions in Compile ++= "-source" :: "1.7" :: "-target" :: "1.7" :: Nil
scalacOptions += "-Ywarn-value-discard"

transitiveAndroidLibs in Android := false

shrinkResources := true
proguardScala in Android := true
useProguard in Android := true
dexMinimizeMain in Android := true
dexMaxProcessCount := 1
dexMainClasses in Android := Seq(
  "net/etorok/droidfuzzylookup/MainActivity.class"
)
proguardOptions in Android ++= Seq(
  "-keepattributes InnerClasses",
  "-dontwarn scala.async.internal.**",
  "-dontwarn scala.xml.parsing.**"
)

libraryDependencies ++= Seq(
  aar("com.android.support" % "support-compat" % "25.1.0"),
  aar("com.android.support" % "support-fragment" % "25.1.0"),
  aar("org.macroid" %% "macroid" % "2.0"),
  aar("org.macroid" %% "macroid-extras" % "2.0"),
  aar("org.macroid" %% "macroid-viewable" % "2.0"),
  "com.rockymadden.stringmetric" %% "stringmetric-core" % "0.27.4",
  "com.android.support.test" % "runner" % "0.5" % "androidTest",
  "com.android.support.test" % "rules" % "0.5" % "androidTest",
  "com.android.support.test.espresso" % "espresso-core" % "2.2.2" % "androidTest"
)

enablePlugins(AndroidProtify)
