package net.etorok.droidfuzzylookup

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget._
import macroid.FullDsl._
import macroid._
import macroid.contrib.TextTweaks
import macroid.extras.EditTextTweaks._
import macroid.extras.LinearLayoutTweaks._
import macroid.extras.ViewTweaks._

import scala.language.postfixOps

class MainActivity extends Activity with Contexts[Activity] {
  // allows accessing `.value` on TR.resource.constants
  implicit val context = this

  lazy val pathLookup = new PathLookup
  lazy val msg = toast("Press Back again to exit") <~ long <~ fry
  var listview = slot[ListView]
  var toastDuration = 0
  var lastBackPressed = 0L

  override def onCreate(savedInstanceState: Bundle): Unit = {
    requestWindowFeature(Window.FEATURE_ACTION_BAR)
    super.onCreate(savedInstanceState)
    var input = slot[EditText]
    setContentView {
      Ui.get {
        l[LinearLayout](
          w[EditText] <~ wire(input) <~ hint(TR.string.search_hint.value) <~ TextTweaks.medium
            <~ etSetInputTypeText
            <~ etImeOptionSearch
            <~ etClickActionSearch(onAction)
            <~ vMatchWidth <~ llLayoutMargin(16 dp, 16 dp, 16 dp, 16 dp),
          w[ListView] <~ wire(listview) <~ vMatchParent
        ) <~ vMatchParent <~ vertical
      }
    }
  }

  def onAction(pattern: String) = {
    pathLookup.performSearch(pattern) foreachUi { results =>
      Log.i("results", s"Got ${results.length} answers")
      listview <~ FileListable.listAdapterTweak(results)
    }
  }

  override def onBackPressed(): Unit = {
    val now = System.currentTimeMillis
    val dt = now - lastBackPressed
    if (dt < 3500) {
      Ui.get(msg <~ Loaf(_.cancel()))
      super.onBackPressed()
    } else {
      lastBackPressed = now
      Ui.get(msg <~ fry)
    }

  }

}