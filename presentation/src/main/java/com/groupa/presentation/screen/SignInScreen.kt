package com.groupa.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.Slate900
import com.groupa.coreui.theme.White
import com.groupa.coreui.R

@Composable
fun SignInScreen(
    onSignIn: (String, String) -> Boolean,
    onProviderSignIn: (String) -> Boolean,
    onForgotPassword: () -> Unit,
    onDismiss: () -> Unit,
) {
    var useEmail by rememberSaveable { mutableStateOf(true) }
    var identifier by rememberSaveable { mutableStateOf("tim@qq.com") }
    var password by rememberSaveable { mutableStateOf("time123") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    val emailLabel = stringResource(R.string.label_email_address)
    val phoneLabel = stringResource(R.string.label_phone_number)
    val enterValidCredentialsError = stringResource(R.string.error_enter_valid_credentials)
    val unableToSignInError = stringResource(R.string.error_unable_to_sign_in)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "DealSpot",
                color = CheckoutBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.action_close),
                    tint = Ink,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F5F9),
        ) {
            Row(
                modifier = Modifier.padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                listOf(emailLabel, phoneLabel).forEachIndexed { index, label ->
                    val selected = useEmail == (index == 0)
                    Surface(
                        onClick = { useEmail = index == 0 },
                        shape = RoundedCornerShape(9.dp),
                        color = if (selected) White else Color.Transparent,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = label,
                            color = if (selected) Slate900 else Gray500,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = if (useEmail) emailLabel else phoneLabel) },
            leadingIcon = {
                Icon(
                    imageVector = if (useEmail) Icons.Rounded.Email else Icons.Rounded.Phone,
                    contentDescription = null,
                    tint = Gray400,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (useEmail) KeyboardType.Email else KeyboardType.Phone,
            ),
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.label_password)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Gray400,
                )
            },
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
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
        )

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = Color(0xFFDC2626),
                fontSize = 13.sp,
            )
        }

        TextButton(
            onClick = onForgotPassword,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                text = stringResource(R.string.action_forgot_password),
                color = CheckoutBlue,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Button(
            onClick = {
                val enteredIdentifier = identifier.trim()
                val enteredPassword = password.trim()
                if (enteredIdentifier.isBlank() || enteredPassword.isBlank()) {
                    errorMessage = String.format(
                        enterValidCredentialsError,
                        if (useEmail) emailLabel else phoneLabel,
                    )
                } else if (onSignIn(enteredIdentifier, enteredPassword)) {
                    errorMessage = ""
                } else {
                    errorMessage = unableToSignInError
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CheckoutBlue),
        ) {
            Text(
                text = stringResource(R.string.title_sign_in),
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(DividerGray),
            )
            Text(
                text = stringResource(R.string.label_or_continue_with),
                color = Gray500,
                fontSize = 12.sp,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(DividerGray),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onProviderSignIn("Google") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = Slate900,
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Google")
            }
            Button(
                onClick = { onProviderSignIn("Apple") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Apple")
            }
        }

        TextButton(
            onClick = onForgotPassword,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.action_sign_up),
                color = Gray500,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
