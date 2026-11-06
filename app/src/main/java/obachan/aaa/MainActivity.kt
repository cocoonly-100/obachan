package obachan.aaa
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import obachan.aaa.ui.theme.ObchanTheme
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.io.File
import java.util.Objects
import kotlin.math.log
import androidx.core.net.toUri
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

const val TAG="debug"
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    lifecycleScope.launch{update(this@MainActivity)}
    setContent {
      ObchanTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Greeting(
            name = "Android",
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
suspend fun update(context: Context)= withContext(Dispatchers.IO){
  try {
    val json = fecthlatestjson() ?: return@withContext
    val latestversion = json.optLong("versioncode", 0L)
    val apkurl = json.optString("url", "")
    Log.d(TAG, "$latestversion $json")
    if (latestversion <= 0L || apkurl.isBlank()) return@withContext
    val currentversion = checkcurrentversion(context)
    Log.d("debug", "current=$currentversion latest=$latestversion")
    if (currentversion >= latestversion) return@withContext
    //promptupdate
    if (!promptupdate(context,currentversion,latestversion)) return@withContext
    val dest = File(context.cacheDir, "obachan.apk")
    val ok = downloadapk(apkurl, dest)
    Log.d(TAG, "downloaded")
    if (ok==false) return@withContext
    withContext(Dispatchers.Main){installapk(context,dest)}
    Log.d(TAG, "install intent")
  }catch(e:Exception){
    Log.d(TAG, e.toString())
  }catch(e: Exception){
    Log.d(TAG, e.toString())
  }
}

suspend fun promptupdate(context: Context, current: Long, latest: Long): Boolean =
  withContext(Dispatchers.Main) {
    suspendCancellableCoroutine { cont ->
      val prompt=AlertDialog.Builder(context)
        .setTitle("Update available")
        .setMessage("Current: $current\nLatest: $latest\n\nUpdate now?")
        .setPositiveButton("Update") { _, _ -> if (cont.isActive) cont.resume(true)}
        .setNegativeButton("Later") { _, _ -> if (cont.isActive) cont.resume(false) }
        .setOnCancelListener {if (cont.isActive) cont.resume(false) }
        .create()
      cont.invokeOnCancellation { prompt.dismiss() }
      prompt.show()
    }
  }
fun checkcurrentversion(context: Context):Long{
  val info=context.packageManager.getPackageInfo(context.packageName,0)
  return if (Build.VERSION.SDK_INT >= 28) info.longVersionCode
  else @Suppress("DEPRECATION") info.versionCode.toLong()
}

fun fecthlatestjson(): JSONObject?{
  val conn=URL("https://raw.githubusercontent.com/cocoonly-100/obachan/refs/heads/master/latest.json").openConnection() as HttpURLConnection
  conn.instanceFollowRedirects = true
  conn.setRequestProperty("User-Agent", "obachan-updater")
  conn.connectTimeout=3000
  conn.readTimeout=5000
  conn.useCaches = false
  try{
    if(conn.responseCode !in 200..299) return null
    return JSONObject(conn.inputStream.bufferedReader().use {it.readText()})
  }catch(e: Exception){
    Log.d(TAG, e.toString());return null
  }finally {conn.disconnect()}
}

fun downloadapk(url: String, dest:java.io.File):Boolean?{
  dest.parentFile?.mkdirs()
  val conn=URL(url).openConnection() as HttpURLConnection
  conn.instanceFollowRedirects = true
  conn.setRequestProperty("User-Agent", "obachan-updater")
  conn.connectTimeout=8000
  conn.readTimeout=15000
  conn.useCaches = false
  try{
    Log.d(TAG, conn.responseCode.toString())
    if(conn.responseCode !in 200..299) return false
    conn.inputStream.use {input-> dest.outputStream().use{out-> input.copyTo(out)}}
    return true
  }catch(e:Exception){
    dest.delete()
    Log.d(TAG,e.toString())
    return false
  }finally {conn.disconnect()}
}

fun installapk(context: Context, apk: File) {
  Log.d(TAG, "permission")
  if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O &&!context.packageManager.canRequestPackageInstalls()){
    val settings= Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData("package:${context.packageName}".toUri())
    settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(settings)
  }
  Log.d(TAG, "intentstart")
  val uri = FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",apk)
  val intent = Intent(Intent.ACTION_VIEW).apply {
    setDataAndType(uri, "application/vnd.android.package-archive")
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  Log.d(TAG, "installstart")
  context.startActivity(intent)
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "ha;ah asu $name!",
    modifier = modifier
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  ObchanTheme {
    Greeting("Android")
  }
}