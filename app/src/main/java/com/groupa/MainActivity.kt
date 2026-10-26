package com.groupa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.pm.PackageManager
import android.net.Uri
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.ContextThemeWrapper
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.GroupATheme
import com.groupa.domain.model.GroupMessage
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.White
import com.groupa.coreui.util.AppLanguage
import com.groupa.coreui.R as CoreUiR
import com.groupa.navigation.GroupRoutes
import com.groupa.presentation.download.CarModelDownloadManager
import com.groupa.presentation.screen.AccountSettingsScreen
import com.groupa.presentation.screen.BodyMuscleScreen
import com.groupa.presentation.screen.Car3DScreen
import com.groupa.presentation.screen.CheckoutScreen
import com.groupa.presentation.screen.ChatScreen
import com.groupa.presentation.screen.ChangePasswordScreen
import com.groupa.presentation.screen.ConfirmationScreen
import com.groupa.presentation.screen.CouponDetailScreen
import com.groupa.presentation.screen.CouponsScreen
import com.groupa.presentation.screen.ContactsScreen
import com.groupa.presentation.screen.CurrencySelectionScreen
import com.groupa.presentation.screen.DealDetailScreen
import com.groupa.presentation.screen.EditPersonalInformationScreen
import com.groupa.presentation.screen.HomeScreen
import com.groupa.presentation.screen.InviteFriendsScreen
import com.groupa.presentation.screen.LanguageSelectionScreen
import com.groupa.presentation.screen.MapViewScreen
import com.groupa.presentation.screen.MessagesScreen
import com.groupa.presentation.screen.PaymentMethodsScreen
import com.groupa.presentation.screen.ProfileScreen
import com.groupa.presentation.screen.PurchasesScreen
import com.groupa.presentation.screen.SignInScreen
import com.groupa.presentation.screen.SplashScreen
import com.groupa.presentation.screen.StockInfoScreen
import com.groupa.presentation.screen.SupportScreen
import com.groupa.presentation.viewmodel.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val languagePreferences by lazy {
        getSharedPreferences(APP_LANGUAGE_PREFERENCES, Context.MODE_PRIVATE)
    }
    private val currencyPreferences by lazy {
        getSharedPreferences(APP_CURRENCY_PREFERENCES, Context.MODE_PRIVATE)
    }
    private var appLanguageTag by mutableStateOf("")
    private var appCurrencyCode by mutableStateOf(DEFAULT_CURRENCY_CODE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appLanguageTag = languagePreferences.getString(APP_LANGUAGE_TAG, "").orEmpty()
        appCurrencyCode = currencyPreferences.getString(APP_CURRENCY_CODE, DEFAULT_CURRENCY_CODE).orEmpty()
        setContent {
            val localizedContext = remember(appLanguageTag) {
                createLocalizedContext(appLanguageTag)
            }
            CompositionLocalProvider(LocalContext provides localizedContext) {
                GroupATheme {
                    GroupApp(
                        appLanguageTag = appLanguageTag,
                        onSelectLanguage = ::updateAppLanguage,
                        appCurrencyCode = appCurrencyCode,
                        onSelectCurrency = ::updateAppCurrency,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        CarModelDownloadManager.markAppInForeground()
    }

    override fun onStop() {
        CarModelDownloadManager.markAppInBackground()
        super.onStop()
    }

    private fun createLocalizedContext(languageTag: String): Context {
        val resolvedLanguageTag = if (languageTag.isEmpty()) {
            LocaleListCompat.getDefault()[0]?.toLanguageTag().orEmpty()
        } else {
            languageTag
        }
        val localeTag = AppLanguage.resolveStoredTag(
            storedLanguageTag = resolvedLanguageTag,
            systemLanguageTag = resolvedLanguageTag,
        )

        val configuration = Configuration(resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(localeTag))
        return LocalizedContext(configuration)
    }

    private inner class LocalizedContext(
        overrideConfiguration: Configuration,
    ) : ContextThemeWrapper(this, R.style.Theme_GroupA) {
        init {
            applyOverrideConfiguration(overrideConfiguration)
        }
    }

    private fun updateAppLanguage(languageTag: String) {
        languagePreferences.edit().putString(APP_LANGUAGE_TAG, languageTag).apply()
        appLanguageTag = languageTag
    }

    private fun updateAppCurrency(currencyCode: String) {
        currencyPreferences.edit().putString(APP_CURRENCY_CODE, currencyCode).apply()
        appCurrencyCode = currencyCode
    }

    private companion object {
        const val APP_LANGUAGE_PREFERENCES = "groupa_language"
        const val APP_LANGUAGE_TAG = "app_language_tag"
        const val APP_CURRENCY_PREFERENCES = "groupa_currency"
        const val APP_CURRENCY_CODE = "app_currency_code"
        const val DEFAULT_CURRENCY_CODE = "USD"
    }
}

@Composable
private fun GroupApp(
    appLanguageTag: String,
    onSelectLanguage: (String) -> Unit,
    appCurrencyCode: String,
    onSelectCurrency: (String) -> Unit,
) {
    val navController = rememberNavController()
    val viewModel: GroupViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showCarModelReadyBanner by remember { mutableStateOf(false) }
    val carModelDownloadMessage = GroupMessage(
        id = "car-3d-model-download-ready",
        title = "3D Model Ready",
        preview = "The car model has finished downloading. Tap to view the 3D dashboard.",
        time = "Now",
        isChat = false,
        unread = true,
        avatarLabel = "C",
        car3dId = "carconcept",
    )

    LaunchedEffect(Unit) {
        CarModelDownloadManager.completionEvent.collect {
            showCarModelReadyBanner = true
        }
    }

    LaunchedEffect(showCarModelReadyBanner) {
        if (showCarModelReadyBanner) {
            delay(5_000)
            showCarModelReadyBanner = false
        }
    }

    LaunchedEffect(showCarModelReadyBanner, currentRoute) {
        if (showCarModelReadyBanner && currentRoute == GroupRoutes.HOME) {
            delay(5_000)
            if (showCarModelReadyBanner) {
                viewModel.addMessage(carModelDownloadMessage)
                showCarModelReadyBanner = false
            }
        }
    }

    LaunchedEffect(state.user.isSignedIn, currentRoute) {
        if (state.user.isSignedIn && currentRoute == GroupRoutes.SIGN_IN) {
            navController.navigate(GroupRoutes.HOME) {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GroupNavHost(
            navController = navController,
            viewModel = viewModel,
            state = state,
            appLanguageTag = appLanguageTag,
            onSelectLanguage = onSelectLanguage,
            appCurrencyCode = appCurrencyCode,
            onSelectCurrency = onSelectCurrency,
        )
        if (currentRoute in GroupRoutes.bottomRoutes) {
            GroupBottomBar(
                currentRoute = currentRoute,
                showMessagesBadge = state.hasUnreadMessages,
                onTabSelected = { route -> navController.navigateRoot(route) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            )
        }
        if (showCarModelReadyBanner) {
            CarModelDownloadReadyBanner(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                onOpen = {
                    showCarModelReadyBanner = false
                    navController.navigate(GroupRoutes.car3d("carconcept"))
                },
            )
        }
    }
}

@Composable
private fun CarModelDownloadReadyBanner(
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
) {
    Surface(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(onClick = onOpen),
        color = White,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "3D model ready",
                    color = Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Tap to view the car dashboard",
                    color = Ink.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                )
            }
            Text(
                text = "View",
                color = GroupBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun GroupNavHost(
    navController: NavHostController,
    viewModel: GroupViewModel,
    state: com.groupa.presentation.viewmodel.GroupUiState,
    appLanguageTag: String,
    onSelectLanguage: (String) -> Unit,
    appCurrencyCode: String,
    onSelectCurrency: (String) -> Unit,
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = GroupRoutes.SPLASH,
    ) {
        composable(GroupRoutes.SPLASH) {
            SplashScreen(
                onTimeout = {
                    val destination = if (state.user.isSignedIn) GroupRoutes.HOME else GroupRoutes.SIGN_IN
                    navController.navigate(destination) {
                        popUpTo(GroupRoutes.SPLASH) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(GroupRoutes.SIGN_IN) {
            SignInScreen(
                onSignIn = { email, password ->
                    viewModel.signIn(email, password) != null
                },
                onProviderSignIn = { provider ->
                    viewModel.signInWithProvider(provider) != null
                },
                onForgotPassword = { navController.navigate(GroupRoutes.SUPPORT) },
                onDismiss = {
                    navController.navigate(GroupRoutes.HOME) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(GroupRoutes.HOME) {
            val currentLocation = rememberDetectedLocation()
            HomeScreen(
                deals = state.deals,
                locationLabel = currentLocation.label,
                onDealClick = { dealId -> navController.navigate(GroupRoutes.dealDetail(dealId)) },
                onLocationClick = {
                    val mapTarget = if (
                        currentLocation.latitude != null &&
                        currentLocation.longitude != null
                    ) {
                        "${currentLocation.latitude},${currentLocation.longitude}"
                    } else {
                        currentLocation.label
                    }
                    navController.navigate(GroupRoutes.map(mapTarget, currentLocation.label))
                },
            )
        }
        composable(GroupRoutes.DEAL_DETAIL) { entry ->
            val dealId = entry.arguments?.getString("dealId").orEmpty()
            val deal = state.deals.firstOrNull { it.id == dealId }
            if (deal != null) {
                DealDetailScreen(
                    deal = deal,
                    onBack = { navController.popBackStack() },
                    onBuyNow = { id ->
                        viewModel.addDealToCart(id)
                        navController.navigate(GroupRoutes.PURCHASES)
                    },
                    onOpenMap = {
                        val dealTarget = "${deal.latitude},${deal.longitude}"
                        navController.navigate(GroupRoutes.map(dealTarget, deal.merchant))
                    },
                )
            }
        }
        composable(GroupRoutes.MAP) { entry ->
            MapViewScreen(
                target = entry.arguments?.getString("target").orEmpty(),
                label = entry.arguments?.getString("label").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(GroupRoutes.PURCHASES) {
            val showPurchasesBack =
                navController.previousBackStackEntry?.destination?.route == GroupRoutes.DEAL_DETAIL
            PurchasesScreen(
                state = state,
                showBack = showPurchasesBack,
                onBack = { navController.popBackStack(GroupRoutes.HOME, false) },
                onQuantityChange = viewModel::changeQuantity,
                onRemove = viewModel::removeItem,
                onPromoInputChange = viewModel::onPromoInputChange,
                onApplyPromo = viewModel::applyPromo,
                onCheckout = { navController.navigate(GroupRoutes.CHECKOUT) },
                onViewVoucher = { orderId -> navController.navigate(GroupRoutes.confirmation(orderId)) },
                onBuyAgain = { dealId ->
                    viewModel.addDealToCart(dealId)
                    navController.navigate(GroupRoutes.PURCHASES)
                },
                onOpenDeal = { dealId -> navController.navigate(GroupRoutes.dealDetail(dealId)) },
                initialTab = "Cart",
            )
        }
        composable(GroupRoutes.ORDERS) {
            PurchasesScreen(
                state = state,
                showBack = false,
                onBack = { navController.popBackStack(GroupRoutes.HOME, false) },
                onQuantityChange = viewModel::changeQuantity,
                onRemove = viewModel::removeItem,
                onPromoInputChange = viewModel::onPromoInputChange,
                onApplyPromo = viewModel::applyPromo,
                onCheckout = { navController.navigate(GroupRoutes.CHECKOUT) },
                onViewVoucher = { orderId -> navController.navigate(GroupRoutes.confirmation(orderId)) },
                onBuyAgain = { dealId ->
                    viewModel.addDealToCart(dealId)
                    navController.navigate(GroupRoutes.PURCHASES)
                },
                onOpenDeal = { dealId -> navController.navigate(GroupRoutes.dealDetail(dealId)) },
                initialTab = "Orders",
            )
        }
        composable(GroupRoutes.CHECKOUT) {
            CheckoutScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onConfirmAndPay = {
                    val order = viewModel.purchaseCart()
                    navController.navigate(GroupRoutes.confirmation(order.orderId)) {
                        popUpTo(GroupRoutes.HOME)
                    }
                },
            )
        }
        composable(GroupRoutes.CONFIRMATION) { entry ->
            val orderId = entry.arguments?.getString("orderId").orEmpty()
            val order = state.orders.firstOrNull { it.orderId == orderId }
            if (order != null) {
                ConfirmationScreen(
                    order = order,
                    onBackToExplore = {
                        navController.popBackStack(GroupRoutes.HOME, false)
                    },
                    onViewVoucher = { navController.navigate(GroupRoutes.ORDERS) },
                    onShareVoucher = {
                        viewModel.sendChat(
                            "Voucher ${order.code} for ${order.title} at ${order.merchant} is ready to use.",
                        )
                        state.chats.firstOrNull()?.let { chat ->
                            navController.navigate(GroupRoutes.chat(chat.id))
                        }
                    },
                )
            }
        }
        composable(GroupRoutes.MESSAGES) {
            MessagesScreen(
                state = state,
                onOpenChat = { threadId -> navController.navigate(GroupRoutes.chat(threadId)) },
                onOpenOrder = { orderId -> navController.navigate(GroupRoutes.confirmation(orderId)) },
                onOpenStock = { stockId -> navController.navigate(GroupRoutes.stockInfo(stockId)) },
                onOpenBodyMuscle = { muscleId -> navController.navigate(GroupRoutes.bodyMuscles(muscleId)) },
                onOpenCar3D = { carId -> navController.navigate(GroupRoutes.car3d(carId)) },
                onMarkAllRead = viewModel::markAllMessagesAsRead,
                onOpenContacts = { navController.navigate(GroupRoutes.CONTACTS) },
                onMessageRead = viewModel::markMessageAsRead,
                onDeleteMessages = viewModel::deleteMessages,
            )
        }
        composable(GroupRoutes.CONTACTS) {
            ContactsScreen(
                contacts = state.contacts,
                onBack = { navController.popBackStack() },
                onAddContact = viewModel::addContact,
            )
        }
        composable(GroupRoutes.BODY_MUSCLES) { entry ->
            BodyMuscleScreen(
                initialMuscleId = entry.arguments?.getString("muscleId"),
                onBack = { navController.popBackStack() },
            )
        }
        composable(GroupRoutes.PROFILE) {
            ProfileScreen(
                name = state.user.name,
                email = state.user.email,
                memberSince = state.user.memberSince,
                totalSavings = state.user.totalSavings,
                dealsSaved = state.user.dealsSaved,
                avatarUri = state.user.avatarUri,
                version = BuildConfig.VERSION_NAME,
                onOpenAccountSettings = { navController.navigate(GroupRoutes.ACCOUNT) },
                onOpenPaymentMethods = { navController.navigate(GroupRoutes.PAYMENT) },
                onOpenCoupons = { navController.navigate(GroupRoutes.COUPONS) },
                onOpenInviteFriends = { navController.navigate(GroupRoutes.INVITE) },
                onOpenSupport = { navController.navigate(GroupRoutes.SUPPORT) },
                onChangeAvatar = { avatarUri, copyPickedAvatar ->
                    viewModel.updateAvatar(avatarUri, copyPickedAvatar)
                },
                onLogout = {
                    viewModel.signOutUser()
                    navController.navigate(GroupRoutes.SIGN_IN) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(GroupRoutes.CAR_3D) { entry ->
            Car3DScreen(
                carId = entry.arguments?.getString("carId").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(GroupRoutes.STOCK_INFO) { entry ->
            val stockId = entry.arguments?.getString("stockId").orEmpty()
            val stock = state.stocks.firstOrNull { it.id == stockId }
            if (stock != null) {
                StockInfoScreen(
                    stock = stock,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(GroupRoutes.ACCOUNT) {
            AccountSettingsScreen(
                user = state.user,
                selectedLanguageTag = appLanguageTag,
                selectedCurrencyCode = appCurrencyCode,
                onBack = { navController.popBackStack() },
                onEditPersonalInformation = { navController.navigate(GroupRoutes.EDIT_PROFILE) },
                onChangePassword = { navController.navigate(GroupRoutes.CHANGE_PASSWORD) },
                onChangeLanguage = { navController.navigate(GroupRoutes.LANGUAGE) },
                onChangeCurrency = { navController.navigate(GroupRoutes.CURRENCY) },
            )
        }
        composable(GroupRoutes.LANGUAGE) {
            LanguageSelectionScreen(
                selectedLanguageTag = appLanguageTag,
                onBack = { navController.popBackStack() },
                onSelectLanguage = onSelectLanguage,
            )
        }
        composable(GroupRoutes.CURRENCY) {
            CurrencySelectionScreen(
                selectedCurrencyCode = appCurrencyCode,
                onBack = { navController.popBackStack() },
                onSelectCurrency = onSelectCurrency,
            )
        }
        composable(GroupRoutes.PAYMENT) {
            PaymentMethodsScreen(
                user = state.user,
                onBack = { navController.popBackStack() },
            )
        }
        composable(GroupRoutes.EDIT_PROFILE) {
            EditPersonalInformationScreen(
                user = state.user,
                onBack = { navController.popBackStack() },
                onUpdateProfile = viewModel::updateProfile,
            )
        }
        composable(GroupRoutes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                onBack = { navController.popBackStack() },
                onChangePassword = { currentPassword, newPassword, _ ->
                    viewModel.changePassword(currentPassword, newPassword)
                },
            )
        }
        composable(GroupRoutes.CHAT) { entry ->
            val threadId = entry.arguments?.getString("threadId").orEmpty()
            val thread = state.chats.firstOrNull { it.id == threadId }
            if (thread != null) {
                LaunchedEffect(thread.id) {
                    viewModel.markMessageAsRead(thread.id)
                }
                val dealId = when (thread.id) {
                    "gourmet-burger" -> "burger"
                    "fitzone" -> "gym"
                    else -> "burger"
                }
                ChatScreen(
                    thread = thread,
                    messages = state.chatMessages,
                    onSendMessage = viewModel::sendChat,
                    onOpenDeal = { navController.navigate(GroupRoutes.dealDetail(dealId)) },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(GroupRoutes.COUPONS) {
            CouponsScreen(
                coupons = state.coupons,
                onBack = { navController.popBackStack() },
                onOpenCoupon = { couponId ->
                    navController.navigate(GroupRoutes.couponDetail(couponId))
                },
            )
        }
        composable(GroupRoutes.COUPON_DETAIL) { entry ->
            val couponId = entry.arguments?.getString("couponId").orEmpty()
            val coupon = state.coupons.firstOrNull { it.id == couponId }
            if (coupon != null) {
                CouponDetailScreen(
                    coupon = coupon,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(GroupRoutes.INVITE) {
            InviteFriendsScreen(
                onBack = { navController.popBackStack() },
                onInviteWeChat = {
                    openThirdPartyInvite(
                        context = context,
                        packageName = "com.tencent.mm",
                        fallbackUrl = "https://weixin.qq.com/",
                    )
                },
                onInviteTwitter = {
                    val inviteMessage = context.getString(CoreUiR.string.invite_message)
                    openThirdPartyInvite(
                        context = context,
                        packageName = "com.twitter.android",
                        fallbackUrl = "https://twitter.com/intent/tweet?text=${Uri.encode(inviteMessage)}",
                    )
                },
                onInviteDouyin = {
                    openThirdPartyInvite(
                        context = context,
                        packageName = "com.ss.android.ugc.aweme",
                        fallbackUrl = "https://www.douyin.com/",
                    )
                },
                onInviteTaobao = {
                    openThirdPartyInvite(
                        context = context,
                        packageName = "com.taobao.taobao",
                        fallbackUrl = "https://www.taobao.com/",
                    )
                },
                onInviteAlipay = {
                    openThirdPartyInvite(
                        context = context,
                        packageName = "com.eg.android.AlipayGphone",
                        fallbackUrl = "https://mobile.alipay.com/",
                    )
                },
                onInvitePinduoduo = {
                    openThirdPartyInvite(
                        context = context,
                        packageName = "com.xunmeng.pinduoduo",
                        fallbackUrl = "https://www.pinduoduo.com/",
                    )
                },
            )
        }
        composable(GroupRoutes.SUPPORT) {
            SupportScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun rememberDetectedLocation(): DetectedLocation {
    val context = LocalContext.current
    val detectingLabel = stringResource(CoreUiR.string.location_detecting)
    var detectedLocation by remember {
        mutableStateOf(DetectedLocation(detectingLabel, null, null))
    }
    val coroutineScope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        onLocationPermissionsResult(
            permissions = permissions,
            permissionDeniedLabel = context.getString(CoreUiR.string.location_permission_denied),
            onDetectLocation = {
                coroutineScope.launch {
                    detectedLocation = detectCurrentLocation(context) { locationResult ->
                        detectedLocation = locationResult
                    }
                }
            },
            onPermissionDenied = { detectedLocation = it },
        )
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
            detectedLocation = detectCurrentLocation(context) { cachedLocation ->
                detectedLocation = cachedLocation
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    return detectedLocation
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
}

private suspend fun detectCurrentLocation(
    context: Context,
    onCachedLocation: (DetectedLocation) -> Unit,
): DetectedLocation {
    val location = withContext(Dispatchers.IO) {
        findLastKnownLocation(context)
    }

    if (location != null) {
        onLocationResult(location.latitude, location.longitude, onCachedLocation)
    }

    val freshLocation = withContext(Dispatchers.IO) {
        requestFreshLocation(context)
    }
    if (freshLocation != null) {
        onLocationResult(
            freshLocation.latitude,
            freshLocation.longitude,
            onCachedLocation,
        )
    }
    val resolvedLocation = freshLocation ?: location

    if (resolvedLocation == null) {
        return DetectedLocation(
            label = context.getString(CoreUiR.string.location_permission_denied),
            latitude = null,
            longitude = null,
        )
    }

    val label = withTimeoutOrNull(2_000) {
        withContext(Dispatchers.IO) {
            reverseGeocodeLocation(
                context,
                resolvedLocation.latitude,
                resolvedLocation.longitude,
            )
        }
    } ?: String.format(
        Locale.US,
        "%.4f, %.4f",
        resolvedLocation.latitude,
        resolvedLocation.longitude,
    )

    return DetectedLocation(
        label = label,
        latitude = resolvedLocation.latitude,
        longitude = resolvedLocation.longitude,
    )
}

@SuppressLint("MissingPermission")
private suspend fun requestFreshLocation(context: Context): Location? {
    val locationManager = context.getSystemService(LocationManager::class.java) ?: return null
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    ).filter { locationManager.isProviderEnabled(it) }

    if (providers.isEmpty()) return null

    return withTimeoutOrNull(3_000) {
        suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
            }

            providers.forEach { provider ->
                locationManager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper(),
                )
            }

            continuation.invokeOnCancellation {
                locationManager.removeUpdates(listener)
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun findLastKnownLocation(context: Context): Location? {
    val locationManager = context.getSystemService(LocationManager::class.java) ?: return null
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    )

    return providers.firstNotNullOfOrNull { provider ->
        if (locationManager.isProviderEnabled(provider)) {
            locationManager.getLastKnownLocation(provider)
        } else {
            null
        }
    }
}

@Suppress("DEPRECATION")
private fun reverseGeocodeLocation(
    context: Context,
    latitude: Double,
    longitude: Double,
): String? {
    if (!Geocoder.isPresent()) return null
    val address = Geocoder(context, Locale.getDefault())
        .getFromLocation(latitude, longitude, 1)
        ?.firstOrNull()
        ?: return null
    val label = listOfNotNull(
        address.thoroughfare,
        address.subLocality,
        address.locality,
        address.adminArea,
    )
        .distinct()
        .joinToString(", ")

    return label.ifBlank { null }
}

private fun openThirdPartyInvite(
    context: Context,
    packageName: String,
    fallbackUrl: String,
) {
    val inviteMessage = context.getString(CoreUiR.string.invite_message)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, inviteMessage)
        setPackage(packageName)
    }
    if (shareIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(shareIntent)
    } else {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)))
    }
}

private fun NavHostController.navigateRoot(route: String) {
    navigate(route) {
        if (route == GroupRoutes.HOME || route == GroupRoutes.MESSAGES) {
            popUpTo(0) {
                inclusive = true
            }
            restoreState = false
        } else {
            popUpTo(GroupRoutes.HOME) {
                saveState = true
            }
            restoreState = true
        }
        launchSingleTop = true
    }
}

@Composable
private fun GroupBottomBar(
    currentRoute: String?,
    showMessagesBadge: Boolean,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = White,
        shadowElevation = 14.dp,
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BottomBarItem(
                label = stringResource(CoreUiR.string.label_explore),
                icon = Icons.Rounded.Explore,
                selected = currentRoute == GroupRoutes.HOME,
                onClick = { onTabSelected(GroupRoutes.HOME) },
                modifier = Modifier.weight(1f),
            )
            BottomBarItem(
                label = stringResource(CoreUiR.string.label_messages),
                icon = Icons.Rounded.ChatBubble,
                selected = currentRoute == GroupRoutes.MESSAGES,
                showBadge = showMessagesBadge,
                onClick = { onTabSelected(GroupRoutes.MESSAGES) },
                modifier = Modifier.weight(1f),
            )
            BottomBarItem(
                label = stringResource(CoreUiR.string.label_purchases),
                icon = Icons.Rounded.ShoppingBag,
                selected = currentRoute == GroupRoutes.PURCHASES || currentRoute == GroupRoutes.ORDERS,
                onClick = { onTabSelected(GroupRoutes.PURCHASES) },
                modifier = Modifier.weight(1f),
            )
            BottomBarItem(
                label = stringResource(CoreUiR.string.label_profile),
                icon = Icons.Rounded.Person,
                selected = currentRoute == GroupRoutes.PROFILE,
                onClick = { onTabSelected(GroupRoutes.PROFILE) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBadge: Boolean = false,
) {
    val contentColor = if (selected) GroupBlue else Color(0xFF9CA3AF)
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(23.dp),
            )
            if (showBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 1.dp)
                        .size(7.dp)
                        .background(Color(0xFFEF4444), CircleShape),
                )
            }
        }
        Text(
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}
