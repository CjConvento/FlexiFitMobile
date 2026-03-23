package com.example.flexifitapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class PrivacyPolicyActivity : AppCompatActivity() {

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
        webView.loadDataWithBaseURL(null, getPrivacyHtml(), "text/html", "UTF-8", null)
    }

    private fun getPrivacyHtml(): String = """
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
            <h1>Privacy Policy</h1>
            <p><strong>Last updated:</strong> March 21, 2026</p>

            <h2>1. Information We Collect</h2>
            <p>We collect personal information (name, email, weight, height, fitness data) to provide personalised workouts and nutrition plans.</p>

            <h2>2. How We Use Your Information</h2>
            <p>Your data is used to track progress, generate insights, and improve the app. We do not sell your personal information.</p>

            <h2>3. Data Sharing</h2>
            <p>We may share aggregated, anonymised data with third‑party services for analytics. We do not share personally identifiable information without your consent.</p>

            <h2>4. Data Security</h2>
            <p>We implement industry‑standard security measures to protect your data.</p>

            <h2>5. Your Rights</h2>
            <p>You can request access, correction, or deletion of your data by contacting us at support@flexifit.com.</p>

            <h2>6. Changes to This Policy</h2>
            <p>We may update this policy; continued use indicates acceptance.</p>
        </body>
        </html>
    """.trimIndent()
}