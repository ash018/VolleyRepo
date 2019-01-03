package info.mis.motorequipment.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import info.mis.motorequipment.R;
import info.mis.motorequipment.app.AppConfig;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

public class AdminReport extends AppCompatActivity {

    private WebView wv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_report);

        String url = "http://mis.digital:82/ReportApp/MotorConstructionEquipment";

        wv1=(WebView)findViewById(R.id.webView);


        wv1.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        wv1.getSettings().setLoadsImagesAutomatically(true);
        wv1.getSettings().setJavaScriptEnabled(true);

        wv1.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        wv1.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        wv1.getSettings().setDomStorageEnabled(true);
        wv1.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        wv1.getSettings().setUseWideViewPort(true);
        wv1.getSettings().setEnableSmoothTransition(true);



        wv1.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);


        wv1.loadUrl(AppConfig.ADMIN_REPORT);
        wv1.setWebViewClient(new MyBrowser());

    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
