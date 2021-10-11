package com.example.dehaatauthsdk

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import net.openid.appauth.*
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors
import net.openid.appauth.AuthorizationService.TokenResponseCallback

class LoginActivity : AppCompatActivity() {

    private lateinit var initialConfiguration: Configuration
    private lateinit var mAuthService: AuthorizationService
    private lateinit var mAuthServiceConfiguration: AuthorizationServiceConfiguration
    private lateinit var mAuthRequest: AuthorizationRequest
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        webView = WebView(this).apply {
            webViewClient = MyWebViewClient()
            enableWebViewSettings()
        }
        initialConfiguration = Configuration.getInstance(applicationContext)

        CoroutineScope((IO)).launch {
            startAuthorizationServiceCreation()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun WebView.enableWebViewSettings() {
        settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
        }
    }

    private fun startAuthorizationServiceCreation() {
        disposeCurrentServiceIfExist()
        mAuthService = createNewAuthorizationService()

        initialConfiguration.discoveryUri?.let {
            fetchEndpointsFromDiscoveryUrl(it)
        }
    }

    private fun disposeCurrentServiceIfExist() {
        if (::mAuthService.isInitialized)
            mAuthService.dispose()
    }

    private fun fetchEndpointsFromDiscoveryUrl(discoveryUrl: Uri) {
        AuthorizationServiceConfiguration.fetchFromUrl(
            discoveryUrl,
            handleConfigurationRetrievalResult,
            initialConfiguration.connectionBuilder
        )
    }

    private fun createNewAuthorizationService() =
        AuthorizationService(
            this,
            AppAuthConfiguration.Builder()
                .setConnectionBuilder(initialConfiguration.connectionBuilder)
                .build()
        )

    private var handleConfigurationRetrievalResult =
        AuthorizationServiceConfiguration.RetrieveConfigurationCallback { config, exception ->
            if (config == null) {
                Toast.makeText(this, exception.toString(), Toast.LENGTH_SHORT).show()
                return@RetrieveConfigurationCallback
            }
            mAuthServiceConfiguration = config
            createAuthRequest()
        }

    private fun createAuthRequest() {
        mAuthRequest =
            AuthorizationRequest.Builder(
                mAuthServiceConfiguration,
                initialConfiguration.clientId!!,
                ResponseTypeValues.CODE,
                initialConfiguration.redirectUri
            ).setScope(initialConfiguration.scope).setLoginHint("Please enter email").build()

        loadAuthorizationEndpointInWebView(mAuthRequest.toUri().toString())

    }

    private fun loadAuthorizationEndpointInWebView(authUrl: String) {
        CoroutineScope(Main).launch {
            webView.loadUrl(authUrl)
        }
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            url?.let {
                if (checkIfUrlIsAuthorizationUrl(it))
                    inputUserCredentialsAndClickSignIn(
                        CommunicationUtils.getUserName(),
                        CommunicationUtils.getPassword()
                    )

                if (checkIfUrlIsRedirectUrl(it))
                    handleRedirectUrl(it)
            }
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }
    }

    private fun checkIfUrlIsAuthorizationUrl(url: String) =
        url.contains(mAuthRequest.toUri().toString())

    private fun checkIfUrlIsRedirectUrl(url: String) =
        url.contains(initialConfiguration.redirectUri.toString())

    private fun inputUserCredentialsAndClickSignIn(userName: String, password: String) =
        webView.loadUrl(
            "javascript: {" +
                    "document.getElementById('username').value = '" + userName + "';" +
                    "document.getElementById('password').value = '" + password + "';" +
                    "document.getElementById('kc-login').click();" +
                    "};"
        )

    private fun handleRedirectUrl(url: String) {
        val intent = extractResponseDataFromRedirectUrl(url)
        val response = AuthorizationResponse.fromIntent(intent)
        response?.let {
            performTokenRequest(response.createTokenExchangeRequest(), handleTokenResponseCallback)
        }

    }

    private fun extractResponseDataFromRedirectUrl(url: String): Intent {
        val redirectUrl = Uri.parse(url)
        if (redirectUrl.queryParameterNames.contains(AuthorizationException.PARAM_ERROR))
            return AuthorizationException.fromOAuthRedirect(redirectUrl).toIntent()
        else {
            val response = AuthorizationResponse.Builder(mAuthRequest)
                .fromUri(redirectUrl)
                .build()

            if (mAuthRequest.getState() == null && response.state != null
                || mAuthRequest.getState() != null && mAuthRequest.getState() != response.state
            )
                return AuthorizationRequestErrors.STATE_MISMATCH.toIntent()

            return response.toIntent()
        }
    }

    private fun performTokenRequest(
        request: TokenRequest,
        callback: TokenResponseCallback
    ) {
        val clientAuthentication =
            ClientSecretBasic(initialConfiguration.tokenEndpointUri.toString())

        mAuthService.performTokenRequest(
            request,
            clientAuthentication,
            callback
        )
    }

    private var handleTokenResponseCallback =
        TokenResponseCallback { response, exception ->
            response?.let {
                val tokenInfo = TokenInfo(
                    it.accessToken!!,
                    it.refreshToken!!,
                    it.idToken!!
                )
                CommunicationUtils.getLoginCallback().onSuccess(tokenInfo)
            } ?: kotlin.run {
                CommunicationUtils.getLoginCallback().onFailure(exception)
            }

            Toast.makeText(applicationContext, "All Tokens received", Toast.LENGTH_SHORT).show()
        }

}