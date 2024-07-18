import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
@Preview
fun App() {
    var filePath by remember { mutableStateOf("") }
    var selectVideoText by remember { mutableStateOf("请选择视频") }
    var loadingProgress by remember { mutableStateOf(false) }
    var suffixLists = arrayOf("mp4")

    var buttonEnableState by remember { mutableStateOf(false) }
    var buttonEnableState2 by remember { mutableStateOf(false) }
    var cmdContentState by remember { mutableStateOf("") }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally).padding(10.dp),
                onClick = {
                    showFileSelector(suffixList = suffixLists, onFileSelected = {
                        filePath = it
                        selectVideoText = it
                        //选择文件成功后启用按钮
                        buttonEnableState = true
                    })
                }) {
                Text("选择视频")
            }

            Text(
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally).padding(10.dp),
                text = selectVideoText
            )

            Button(
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally).padding(10.dp),
                enabled = buttonEnableState,
                onClick = {
                    if (filePath.isNotBlank()) {
                        loadingProgress = true
                        GlobalScope.launch {
                            startChange(filePath) {
                                cmdContentState = it
                                loadingProgress = false
                            }
                        }
                    }
                }) {
                Text("MP4 -> M3U8")
            }

            Button(
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally).padding(10.dp),
                enabled = buttonEnableState2,
                onClick = {
                    if (filePath.isNotBlank()) {
                    }
                }) {
                Text("M3U8 -> MP4 (还没写，懒得写，有空写)")
            }

            if (loadingProgress) {
                CircularProgressIndicator(modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
            }


            Text(
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally).padding(10.dp),
                text = cmdContentState
            )
        }
    }


}


fun startChange(filePath: String, resultCmdContent: (String) -> Unit) {
    val parentPath = File(filePath)
    val filePathName = parentPath.name.substring(0, parentPath.name.indexOf("."))
    val newFilePath = File("${parentPath.parent}\\${filePathName}").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    val ffmpegFilePath: String =
        "ffmpeg\\bin\\ffmpeg.exe"
    val cmdList = listOf(
        ffmpegFilePath,
        "-i",
        "d:\\Download\\下载.mp4",
        "-force_key_frames",
        " \"expr:gte(t,n_forced*2)\"",
        "-strict",
        "-2",
        "-c:a",
        "aac",
        "-c:v",
        "libx264",
        "-hls_time",
        "2",
        "-f",
        "hls",
        "index.m3u8"
    )
    val cmdArray = arrayOf(
        ffmpegFilePath,
        "-i",
        "d:\\Download\\下载.mp4",
        "-force_key_frames",
        " \"expr:gte(t,n_forced*2)\"",
        "-strict",
        "-2",
        "-c:a",
        "aac",
        "-c:v",
        "libx264",
        "-hls_time",
        "2",
        "-f",
        "hls",
        "index.m3u8"
    )
//    ffmpeg -i test.mp4 -force_key_frames "expr:gte(t,n_forced*2)" -strict -2 -c:a aac -c:v libx264 -hls_time 2 -f hls index.m3u8

    val pb = ProcessBuilder(
        ffmpegFilePath,
        "-i",
        filePath,
        "-force_key_frames",
        "\"expr:gte(t,n_forced*2)\"",
        "-strict",
        "-2",
        "-c:a",
        "aac",
        "-c:v",
        "libx264",
        "-hls_time",
        "2",
        "-f",
        "hls",
        "$newFilePath\\index.m3u8"
    )
//    val output = File("output.txt")
//    val error = File("error.txt")
//
//    pb.redirectOutput(output)
//    pb.redirectError(error)

    //正常信息和错误信息合并输出
    pb.redirectErrorStream(true)

    val process = pb.start()

    //如果你想获取到执行完后的信息，那么下面的代码也是需要的
    var line: String?
    val br = BufferedReader(InputStreamReader(process.inputStream))
    while (br.readLine().also { line = it } != null) {
        println(line)
//        resultCmdContent(line ?: "")
    }

    resultCmdContent("任务已完成!\n路径: ${newFilePath.absolutePath}")
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
//        title = "视频格式转换 by:小康 QQ30028040",
        title = "视频格式转换 by:小康",
    ) {
        App()
    }
}


private inline fun showFileSelector(
    suffixList: Array<String>,
    onFileSelected: (String) -> Unit
) {
    JFileChooser().apply {
        //设置页面风格
        try {
            val lookAndFeel = UIManager.getSystemLookAndFeelClassName()
            UIManager.setLookAndFeel(lookAndFeel)
            SwingUtilities.updateComponentTreeUI(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        fileSelectionMode = JFileChooser.FILES_ONLY
        isMultiSelectionEnabled = false
        fileFilter = FileNameExtensionFilter("文件过滤", *suffixList)

        val result = showOpenDialog(ComposeWindow())
        if (result == JFileChooser.APPROVE_OPTION) {
            val dir = this.currentDirectory
            val file = this.selectedFile
            println("Current apk dir: ${dir.absolutePath} ${dir.name}")
            println("Current apk name: ${file.absolutePath} ${file.name}")
            onFileSelected(file.absolutePath)
        }
    }
}

