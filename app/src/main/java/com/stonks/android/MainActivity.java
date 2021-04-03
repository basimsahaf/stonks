package com.stonks.android;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.stonks.android.external.AlpacaWebSocket;
import com.stonks.android.model.WebSocketObserver;
import com.stonks.android.utility.Formatters;

public class MainActivity extends AppCompatActivity {
    private SlidingUpPanelLayout slidingUpPanel;
    private LinearLayout portfolioTitle;
    private TextView globalTitle;
    private AlpacaWebSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // home screen by default
        switchFragment(new HomePageFragment());

        final BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnNavigationItemSelectedListener(
                menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.home_nav:
                            switchFragment(new HomePageFragment());
                            break;
                        case R.id.search_nav:
                            switchFragment(new SearchableFragment());
                            break;
                        case R.id.activity_nav:
                            switchFragment(new RecentTransactionsFragment());
                            break;
                        case R.id.saved_nav:
                            switchFragment(new SavedStocksFragment());
                            break;
                        case R.id.settings_nav:
                            switchFragment(new SettingsFragment());
                            break;
                        default:
                            return false;
                    }

                    return true;
                });

        LayoutInflater inflator =
                (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflator.inflate(R.layout.actionbar_layout, null);

        // each fragment can update these properties as needed
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(actionBarView);

        this.portfolioTitle =
                getSupportActionBar().getCustomView().findViewById(R.id.portfolio_title);
        this.globalTitle = getSupportActionBar().getCustomView().findViewById(R.id.global_title);

        // initializing sliding drawer
        slidingUpPanel = findViewById(R.id.sliding_layout);
        slidingUpPanel.setPanelHeight(0);
        slidingUpPanel.setAnchorPoint(1.0f);

        this.socket = new AlpacaWebSocket();
    }

    public void subscribe(String symbol, WebSocketObserver observer) {
        this.socket.subscribe(symbol, observer);
    }

    public void setPortfolioValue(float value) {
        globalTitle.setVisibility(View.GONE);
        portfolioTitle.setVisibility(View.VISIBLE);

        updatePortfolioValue(value);
    }

    public void updatePortfolioValue(float value) {
        TextView currentValue =
                getSupportActionBar().getCustomView().findViewById(R.id.current_value);
        currentValue.setText(Formatters.formatPrice(value));
    }

    public void setGlobalTitle(String title) {
        globalTitle.setText(title);

        portfolioTitle.setVisibility(View.GONE);
        globalTitle.setVisibility(View.VISIBLE);
    }

    public void setActionBarCustomViewAlpha(float alpha) {
        portfolioTitle.setAlpha(alpha);
    }

    public void hideActionBarCustomViews() {
        globalTitle.setVisibility(View.GONE);
        portfolioTitle.setVisibility(View.GONE);
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    public SlidingUpPanelLayout getSlidingUpPanel() {
        return slidingUpPanel;
    }
}
