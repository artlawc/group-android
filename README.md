# GroupA

GroupA is an Android implementation of the GroupGO deal-discovery flow from Figma. It follows the GmailMock multi-module architecture and uses Jetpack Compose, Hilt, Kotlin Flows, and Navigation Compose.

## Screens

- Explore/home with search, category filters, featured deals, and bottom navigation
- Deal detail with hero image, inclusions, about, merchant location, and Buy Now
- Purchases with cart quantities, promo code, order totals, and order history
- Checkout with delivery method and payment selection
- Confirmation with voucher code, order ID, total paid, and redemption steps
- Placeholder Messages and Profile tabs

## Modules

- `:app` – Android entry point, Hilt application, navigation, and bottom bar
- `:presentation` – Compose screens and `GroupViewModel`
- `:domain` – models, repository contract, and use cases
- `:data` – in-memory mock repository and Hilt binding
- `:core` – functional/result primitives
- `:core-ui` – GroupGO theme, shared components, and deal images

## Build

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is written to:

```text
app\build\outputs\apk\debug\app-debug.apk
```

The project expects the Android SDK at `C:\Users\11605\timtrick\android-sdk`; adjust `local.properties` when opening on another machine.
