package net.etorok.droidfuzzylookup

import java.io.File

import android.util.Log
import com.rockymadden.stringmetric.similarity.RatcliffObershelpMetric

import scala.collection.breakOut
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by edwin on 1/21/17.
  */
class PathLookup {
  private val allFiles = Future {
    printTime("allFiles") {
      listFiles(getStorageDir()).toTraversable.groupBy(_.getName)
    }
  }
  private val bytesFiles = allFiles map { theMap =>
    Log.d("init", s"allFiles: ${theMap.size}")
    printTime("bytesFiles") {
      val table: Array[Set[String]] = Array.ofDim(256)
      theMap foreach { case (key, _) =>
        val bytes = key.getBytes
        bytes.foreach { b =>
          val idx: Int = b
          if (table(idx) == null)
            table(idx) = Set(key)
          else
            table(idx) += key
        }
      }
      table
    }
  }

  def performSearch(pattern: String) =
    bytesFiles flatMap { table =>
      Log.i("performSearch", s"pattern: ${pattern}")
      val candidates = printTime("candidates") {
        val bytes = pattern.getBytes()
        if (bytes.length == 0) Set.empty
        else (bytes foldLeft table(bytes(0))) {
          case (set, c) => set & table(c)
        }
      }
      Log.d("process", s"candidates:${candidates.size}")
      val answers: Array[(Double, String)] = printTime("compute scores") {
        candidates.map { name =>
          RatcliffObershelpMetric.compare(name, pattern).getOrElse(0.0) -> name
        }(breakOut)
      }
      Log.d("process", s"answers: ${answers.size}")
      printTime("sort scores") {
        answers.sortBy(_._1).take(100)
      }
      allFiles map { all =>
        answers.toSeq flatMap { case (_, name) => all.getOrElse(name, Traversable()) }
      }
    }

  private def printTime[A](msg: String)(f: => A) = {
    val t0 = System.nanoTime()
    val result = f
    val dt = (System.nanoTime() - t0) / 1e6
    Log.d(msg, s"${dt.formatted("%.3f")} ms")
    result
  }

  private def listFilesCanon(file: File): Iterator[File] = {
    try {
      val path = file.getPath()
      if (file.getCanonicalPath() != path)
        Iterator.empty // a symlink
      else if (file.isDirectory)
        file.listFiles() match {
          case null => Iterator.empty
          case files => files.iterator flatMap listFilesCanon
        }
      else
        Iterator.single(file)
    } catch {
      case _: SecurityException => Iterator.empty
      case _: java.io.IOException => Iterator.empty
    }
  }

  private def listFiles(file: File) = {
    Log.i("listFiles", file.getPath())
    listFilesCanon(file.getCanonicalFile())
  }

  private def getStorageDir(): File = {
    return new File(scala.util.Properties.envOrElse("ANDROID_STORAGE", "/storage"))
  }

}
