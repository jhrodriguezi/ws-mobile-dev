package lab.jhrodriguezi.helloworld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.jhrodriguezi.helloworld.ui.theme.HelloWorldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelloWorldTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        var userName by remember { mutableStateOf("") }
                        if (userName.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = R.drawable.documentation_meme),
                                    contentDescription = "Image",
                                    modifier = Modifier.size(300.dp)
                                )
                                Spacer(modifier = Modifier.size(32.dp))
                                Greeting(userName)
                                Spacer(modifier = Modifier.size(8.dp))
                                Button(onClick = { userName = "" }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(text = "Go Back")
                                }
                            }
                        } else {
                            Column {
                                var userNamePivot by remember { mutableStateOf(userName) }
                                Text(text = "Please enter your name")
                                Spacer(modifier = Modifier.size(8.dp))
                                OutlinedTextField(
                                    userNamePivot,
                                    onValueChange = { userNamePivot = it })
                                Spacer(modifier = Modifier.size(8.dp))
                                Button(
                                    onClick = { userName = userNamePivot },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        null,
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(text = "Next")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val fullText = "Hello, $name!"

    val annotatedString = buildAnnotatedString {
        append(fullText.substringBefore(name))
        withStyle(style = androidx.compose.ui.text.SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(name)
        }
        append(fullText.substringAfter(name))
    }

    Text(
        text = annotatedString,
        fontSize = 30.sp,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HelloWorldTheme {
        Greeting("Android")
    }
}