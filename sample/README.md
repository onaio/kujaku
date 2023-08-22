## Kujaku Sample App

Use this sample application to view Kujaku's features. Add the following lines in your
`../local.properties` file before compiling this module:

```
mapbox.repo.token="[YOUR MAPBOX REPO ACCESS TOKEN]"
mapbox.sdk.token=[YOUR MAPBOX SDK TOKEN]
cgr.username="[YOUR_CGR_USERNAME_WITHOUT_BRACKETS]"
cgr.password="[YOUR_CGR_PASSWORD_WITHOUT_BRACKETS]"
cgr.url="[CGR_URL_WITHOUT_BRACKETS]"
```
If you don't have a Mapbox account, [sign up](https://www.mapbox.com/signup/), and then navigate to your [Account](https://www.mapbox.com/account/) page. Copy your default public token to your clipboard then use it as `[YOUR MAPBOX SDK TOKEN]` above. Generate another token, a secret access token, that gives you access to `Downloads:Read` scope. This allows you to access the Mapbox repository with the dependencies

**NB: The `mapbox.sdk.token` has double quotation marks around the value while the `mapbox.repo.token` does not have double quotation marks around the value.**
