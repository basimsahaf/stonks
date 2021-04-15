package com.stonks.android;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.stonks.android.external.AlpacaWebSocket;
import com.stonks.android.manager.UserManager;
import com.stonks.android.model.WebSocketObserver;
import com.stonks.android.storage.CompanyTable;
import com.stonks.android.utility.Formatters;

public class MainActivity extends AppCompatActivity {
    private SlidingUpPanelLayout slidingUpPanel;
    private LinearLayout portfolioTitle;
    private TextView globalTitle;
    private AlpacaWebSocket socket;
    private OnBackPressedCallback backPressedCallback;
    private UserManager userManager;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CompanyTable.populateCompanyTableIfEmpty(getApplicationContext());

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

        backPressedCallback =
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        SettingsFragment settingsFragment = getSettingsFragment();
                        StockFragment stockFragment = getStockFragment();
                        FragmentManager fm = getSupportFragmentManager();

                        if (settingsFragment != null
                                && settingsFragment.shouldHandleBackPressed()) {
                            settingsFragment.handleBackPressed();
                        } else if (isSlidingDrawerVisible()) {
                            slidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        } else if (stockFragment != null
                                && stockFragment.shouldHandleBackPressed()
                                && slidingUpPanel.getPanelState()
                                        != SlidingUpPanelLayout.PanelState.ANCHORED) {
                            stockFragment.handleBackPressedForOverlay();
                        } else if (fm.getBackStackEntryCount() > 1) {
                            fm.popBackStack();
                        }
                    }
                };
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        this.socket = new AlpacaWebSocket();
        this.userManager = UserManager.getInstance(getApplicationContext());
    }

    public void subscribe(String symbol, WebSocketObserver observer) {
        this.socket.subscribe(symbol, observer);
    }

    public void unsubscribe(String symbol) {
        this.socket.unsubscribe(symbol);
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
        String tag = fragment.getClass().getCanonicalName();

        ft.replace(R.id.fragment_container, fragment, tag);
        ft.addToBackStack(tag).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backPressedCallback.handleOnBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public SlidingUpPanelLayout getSlidingUpPanel() {
        return slidingUpPanel;
    }

    private boolean isSlidingDrawerVisible() {
        Boolean panelAnchored =
                slidingUpPanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED;
        BuySellFragment buySellFragment =
                (BuySellFragment)
                        getSupportFragmentManager()
                                .findFragmentByTag(BuySellFragment.class.getCanonicalName());

        HypotheticalFragment hypotheticalFragment =
                (HypotheticalFragment)
                        getSupportFragmentManager()
                                .findFragmentByTag(HypotheticalFragment.class.getCanonicalName());

        Boolean drawerFragmentVisible =
                (buySellFragment != null && buySellFragment.isVisible())
                        || (hypotheticalFragment != null && hypotheticalFragment.isVisible());
        return panelAnchored || drawerFragmentVisible;
    }

    private SettingsFragment getSettingsFragment() {
        return (SettingsFragment)
                getSupportFragmentManager()
                        .findFragmentByTag(SettingsFragment.class.getCanonicalName());
    }

    private StockFragment getStockFragment() {
        return (StockFragment)
                getSupportFragmentManager()
                        .findFragmentByTag(StockFragment.class.getCanonicalName());
    }
}
