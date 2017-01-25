package net.etorok.droidfuzzylookup

import android.app.Activity
import android.os.Bundle
import android.view.{Gravity, Window}
import android.widget._
import macroid.FullDsl._
import macroid._
import macroid.contrib.TextTweaks
import macroid.extras.EditTextTweaks._
import macroid.extras.LinearLayoutTweaks._
import macroid.extras.ViewTweaks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class MainActivity extends Activity with Contexts[Activity] {
  // allows accessing `.value` on TR.resource.constants
  private implicit val context = this

  private val FIRST_ID = 1000
  object Id extends IdGenerator(start = FIRST_ID)

  private lazy val pathLookup = new PathLookup
  private lazy val msg = toast("Press Back again to exit") <~ long <~ fry
  //noinspection VarCouldBeVal
  private var listview = slot[ListView]
  //noinspection VarCouldBeVal
  private var progress = slot[ProgressBar]
  private var lastBackPressed = 0L

  val getListViewId: Int = Id.listview

  def isIdle: Boolean = listview.get.isShown

  override def onCreate(savedInstanceState: Bundle): Unit = {
    requestWindowFeature(Window.FEATURE_ACTION_BAR)
    super.onCreate(savedInstanceState)
    setContentView {
      Ui.get {
        l[LinearLayout](
          w[EditText] <~ hint(TR.string.search_hint.value) <~ TextTweaks.medium
            <~ etSetInputTypeText
            <~ etImeOptionSearch
            <~ etClickActionSearch(onAction)
            <~ vMatchWidth <~ llLayoutMargin(16 dp, 16 dp, 16 dp, 16 dp),
          w[ProgressBar] <~ wire(progress) <~ hide,
          w[ListView] <~ id(getListViewId) <~ wire(listview) <~ vMatchParent
        ) <~ vMatchParent <~ vertical
      }
    }
  }

  private def perform(f: Ui[Any]) = { Ui.get(f); () }

  private def onAction(pattern: String) = perform {
    // http://47deg.github.io/macroid/docs/guide/Operators.html
    val future = pathLookup.performSearch(pattern)
    (listview <~ hide <~~ future.map(FileListable.listAdapterTweak) <~ show) ~
      (progress <~ llLayoutGravity(Gravity.CENTER) <~ waitProgress(future))
  }

  override def onBackPressed(): Unit = {
    val now = System.currentTimeMillis
    val dt = now - lastBackPressed
    if (dt < 3500) {
      perform(msg <~ Loaf(_.cancel()))
      super.onBackPressed()
    } else {
      lastBackPressed = now
      perform(msg <~ fry)
    }

  }

}
