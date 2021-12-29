package com.airmole.xmsoftapartmentwearos

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.ambient.AmbientModeSupport
import com.airmole.xmsoftapartmentwearos.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity(), AmbientModeSupport.AmbientCallbackProvider {

    // e鹭安身份编码
    private val eluanCode = "314872627268327871345074334B46654F6B4E50626F51526D2B77616B443132397A7A624166686E3648513D"

    private val ambientCallbackState = AmbientCallbackState()
    private lateinit var binding: ActivityMainBinding
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = ambientCallbackState

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)

        // 设置屏幕常量
        super.onCreate(savedInstanceState)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 设置屏幕最亮
        val window = getWindow()
        val windowLayoutParams = window.getAttributes()
        windowLayoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.setAttributes(windowLayoutParams)

        setContentView(binding.root)
        ambientController = AmbientModeSupport.attach(this)

        // 生成门禁二维码
        makeQrcode(false)

        // 点击刷新按钮更新二维码
        binding.button.setOnClickListener {
            makeQrcode(true)
        }

    }

    // 页面渲染完成后
    override fun onStart() {
        // 每分钟刷新
        Timer().schedule(object:TimerTask(){
            override fun run() {
                val nowTimestamp = System.currentTimeMillis() / 1000
                if (nowTimestamp.toInt() % 60 == 0) {
                    Log.d("schedule", nowTimestamp.toInt().toString())
                    runOnUiThread {
                        makeQrcode(false)
                    }
                }
            }
        }, Date(), 1000)
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 恢复屏幕亮度
        val window = getWindow()
        val windowLayoutParams = window.getAttributes()
        windowLayoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.setAttributes(windowLayoutParams)
        // 取消屏幕常亮
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // 生成二维码
    fun makeQrcode(isRefresh: Boolean) {
        val qrcodeStr = jsonStr()
        val qrCode = ZxingUtils.createQRCode(qrcodeStr)
        binding.imageView.setImageBitmap(qrCode)
        if (isRefresh) {
            Toast.makeText(this, "已刷新", Toast.LENGTH_SHORT).show()
        }

    }

    // 返回二维码内容JSON编码值
    fun jsonStr(): String {
        data class Json (
            val sfmbm: String? = eluanCode,
            val sfmyxjzsj: String? = getVaildTimeStamp()
        ) {
            override fun toString(): String {
                return "{\"sfmbm\":\"${sfmbm}\",\"sfmyxjzsj\":\"${sfmyxjzsj}\"}"
            }
        }

        val json = Json()
        return json.toString()
    }

    // 计算二维码有效时间值
    fun getVaildTimeStamp(): String {
        val time = System.currentTimeMillis()
        return ((time / 1000) + 60).toString()
    }

}

private class AmbientCallbackState : AmbientModeSupport.AmbientCallback() {

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        // Handle entering ambient mode
        super.onEnterAmbient(ambientDetails)
    }

}