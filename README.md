## Simple weather forecast android app

<a href='https://play.google.com/store/apps/details?id=de.joesch_it.chillweather&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="20%" /></a>

To run this app, add a file app/src/main/res/values/passwords.xml with the following content:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_geocoding_api_key" translatable="false"><!-- YOUR_GOOGLE_API_KEY --></string>
    <string name="darksky_api_key" translatable="false"><!-- YOUR_DARKSKY_API_KEY --></string>
    <string name="acra_uri" translatable="false"><!-- YOUR_ACRA_URI --></string>
</resources>
```
Then rename app/build.gradle.example to app/build.gradle and fill in your own passwords or remove the whole block "signingConfigs". Ready to go!