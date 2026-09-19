package com.hackathon.finni.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hackathon.finni.core.ui.extension.getStringResource
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.auth_action_toggle_password_visibility
import com.hackathon.finni.resources.auth_email_label
import com.hackathon.finni.resources.auth_email_placeholder
import com.hackathon.finni.resources.auth_password_label
import com.hackathon.finni.resources.ic_login
import com.hackathon.finni.resources.ic_password
import com.hackathon.finni.resources.ic_person
import com.hackathon.finni.resources.ic_visibility
import com.hackathon.finni.resources.ic_visibility_off
import com.hackathon.finni.resources.register_action_login
import com.hackathon.finni.resources.register_action_submit
import com.hackathon.finni.resources.register_already_have_account_prompt
import com.hackathon.finni.resources.register_confirm_password_label
import com.hackathon.finni.resources.register_nickname_label
import com.hackathon.finni.resources.register_nickname_placeholder
import com.hackathon.finni.resources.register_subtitle
import com.hackathon.finni.resources.register_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var nicknameText by rememberSaveable { mutableStateOf(uiState.input.nickname) }
    var emailText by rememberSaveable { mutableStateOf(uiState.input.email) }
    var passwordText by rememberSaveable { mutableStateOf(uiState.input.password) }
    var confirmPasswordText by rememberSaveable { mutableStateOf(uiState.input.confirmPassword) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Icon
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_person),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Title and Subtitle
                Text(
                    text = stringResource(Res.string.register_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Res.string.register_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nickname field
                OutlinedTextField(
                    value = nicknameText,
                    onValueChange = {
                        nicknameText = it
                        viewModel.onNicknameChange(it)
                    },
                    label = { Text(stringResource(Res.string.register_nickname_label)) },
                    placeholder = { Text(stringResource(Res.string.register_nickname_placeholder)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_person),
                            contentDescription = null
                        )
                    },
                    isError = uiState.nicknameError != null,
                    supportingText = uiState.nicknameError?.let { errorText ->
                        { Text(errorText.getStringResource()) }
                    },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Email field
                OutlinedTextField(
                    value = emailText,
                    onValueChange = {
                        emailText = it
                        viewModel.onEmailChange(it)
                    },
                    label = { Text(stringResource(Res.string.auth_email_label)) },
                    placeholder = { Text(stringResource(Res.string.auth_email_placeholder)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_person),
                            contentDescription = null
                        )
                    },
                    isError = uiState.emailError != null,
                    supportingText = uiState.emailError?.let { errorText ->
                        { Text(errorText.getStringResource()) }
                    },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Password field
                OutlinedTextField(
                    value = passwordText,
                    onValueChange = {
                        passwordText = it
                        viewModel.onPasswordChange(it)
                    },
                    label = { Text(stringResource(Res.string.auth_password_label)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_password),
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                            Icon(
                                painter = painterResource(
                                    if (uiState.isPasswordVisible) {
                                        Res.drawable.ic_visibility
                                    } else {
                                        Res.drawable.ic_visibility_off
                                    }
                                ),
                                contentDescription = stringResource(Res.string.auth_action_toggle_password_visibility),
                                tint = if (uiState.isPasswordVisible) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                        }
                    },
                    visualTransformation = if (uiState.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError?.let { errorText ->
                        { Text(errorText.getStringResource()) }
                    },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirm Password field
                OutlinedTextField(
                    value = confirmPasswordText,
                    onValueChange = {
                        confirmPasswordText = it
                        viewModel.onConfirmPasswordChange(it)
                    },
                    label = { Text(stringResource(Res.string.register_confirm_password_label)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_password),
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::onToggleConfirmPasswordVisibility) {
                            Icon(
                                painter = painterResource(
                                    if (uiState.isConfirmPasswordVisible) {
                                        Res.drawable.ic_visibility
                                    } else {
                                        Res.drawable.ic_visibility_off
                                    }
                                ),
                                contentDescription = stringResource(Res.string.auth_action_toggle_password_visibility),
                                tint = if (uiState.isConfirmPasswordVisible) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                        }
                    },
                    visualTransformation = if (uiState.isConfirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = uiState.confirmPasswordError != null,
                    supportingText = uiState.confirmPasswordError?.let { errorText ->
                        { Text(errorText.getStringResource()) }
                    },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            viewModel.onRegisterClick()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Register Button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.onRegisterClick()
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_login),
                                contentDescription = null
                            )
                            Text(stringResource(Res.string.register_action_submit))
                        }
                    }
                }

                // Already have account Link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.register_already_have_account_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = viewModel::onLoginClick) {
                        Text(stringResource(Res.string.register_action_login))
                    }
                }
            }
        }
    }
}
