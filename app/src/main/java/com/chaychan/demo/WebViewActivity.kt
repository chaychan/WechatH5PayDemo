package com.chaychan.demo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {

    companion object{
        const val BASE_URL = "https://www.yungouos.com/"
        const val DEMO_URL = "$BASE_URL#/demo"
    }

    private var isError = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        setSupportActionBar(findViewById(R.id.toolbar))

        initWebView()
        webView.loadUrl(DEMO_URL)
    }

    private fun initWebView() {
        val settings = webView.settings
        settings.setJavaScriptEnabled(true)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportZoom(true)
        settings.setSupportMultipleWindows(true)
        settings.setAppCacheEnabled(true)
        settings.domStorageEnabled = true

        webView.webChromeClient = object:WebChromeClient(){
            override fun onReceivedTitle(view: WebView, title: String) {
                toolbar.title = title
            }

            override fun onProgressChanged(p0: WebView?, progress: Int) {
                if (progress == 100) {
                    pbLoading.visibility = View.GONE
                } else {
                    if (pbLoading.visibility == View.GONE)
                        pbLoading.visibility = View.VISIBLE

                    pbLoading.progress = progress
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.isNullOrEmpty()) {
                    return false
                }

                if (url.contains("https://wx.tenpay.com")) {
                    //是支付的url 添加Referer
                    val extraHeaders = HashMap<String, String>()
                    extraHeaders.put("Referer", BASE_URL)
                    view?.loadUrl(url, extraHeaders)
                    return true
                }


                try {
                    if (url.startsWith("weixin://")) {
                        if (url.startsWith("weixin://") && !hadInstalledWechat()){
                            //没有安装微信 则不跳转
                            return false
                        }

                        //唤起微信
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                        return true
                    }
                } catch (e: Exception) {
                    return false
                }

                return super.shouldOverrideUrlLoading(view,url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                if (isError) {
                    isError = false
                    onLoadError()
                } else {
                    onLoadPageFinished()
                }
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                isError = true
            }
        }
    }

    private fun onLoadPageFinished() {
        //加载完成
    }

    private fun onLoadError() {
        Toast.makeText(this,"加载失败，请重新进入重试", Toast.LENGTH_LONG)
    }

    fun hadInstalledWechat(): Boolean {
        val packageManager = packageManager // 获取packagemanager
        val pInfo = packageManager.getInstalledPackages(0) // 获取所有已安装程序的包信息
        if (pInfo != null) {
            for (i in pInfo.indices) {
                val pn = pInfo[i].packageName
                if (pn == "com.tencent.mm") {
                    return true
                }
            }
        }
        return false
    }
}