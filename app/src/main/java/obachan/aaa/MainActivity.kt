package obachan.aaa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import obachan.aaa.ui.theme.ObchanTheme
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.util.Objects

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
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


fun fetchlatest(): JSONObject?{
  val conn=URL("https://raw.githubusercontent.com/cocoonly-100/obachan/refs/heads/master/latest.json").openConnection() as HttpURLConnection
  conn.requestMethod="GET"
  conn.connectTimeout=3000
  conn.readTimeout=1500
  try{
    if(conn.responseCode !in 200..299) return null
    return JSONObject(conn.getInputStream().bufferedReader().use {it.readText()})
  }finally {conn.disconnect()}
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
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