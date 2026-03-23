package com.example.flexifitapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class TermsOfServiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_privacy)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadDataWithBaseURL(null, getTermsHtml(), "text/html", "UTF-8", null)
    }

    private fun getTermsHtml(): String = """
        <html>
        <head>
            <style>
                body { font-family: sans-serif; padding: 20px; line-height: 1.5; background-color: #f5f5f5; color: #333; }
                h1 { color: #2c3e50; }
                h2 { color: #2c3e50; margin-top: 20px; }
                p { margin: 10px 0; }
            </style>
        </head>
        <body>
            <h1>Terms of Service</h1>
            <p><strong>Last updated:</strong> March 21, 2026</p>

            <h2>1. Acceptance of Terms</h2>
            <p>By using FlexiFit, you agree to these Terms of Service.</p>

            <h2>2. Use of the Service</h2>
            <p>FlexiFit provides fitness tracking, workout plans, and nutritional guidance. You are responsible for your own health and safety.</p>

            <h2>3. User Accounts</h2>
            <p>You must provide accurate information and keep your account credentials secure. You are liable for all activities under your account.</p>

            <h2>4. Privacy</h2>
            <p>Your data is handled according to our <a href="javascript:void(0)" onclick="window.location.href = 'privacy://'">Privacy Policy</a>. By using FlexiFit, you consent to the collection and use of your data as described.</p>

            <h2>5. Intellectual Property</h2>
            <p>All content, trademarks, and features are owned by FlexiFit and may not be copied without permission.</p>

            <h2>6. Disclaimer of Warranties</h2>
            <p>The app is provided "as is" without warranties of any kind. Consult a professional before starting any fitness program.</p>

            <h2>7. Limitation of Liability</h2>
            <p>FlexiFit is not liable for any injuries, losses, or damages arising from your use of the service.</p>

            <h2>8. Changes to Terms</h2>
            <p>We may update these terms. Continued use means acceptance of the changes.</p>

            <h2>9. Contact</h2>
            <p>Questions? Contact us at support@flexifit.com</p>
        </body>
        </html>
    """.trimIndent()
}