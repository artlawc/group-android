package com.groupa.presentation.screen

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.R
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.White
import com.groupa.presentation.component.formatMoney
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    memberSince: String,
    totalSavings: Double,
    dealsSaved: Int,
    avatarUri: String,
    onOpenAccountSettings: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
    onOpenCoupons: () -> Unit,
    onOpenInviteFriends: () -> Unit,
    onOpenSupport: () -> Unit,
    onChangeAvatar: (String, suspend () -> String?) -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        createAvatarPickerContract(),
    ) { uri ->
        if (uri != null) {
            onChangeAvatar(uri.toString()) {
                copyAvatarToInternalStorage(context, uri)
            }
        }
    }
    val pickAvatar = {
        avatarPickerLauncher.launch(buildAvatarPickerRequest())
    }
    val avatarBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = avatarUri,
        key2 = context,
    ) {
        val effectiveAvatarUri = avatarUri
        value = if (effectiveAvatarUri.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                createCircleAvatarBitmap(context, effectiveAvatarUri)?.asImageBitmap()
            }
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 115.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = White,
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GroupBlue.copy(alpha = 0.1f),
                        modifier = Modifier.clickable(onClick = pickAvatar),
                    ) {
                        Box(
                            modifier = Modifier.size(82.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            val bitmap = avatarBitmap
                            if (bitmap == null) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = stringResource(R.string.profile_change_avatar),
                                    tint = GroupBlue,
                                    modifier = Modifier.size(36.dp),
                                )
                            } else {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = stringResource(R.string.profile_change_avatar),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                    )
                    Text(
                        text = email,
                        color = Gray400,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = stringResource(R.string.profile_member_since, memberSince),
                        color = Gray500,
                        fontSize = 12.sp,
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    title = formatMoney(totalSavings),
                    label = stringResource(R.string.profile_total_savings),
                    icon = Icons.Rounded.Savings,
                    color = GroupBlue,
                    background = Color(0xFFEFF6FF),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = dealsSaved.toString(),
                    label = stringResource(R.string.profile_deals_saved),
                    icon = Icons.Rounded.LocalOffer,
                    color = Color(0xFFF97316),
                    background = Color(0xFFFFF7ED),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = White,
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
            ) {
                Column {
                    SettingRow(
                        title = stringResource(R.string.title_account_settings),
                        icon = Icons.Rounded.Settings,
                        onClick = onOpenAccountSettings,
                    )
                    DividerLine()
                    SettingRow(
                        title = stringResource(R.string.profile_change_avatar),
                        icon = Icons.Rounded.Person,
                        onClick = pickAvatar,
                    )
                    DividerLine()
                    SettingRow(
                        title = stringResource(R.string.title_payment_methods),
                        icon = Icons.Rounded.CreditCard,
                        onClick = onOpenPaymentMethods,
                    )
                    DividerLine()
                    SettingRow(
                        title = stringResource(R.string.profile_my_coupons),
                        icon = Icons.Rounded.LocalOffer,
                        onClick = onOpenCoupons,
                        badge = stringResource(R.string.profile_coupon_badge),
                    )
                    DividerLine()
                    SettingRow(
                        title = stringResource(R.string.profile_invite_friends),
                        icon = Icons.Rounded.Share,
                        onClick = onOpenInviteFriends,
                    )
                    DividerLine()
                    SettingRow(
                        title = stringResource(R.string.profile_support),
                        icon = Icons.Rounded.Call,
                        onClick = onOpenSupport,
                    )
                }
            }
        }
        item {
            Button(
                onClick = onLogout,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = Color(0xFFEF4444),
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEE2E2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.profile_log_out),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.profile_version),
                color = Gray400,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }

}

private suspend fun copyAvatarToInternalStorage(
    context: Context,
    sourceUri: Uri,
): String? = withContext(Dispatchers.IO) {
    runCatching {
        val destination = File(
            context.filesDir,
            "profile_avatar_${System.currentTimeMillis()}.jpg",
        )
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            destination.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: return@runCatching null
        Uri.fromFile(destination).toString()
    }.getOrNull()
}

private fun createCircleAvatarBitmap(
    context: Context,
    avatarUri: String,
): Bitmap? {
    val sourceBitmap = decodeAvatarBitmap(context, avatarUri) ?: return null
    val size = min(sourceBitmap.width, sourceBitmap.height)
    val outputBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(outputBitmap)
    val circlePath = Path().apply {
        addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CCW)
    }
    canvas.clipPath(circlePath)
    val horizontalOffset = (sourceBitmap.width - size) / 2f
    val verticalOffset = (sourceBitmap.height - size) / 2f
    canvas.drawBitmap(
        sourceBitmap,
        -horizontalOffset,
        -verticalOffset,
        Paint(Paint.ANTI_ALIAS_FLAG),
    )
    return outputBitmap
}

internal fun decodeAvatarBitmap(
    context: Context,
    avatarUri: String,
): Bitmap? {
    val uri = Uri.parse(avatarUri)
    val filePath = uri.path
    return if (uri.scheme.equals("file", ignoreCase = true) && !filePath.isNullOrBlank()) {
        decodeFileAvatarBitmap(filePath)
    } else {
        decodeContentAvatarBitmap(context, uri)
    }
}

private fun decodeFileAvatarBitmap(filePath: String): Bitmap? {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(filePath, boundsOptions)
    val largestDimension = max(boundsOptions.outWidth, boundsOptions.outHeight)
    if (largestDimension <= 0) return null

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = max(1, largestDimension / 512)
    }
    return BitmapFactory.decodeFile(filePath, decodeOptions)
}

private fun decodeContentAvatarBitmap(
    context: Context,
    uri: Uri,
): Bitmap? {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream, null, boundsOptions)
    } ?: return null

    val largestDimension = max(boundsOptions.outWidth, boundsOptions.outHeight)
    if (largestDimension <= 0) return null

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = max(1, largestDimension / 512)
    }
    return context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream, null, decodeOptions)
    }
}

@Composable
private fun StatCard(
    title: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = background,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = title,
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Text(
                text = label,
                color = Gray500,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    badge: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF3F4F6)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4B5563),
                modifier = Modifier
                    .padding(7.dp)
                    .size(18.dp),
            )
        }
        Text(
            text = title,
            color = Color(0xFF374151),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CheckoutBlue.copy(alpha = 0.1f),
            ) {
                Text(
                    text = badge,
                    color = CheckoutBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerGray),
    )
}
