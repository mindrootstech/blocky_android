//package com.example.parentalcontrol.ui.screens
//
//import android.widget.Toast
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.parentalcontrol.utils.PreferenceManager
//
//@Composable
//fun SignupScreen(preferenceManager: PreferenceManager, onSignupSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    val context = LocalContext.current
//
//    Column(
//        modifier = Modifier.fillMaxSize().padding(24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text("Create Parent Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)
//        Spacer(modifier = Modifier.height(32.dp))
//
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("Email") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = confirmPassword,
//            onValueChange = { confirmPassword = it },
//            label = { Text("Confirm Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(32.dp))
//
//        Button(
//            onClick = {
//                if (email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword) {
//                    preferenceManager.userEmail = email
//                    preferenceManager.userPassword = password
//                    preferenceManager.isLoggedIn = true
//                    onSignupSuccess()
//                } else {
//                    Toast.makeText(context, "Passwords do not match or fields are empty", Toast.LENGTH_SHORT).show()
//                }
//            },
//            modifier = Modifier.fillMaxWidth().height(50.dp)
//        ) {
//            Text("Sign Up")
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//
//        TextButton(onClick = onNavigateToLogin) {
//            Text("Already have an account? Login")
//        }
//    }
//}
