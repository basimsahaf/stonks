# Stonks
A simulated robo-investor that can help you learn how to trade stocks, without the risk of spending any real money.

Horizontal number picker source: https://stackoverflow.com/questions/6796243/is-it-possible-to-make-a-horizontal-numberpicker
Stonks image source: https://wallpaperaccess.com/stonks

## CREATE A NEW gradle.properties FILE FOR THE PROJECT with the following base:
```
org.gradle.jvmargs=-Xmx2048m
android.useAndroidX=true

android.enableJetifier=true

ALPACA_KEY_ID="your_api_key_here"
ALPACA_SECRET_KEY="your_api_key_here"
TD_API_KEY="your_api_key_here"
FINNHUB_TOKEN="your_api_key_here"

DATABASE_NAME="stonks_db"
```

### Fill in "your_api_key_here" with the following:
#### Alapaca API Keys
1. Go to https://alpaca.markets/ and sign up for an account
1. Click "Live Trading" in the top left and select "Paper Trading"
1. Generate an API Key on the right hand side
1. Set ALPACA_KEY_ID and ALPACA_SECRET_KEY values in gradle.properties

#### TD API Keys
1. Go to https://developer.tdameritrade.com/apis and register
1. Click "My Apps" on the top
1. Add a new app with https://localhost:8080
1. Set TD_API_KEY value in gradle.properties

#### FinnHub API Keys
1. Go to https://finnhub.io/ and register
1. Copy the "API Key" value on the dashboard
1. Set FINNHUB_TOKEN value in gradle.properties

## Commands
### Check for lint errors
```
/gradlew spotlessCheck
```

###Apply fixed to lint errors
```
/gradlew spotlessApply
```
