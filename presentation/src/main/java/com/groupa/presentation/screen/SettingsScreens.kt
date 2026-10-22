package com.groupa.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.SuccessGreen
import com.groupa.coreui.theme.White
import com.groupa.coreui.R
import com.groupa.domain.model.User

@Composable
fun AccountSettingsScreen(
    user: User,
    selectedLanguageTag: String,
    selectedCurrencyCode: String,
    onBack: () -> Unit,
    onEditPersonalInformation: () -> Unit,
    onChangePassword: () -> Unit,
    onChangeLanguage: () -> Unit,
    onChangeCurrency: () -> Unit,
) {
    SettingsScreenScaffold(
        title = stringResource(R.string.title_account_settings),
        onBack = onBack,
    ) {
        SettingsSection(title = stringResource(R.string.section_personal_information)) {
            ConfigRow(
                title = stringResource(R.string.label_full_name),
                value = user.name,
                icon = Icons.Rounded.Person,
                onClick = onEditPersonalInformation,
            )
            SettingsDivider()
            ConfigRow(
                title = stringResource(R.string.label_email),
                value = user.email,
                icon = Icons.Rounded.Email,
                onClick = onEditPersonalInformation,
            )
            SettingsDivider()
            ConfigRow(
                title = stringResource(R.string.label_phone),
                value = user.phone,
                icon = Icons.Rounded.Phone,
                onClick = onEditPersonalInformation,
            )
        }
        SettingsSection(title = stringResource(R.string.section_security)) {
            ConfigRow(
                title = stringResource(R.string.title_change_password),
                icon = Icons.Rounded.Lock,
                onClick = onChangePassword,
            )
            SettingsDivider()
            ConfigRow(
                title = stringResource(R.string.label_two_factor_authentication),
                value = stringResource(R.string.label_enabled),
                icon = Icons.Rounded.Security,
                onClick = {},
            )
        }
        SettingsSection(title = stringResource(R.string.section_preferences)) {
            ConfigRow(
                title = stringResource(R.string.label_language),
                value = if (selectedLanguageTag.isEmpty()) {
                    stringResource(R.string.language_system)
                } else if (selectedLanguageTag.startsWith("zh")) {
                    stringResource(R.string.language_chinese)
                } else {
                    stringResource(R.string.language_english)
                },
                icon = Icons.Rounded.Language,
                onClick = onChangeLanguage,
            )
            SettingsDivider()
            ConfigRow(
                title = stringResource(R.string.label_currency),
                value = when (selectedCurrencyCode) {
                    "CNY" -> stringResource(R.string.currency_yuan)
                    "JPY" -> stringResource(R.string.currency_yen)
                    else -> stringResource(R.string.currency_usd)
                },
                icon = Icons.Rounded.AttachMoney,
                onClick = onChangeCurrency,
            )
        }
        SettingsSection(title = stringResource(R.string.section_legal)) {
            ConfigRow(
                title = stringResource(R.string.label_privacy_policy),
                icon = Icons.Rounded.PrivacyTip,
                onClick = {},
            )
            SettingsDivider()
            ConfigRow(
                title = stringResource(R.string.label_terms_of_service),
                icon = Icons.Rounded.Description,
                onClick = {},
            )
        }
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CheckoutBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                text = stringResource(R.string.action_save_changes),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun LanguageSelectionScreen(
    selectedLanguageTag: String,
    onBack: () -> Unit,
    onSelectLanguage: (String) -> Unit,
) {
    SettingsScreenScaffold(
        title = stringResource(R.string.title_language),
        onBack = onBack,
    ) {
        SettingsSection(title = stringResource(R.string.label_language)) {
            LanguageOptionRow(
                label = stringResource(R.string.language_system),
                languageTag = "",
                selectedLanguageTag = selectedLanguageTag,
                onSelectLanguage = onSelectLanguage,
            )
            SettingsDivider()
            LanguageOptionRow(
                label = stringResource(R.string.language_english),
                languageTag = "en-US",
                selectedLanguageTag = selectedLanguageTag,
                onSelectLanguage = onSelectLanguage,
            )
            SettingsDivider()
            LanguageOptionRow(
                label = stringResource(R.string.language_chinese),
                languageTag = "zh-CN",
                selectedLanguageTag = selectedLanguageTag,
                onSelectLanguage = onSelectLanguage,
            )
        }
    }
}

@Composable
fun CurrencySelectionScreen(
    selectedCurrencyCode: String,
    onBack: () -> Unit,
    onSelectCurrency: (String) -> Unit,
) {
    SettingsScreenScaffold(
        title = stringResource(R.string.label_currency),
        onBack = onBack,
    ) {
        SettingsSection(title = stringResource(R.string.label_currency)) {
            CurrencyOptionRow(
                label = stringResource(R.string.currency_usd),
                currencyCode = "USD",
                selectedCurrencyCode = selectedCurrencyCode,
                onSelectCurrency = onSelectCurrency,
            )
            SettingsDivider()
            CurrencyOptionRow(
                label = stringResource(R.string.currency_yuan),
                currencyCode = "CNY",
                selectedCurrencyCode = selectedCurrencyCode,
                onSelectCurrency = onSelectCurrency,
            )
            SettingsDivider()
            CurrencyOptionRow(
                label = stringResource(R.string.currency_yen),
                currencyCode = "JPY",
                selectedCurrencyCode = selectedCurrencyCode,
                onSelectCurrency = onSelectCurrency,
            )
        }
    }
}

@Composable
fun PaymentMethodsScreen(
    user: User,
    onBack: () -> Unit,
) {
    var savedMethods by remember {
        mutableStateOf(
            listOf(
                SavedPaymentMethod("VISA", "Visa ending in 4242", "Expires 12/25"),
                SavedPaymentMethod("MC", "Mastercard ending in 8899", "Expires 09/24"),
                SavedPaymentMethod("Pay", "Apple Pay", "Connected"),
            ),
        )
    }
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var expiryDate by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }
    var cardholderName by rememberSaveable(user.name) { mutableStateOf(user.name) }
    var saveForFuture by rememberSaveable { mutableStateOf(true) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    SettingsScreenScaffold(
        title = stringResource(R.string.title_payment_methods),
        onBack = onBack,
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF0F6FF),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = CheckoutBlue,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.label_secure_payment),
                    color = CheckoutBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        SettingsSection(title = stringResource(R.string.section_saved_methods)) {
            savedMethods.forEachIndexed { index, method ->
                if (index > 0) SettingsDivider()
                PaymentMethodRow(method)
            }
        }
        SettingsSection(title = stringResource(R.string.section_add_payment_method)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BrandBadge("VISA", Modifier.weight(1f))
                    BrandBadge("AMEX", Modifier.weight(1f))
                }
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { value -> cardNumber = value.filter(Char::isDigit).take(16) },
                    label = { Text(text = stringResource(R.string.label_card_number)) },
                    placeholder = { Text(text = "0000 0000 0000 0000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { value -> expiryDate = value.filter(Char::isDigit).take(4) },
                        label = { Text(text = stringResource(R.string.label_expiry_date)) },
                        placeholder = { Text(text = "MM/YY") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { value -> cvv = value.filter(Char::isDigit).take(3) },
                        label = { Text(text = stringResource(R.string.label_cvv)) },
                        placeholder = { Text(text = "123") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it },
                    label = { Text(text = stringResource(R.string.label_cardholder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Switch(
                        checked = saveForFuture,
                        onCheckedChange = { saveForFuture = it },
                    )
                    Text(
                        text = stringResource(R.string.label_save_card_future),
                        color = Gray500,
                        fontSize = 13.sp,
                    )
                }
                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFDC2626),
                        fontSize = 13.sp,
                    )
                }
                Button(
                    onClick = {
                        val digits = cardNumber.filter(Char::isDigit)
                        if (digits.length != 16 || expiryDate.length != 4 || cvv.length != 3 || cardholderName.isBlank()) {
                            errorMessage = "Enter complete card details."
                        } else {
                            savedMethods = listOf(
                                SavedPaymentMethod(
                                    badge = "CARD",
                                    title = "Card ending in ${digits.takeLast(4)}",
                                    subtitle = "Expires ${expiryDate.take(2)}/${expiryDate.takeLast(2)}",
                                ),
                            ) + savedMethods
                            cardNumber = ""
                            expiryDate = ""
                            cvv = ""
                            errorMessage = ""
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CheckoutBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text(
                        text = stringResource(R.string.action_add_card),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun EditPersonalInformationScreen(
    user: User,
    onBack: () -> Unit,
    onUpdateProfile: (String, String, String) -> Boolean,
) {
    var fullName by rememberSaveable(user.name) { mutableStateOf(user.name) }
    var emailAddress by rememberSaveable(user.email) { mutableStateOf(user.email) }
    var phoneNumber by rememberSaveable(user.phone) { mutableStateOf(user.phone) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    val allProfileFieldsError = stringResource(R.string.error_all_profile_fields_required)
    val unableToUpdateProfileError = stringResource(R.string.error_unable_update_profile)

    SettingsScreenScaffold(
        title = stringResource(R.string.title_edit_personal_information),
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .background(GroupBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = fullName.trim().firstOrNull()?.uppercase() ?: "T",
                    color = GroupBlue,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            TextButton(onClick = {}) {
                Text(
                    text = stringResource(R.string.action_change_photo),
                    color = CheckoutBlue,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            LabeledField(
                label = stringResource(R.string.label_full_name),
                value = fullName,
                onValueChange = { fullName = it },
            )
            LabeledField(
                label = stringResource(R.string.label_email_address),
                value = emailAddress,
                onValueChange = { emailAddress = it },
                keyboardType = KeyboardType.Email,
            )
            LabeledField(
                label = stringResource(R.string.label_phone_number),
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                keyboardType = KeyboardType.Phone,
            )
            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFDC2626),
                    fontSize = 13.sp,
                )
            }
            Button(
                onClick = {
                    if (fullName.isBlank() || emailAddress.isBlank() || phoneNumber.isBlank()) {
                        errorMessage = allProfileFieldsError
                    } else if (onUpdateProfile(fullName, emailAddress, phoneNumber)) {
                        errorMessage = ""
                        onBack()
                    } else {
                        errorMessage = unableToUpdateProfileError
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CheckoutBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_update_profile),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onChangePassword: (String, String, String) -> Boolean,
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    val strength = passwordStrength(newPassword)
    val enterCurrentPasswordError = stringResource(R.string.error_enter_current_password)
    val passwordsDoNotMatchError = stringResource(R.string.error_passwords_do_not_match)
    val passwordRequirementsError = stringResource(R.string.error_password_requirements)
    val passwordMustDifferError = stringResource(R.string.error_password_must_differ)
    val unableToUpdatePasswordError = stringResource(R.string.error_unable_update_password)

    SettingsScreenScaffold(
        title = stringResource(R.string.title_change_password),
        onBack = onBack,
    ) {
        Text(
            text = stringResource(R.string.hint_password_different),
            color = Gray500,
            fontSize = 14.sp,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            PasswordField(
                label = stringResource(R.string.label_current_password),
                value = currentPassword,
                onValueChange = { currentPassword = it },
            )
            PasswordField(
                label = stringResource(R.string.label_new_password),
                value = newPassword,
                onValueChange = { newPassword = it },
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.label_strength),
                        color = Gray500,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = stringResource(
                            listOf(
                                R.string.password_strength_weak,
                                R.string.password_strength_fair,
                                R.string.password_strength_good,
                                R.string.password_strength_strong,
                            )[strength.coerceIn(0, 3)],
                        ),
                        color = if (strength >= 2) SuccessGreen else Color(0xFFF59E0B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .background(
                                    if (index <= strength) CheckoutBlue else DividerGray,
                                    RoundedCornerShape(3.dp),
                                ),
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.hint_password_requirements),
                    color = Gray400,
                    fontSize = 12.sp,
                )
            }
            PasswordField(
                label = stringResource(R.string.label_confirm_new_password),
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
            )
            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFDC2626),
                    fontSize = 13.sp,
                )
            }
            Button(
                onClick = {
                    if (currentPassword.isBlank()) {
                        errorMessage = enterCurrentPasswordError
                    } else if (newPassword != confirmPassword) {
                        errorMessage = passwordsDoNotMatchError
                    } else if (!passwordMeetsRequirements(newPassword)) {
                        errorMessage = passwordRequirementsError
                    } else if (newPassword == currentPassword) {
                        errorMessage = passwordMustDifferError
                    } else if (onChangePassword(currentPassword, newPassword, confirmPassword)) {
                        errorMessage = ""
                        onBack()
                    } else {
                        errorMessage = unableToUpdatePasswordError
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CheckoutBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_update_password),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SettingsScreenScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(color = White, shadowElevation = 3.dp) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = title,
                color = Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        content()
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Gray500,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = White,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun ConfigRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    value: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (value != null) {
                Text(text = value, color = Gray500, fontSize = 12.sp)
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
private fun PaymentMethodRow(method: SavedPaymentMethod) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BrandBadge(method.badge)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = method.title,
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = method.subtitle, color = Gray500, fontSize = 12.sp)
        }
    }
}

@Composable
private fun BrandBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = Color(0xFFEFF6FF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDBEAFE)),
        modifier = modifier,
    ) {
        Text(
            text = text,
            color = CheckoutBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = label,
            color = Ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = stringResource(
                        if (passwordVisible) R.string.action_hide_password else R.string.action_show_password,
                    ),
                    tint = Gray400,
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LanguageOptionRow(
    label: String,
    languageTag: String,
    selectedLanguageTag: String,
    onSelectLanguage: (String) -> Unit,
) {
    val selected = if (languageTag.isEmpty()) {
        selectedLanguageTag.isEmpty()
    } else {
        selectedLanguageTag.startsWith(languageTag.substringBefore('-'))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectLanguage(languageTag) }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = CheckoutBlue,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun CurrencyOptionRow(
    label: String,
    currencyCode: String,
    selectedCurrencyCode: String,
    onSelectCurrency: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectCurrency(currencyCode) }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (selectedCurrencyCode == currencyCode) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = CheckoutBlue,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerGray),
    )
}

private fun passwordStrength(password: String): Int {
    if (password.isBlank()) return 0
    if (password.length < 8) return 0
    val hasNumber = password.any { it.isDigit() }
    val hasSpecialCharacter = password.any { !it.isLetterOrDigit() }
    val hasUppercase = password.any { it.isUpperCase() }
    return when {
        hasNumber && hasSpecialCharacter && hasUppercase -> 3
        hasNumber && hasSpecialCharacter -> 2
        hasNumber || hasSpecialCharacter -> 1
        else -> 0
    }
}

private fun passwordMeetsRequirements(password: String): Boolean {
    return password.length >= 8 &&
        password.any { it.isDigit() } &&
        password.any { !it.isLetterOrDigit() }
}

private data class SavedPaymentMethod(
    val badge: String,
    val title: String,
    val subtitle: String,
)
