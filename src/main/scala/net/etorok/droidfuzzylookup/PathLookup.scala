package net.etorok.droidfuzzylookup

import java.io.File

import android.util.Log
import com.rockymadden.stringmetric.similarity.RatcliffObershelpMetric

import scala.collection.breakOut
import scala.collection.immutable.SortedSet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by edwin on 1/21/17.
  */
class PathLookup {
  private val prefix = this.getClass.toString
  private val allFiles = Future {
    printTime("allFiles") {
      listFiles(getStorageDir).groupBy(_.getName)
    }
  }
  private val bytesFiles = allFiles map { theMap =>
    Log.d(prefix, s"allFiles: ${theMap.size}")
    printTime("bytesFiles") {
      val table = (Array fill 256)(Set[String]())
      theMap foreach {
        case (key, _) =>
          val bytes = key.getBytes
          bytes.foreach(table(_) += key)
      }
      table
    }
  }

  val DEFAULT_MAX_RESULTS = 50

  def performSearch(pattern: String,
                    maxResults: Int = DEFAULT_MAX_RESULTS): Future[Seq[File]] =
    bytesFiles flatMap { table =>
      Log.i(prefix, s"performSearch pattern: $pattern")
      val candidates = printTime("candidates") {
        val bytes = pattern.getBytes()
        val first = bytes.headOption.map(table(_)).getOrElse(Set.empty)
        (bytes foldLeft first) {
          case (set, c) => set & table(c)
        }
      }
      Log.d(prefix, s"candidates:${candidates.size}")
      val answers: SortedSet[(Double, String)] = printTime("compute scores") {
        candidates.map { name =>
          RatcliffObershelpMetric.compare(name, pattern).getOrElse(0.0) -> name
        }(breakOut)
      }
      val sorted = printTime("choose") {
        answers.takeRight(maxResults).toSeq.reverse
      }
      Log.d(prefix, s"answers: ${answers.size}, sorted: ${sorted.size}")
      allFiles map { all =>
        sorted flatMap {
          case (_, name) => all.getOrElse(name, Traversable())
        }
      }
    }

  private def printTime[A](msg: String)(f: => A) = {
    val t0 = System.nanoTime()
    val result = f
    val dt = (System.nanoTime() - t0) / 1e6
    Log.d(prefix, s"$msg: ${dt.formatted("%.3f")} ms")
    result
  }

  private def foldFilesCanon(accum: Set[File], file: File): Set[File] = {
    val path = file.getCanonicalFile
    if (accum.contains(path)) { accum } else {
      val files = try {
        if (path.isDirectory) { Option(path.listFiles) } else { None }
      } catch {
        case (_: SecurityException) | (_: java.io.IOException) => None
      }
      files match {
        case None => accum + path
        case Some(dirFiles) =>
          (dirFiles foldLeft accum)(foldFilesCanon)
      }
    }
  }

  private def listFiles(file: File) = {
    Log.i(prefix, file.getPath)
    foldFilesCanon(Set(), file.getCanonicalFile)
  }

  private def getStorageDir: File = {
    new File(scala.util.Properties.envOrElse("ANDROID_STORAGE", "/storage"))
  }

}
