-dontobfuscate
-dontnote org.apache.http.**
-dontnote android.net.http.**
-keepattributes EnclosingMethod
-dontwarn scala.async.internal.**
-dontwarn scala.xml.parsing.**
###
# Scala-specific proguard config from sbt-android
###
# keep Dynamic because proguard cache fails to handle it gracefully
-keep class scala.Dynamic { *; }
-dontnote scala.concurrent.util.Unsafe
-dontnote scala.Enumeration**
-dontnote scala.ScalaObject
-dontnote org.xml.sax.EntityResolver
-dontnote scala.concurrent.forkjoin.**
-dontwarn scala.beans.ScalaBeanInfo
-dontwarn scala.concurrent.**
-dontnote scala.reflect.**
-dontwarn scala.reflect.**
-dontwarn scala.sys.process.package$
-dontwarn **$$anonfun$*
-dontwarn scala.collection.immutable.RedBlack$Empty
-dontwarn scala.tools.**,plugintemplate.**
-keep public class scala.reflect.ScalaSignature
# This is gone in 2.11
-keep public interface scala.ScalaObject
-keepclassmembers class * {
    ** MODULE$;
}

-keep class scala.collection.SeqLike {
    public java.lang.String toString();
}

-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinPool {
    long eventCount;
    int  workerCounts;
    int  runControl;
    scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode syncStack;
    scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode spareStack;
}

-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinWorkerThread {
    int base;
    int sp;
    int runState;
}

-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinTask {
    int status;
}

-keepclassmembernames class scala.concurrent.forkjoin.LinkedTransferQueue {
    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference head;
    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference tail;
    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference cleanMe;
}
