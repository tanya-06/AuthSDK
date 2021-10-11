package com.example.dehaatauthsdk

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.*
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors
import net.openid.appauth.AuthorizationService.TokenResponseCallback
import net.openid.appauth.internal.Logger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity() {

    private lateinit var configuration: Configuration

    private lateinit var webView: WebView

    private lateinit var mAuthService: AuthorizationService
    private lateinit var mAuthServiceConfiguration :AuthorizationServiceConfiguration

    private lateinit var mAuthRequest :AuthorizationRequest

    private lateinit var mExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView= WebView(this)
        configuration=Configuration.getInstance(applicationContext)
        
        mExecutor = Executors.newSingleThreadExecutor()
        mExecutor.submit(this::recreateAuthorizationService)
    }


    @WorkerThread
    private fun recreateAuthorizationService() {
        if (::mAuthService.isInitialized) {
            Log.i("AuthSDK", "Discarding existing AuthService instance")
            mAuthService.dispose()
        }
        mAuthService = createAuthorizationService()

        AuthorizationServiceConfiguration.fetchFromUrl(
            configuration.discoveryUri!!,
            handleConfigurationRetrievalResult,
            configuration.connectionBuilder
        )
    }

    private fun createAuthorizationService(): AuthorizationService {
        Log.i("AuthSDK", "Creating authorization service")
        val builder = AppAuthConfiguration.Builder()
        builder.setConnectionBuilder(configuration.connectionBuilder)
        return AuthorizationService(this, builder.build())
    }

    private var handleConfigurationRetrievalResult = object : AuthorizationServiceConfiguration.RetrieveConfigurationCallback{
        override fun onFetchConfigurationCompleted(
            config: AuthorizationServiceConfiguration?,
            ex: AuthorizationException?
        ) {
            if (config == null) {
                Log.i("AuthSDK", "Failed to retrieve discovery document", ex)
                return
            }
            mAuthServiceConfiguration=config


            //once authService configuration is recieved from discovery uri
            //its time to create Auth Request
            createAuthRequest("This is Hint")

        }
    }

    private fun createAuthRequest(loginHint: String?) {
        Log.i("AuthSDK", "Creating auth request for login hint: $loginHint")
        val authRequestBuilder = AuthorizationRequest.Builder(
           mAuthServiceConfiguration,
            configuration.clientId!!,
            ResponseTypeValues.CODE,
            configuration.redirectUri
        ).setScope(configuration.scope)
        if (!TextUtils.isEmpty(loginHint)) {
            authRequestBuilder.setLoginHint(loginHint)
        }
        mAuthRequest = authRequestBuilder.build()


        webView.apply {
            webViewClient=MyWebViewClient()
            enableWebViewSettings()
            loadUrl(mAuthRequest.toUri().toString())
        }
    }


    inner class MyWebViewClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

            if(url!!.contains(mAuthRequest.toUri().toString())) {
                //write java script code

                webView.loadUrl(
                    "javascript: {" +
                            "document.getElementById('username').value = '" + "harsh.vardhan@agrevolution.in" + "';" +
                            "document.getElementById('password').value = '" + "admin" + "';" +
                            "document.getElementById('kc-login').click();" +
                            "};"
                );
            }
            if(url.contains(configuration.redirectUri.toString())){
                //communicate back to native
                // what information need to be communicate

                val intent = extractResponseData(Uri.parse(url))
                val response = AuthorizationResponse.fromIntent(intent!!);
                performTokenRequest(response!!.createTokenExchangeRequest(),handleAccessTokenResponse)
                val a =2
            }
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }
    }

    private fun extractResponseData(responseUri: Uri): Intent? {
        return if (responseUri.queryParameterNames.contains(AuthorizationException.PARAM_ERROR)) {
            AuthorizationException.fromOAuthRedirect(responseUri).toIntent()
        } else {
            val response = AuthorizationResponse.Builder(mAuthRequest)
                .fromUri(responseUri)
                .build();
            if (mAuthRequest.getState() == null && response.state != null
                || mAuthRequest.getState() != null && mAuthRequest.getState() != response.state
            ) {
                Logger.warn(
                    "State returned in authorization response (%s) does not match state "
                            + "from request (%s) - discarding response",
                    response.state,
                    mAuthRequest.getState()
                )
                return AuthorizationRequestErrors.STATE_MISMATCH.toIntent()
            }
            response.toIntent()
        }
    }


    @MainThread
    private fun performTokenRequest(
        request: TokenRequest,
        callback: TokenResponseCallback
    ) {
        val clientAuthentication= ClientSecretBasic(configuration.tokenEndpointUri.toString())

        mAuthService.performTokenRequest(
            request,
            clientAuthentication,
            callback
        )
    }

    private var  handleAccessTokenResponse = object :TokenResponseCallback {
        override fun onTokenRequestCompleted(
            response: TokenResponse?,
            ex: AuthorizationException?
        ) {
            Toast.makeText(applicationContext,"All Tokens received",Toast.LENGTH_SHORT).show()
            val a =6
            val c=9
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun WebView.enableWebViewSettings() {
        settings.javaScriptEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
    }
}