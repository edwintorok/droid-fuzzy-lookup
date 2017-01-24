package net.etorok.droidfuzzylookup

import java.io.File

import android.content.{ActivityNotFoundException, Intent}
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.util.LruCache
import android.util.Log
import android.view.Gravity
import android.webkit.MimeTypeMap
import android.widget.{ImageView, LinearLayout, TextView}
import macroid.FullDsl._
import macroid.extras.ImageViewTweaks._
import macroid.extras.LinearLayoutTweaks.{W, _}
import macroid.viewable.SlottedListable
import macroid.{ContextWrapper, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by edwin on 1/22/17.
  */
object FileListable extends SlottedListable[File] {

  val cache: LruCache[String, Drawable] = new LruCache(10)

  override def makeSlots(viewType: Int)(
      implicit ctx: ContextWrapper): (Ui[W], Slots) = {
    val slots = new Slots
    val view = l[LinearLayout](
        w[TextView] <~ wire(slots.path) <~ llMatchWeightHorizontal,
        w[ImageView] <~ wire(slots.picture)
      ) <~ On.click(clickItem(slots))
    (view, slots)
  }

  override def fillSlots(slots: Slots, data: File)(
      implicit ctx: ContextWrapper): Ui[Any] = {
    val path = data.getPath
    slots.file = data
    (slots.path <~ text(path)) ~
      (slots.picture <~ getMimeIcon(data).map(ivSrc)) ~
      (slots.picture <~ llLayoutGravity(Gravity.RIGHT))
  }

  private def getMime(f: File) =
    Option(MimeTypeMap.getFileExtensionFromUrl(f.getPath))
      .map { extension =>
        val mime = MimeTypeMap.getSingleton.getMimeTypeFromExtension(extension)
        Log.d("getMime", s"mime type for ${f.getPath} ($extension) = $mime")
        mime
      }
      .getOrElse("text/plain")

  private def getIntent(file: File) = {
    val intent = new Intent(Intent.ACTION_VIEW)
    val mime = getMime(file)
    intent.setDataAndType(Uri.fromFile(file), mime)
    intent
  }

  private def clickItem(slots: Slots)(implicit ctx: ContextWrapper) = Ui {
    try {
      ctx.bestAvailable.startActivity(getIntent(slots.file))
    } catch {
      case _: ActivityNotFoundException =>
        Ui.get(toast(s"No app can handle ${slots.file}") <~ long <~ fry)
    }
  }

  private def getMimeIcon(f: File)(implicit ctx: ContextWrapper) = Future {
    val mime = getMime(f)
    cache.get(mime) match {
      case null =>
        val pm = ctx.bestAvailable.getPackageManager
        val intent = getIntent(f)
        val ico = {
          val matches = pm.queryIntentActivities(intent, 0)
          Log.d("getMimeIcon", s"matches: ${matches.size()}")
          if (matches.isEmpty) null
          else matches.get(0).loadIcon(pm)
        }
        cache.put(mime, ico)
        ico
      case ico => ico
    }
  }

  //noinspection VarCouldBeVal,VarCouldBeVal
  class Slots {
    var path: Option[TextView] = slot[TextView]
    var picture: Option[ImageView] = slot[ImageView]
    var file = new File("")
  }

}
