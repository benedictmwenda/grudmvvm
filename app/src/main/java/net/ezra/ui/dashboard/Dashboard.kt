package net.ezra.ui.dashboard



import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import net.ezra.navigation.ROUTE_LOGIN

import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import net.ezra.navigation.ROUTE_ADD_STUDENTS
import net.ezra.navigation.ROUTE_DASHBOARD
import net.ezra.navigation.ROUTE_HOME


private var progressDialog: ProgressDialog? = null
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DashboardScreen(navController: NavHostController)  {
    var school by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    var user: User? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }
    var studentCount by remember { mutableIntStateOf(0) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }

    val firestores = Firebase.firestore


    val context = LocalContext.current

    BackHandler {
        navController.popBackStack()

    }


    // Fetch user details from Firestore
    LaunchedEffect(key1 = currentUser?.uid) {
        if (currentUser != null) {
            val userDocRef = firestore.collection("users").document(currentUser.uid)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        user = document.toObject<User>()
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    isLoading = false
                }
        }
    }

    LaunchedEffect(Unit) {
        firestores.collection("Students")
            .get()
            .addOnSuccessListener { result ->
                studentCount = result.size()
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Dashboard", color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xff0FB06A),
                    titleContentColor = Color.White,
                ),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.ArrowBack, "backIcon",tint = Color.White)
                    }
                },



            )
        }, content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xff9AEDC9)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                LazyColumn(

                    modifier = Modifier
                    .fillMaxSize()

                ) {
                    item {


                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                Text(text = "Total Students: $studentCount")

                                IconButton(onClick = {
                                    firestores.collection("Students")
                                        .get()
                                        .addOnSuccessListener { result ->
                                            studentCount = result.size()
                                        }
                                        .addOnFailureListener { exception ->
                                            // Handle failures
                                        }
                                }) {
                                    Icon(Icons.Filled.Refresh, "backIcon")
                                }

                            }




                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(onClick = {

                                navController.navigate(ROUTE_ADD_STUDENTS) {
                                    popUpTo(ROUTE_DASHBOARD) { inclusive = true }
                                }

                            }) {

                                Text("Add Students")

                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("User Details", style = MaterialTheme.typography.h4)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Display the email of the logged-in user
                            Text("Email: ${currentUser?.email ?: "N/A"}")

                            if (isLoading) {
                                // Show loading indicator
                                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            } else {
                                // Display user details
                                user?.let {
                                    Text("School: ${it.school}")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Name: ${it.name}")
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Add more user details here
                                }
                            }


                            Spacer(modifier = Modifier.height(15.dp))

                            Text(text = "Update profile")
                            OutlinedTextField(
                                value = school,
                                onValueChange = { school = it },
                                label = { Text("School") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Add a TextField for entering name
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Add a button to submit the details
                            Button(
                                onClick = {

                                    progressDialog = ProgressDialog(context)
                                    progressDialog?.setMessage("Updating profile...")
                                    progressDialog?.setCancelable(false)
                                    progressDialog?.show()

                                    val user = User(currentUser!!.uid, school, name)

                                    saveUserDetails(user) {
                                        // Handle success or failure
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Submit")
                            }


                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    FirebaseAuth.getInstance().signOut()
                                    navController.navigate(ROUTE_LOGIN)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Logout")
                            }


                            TextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text("Current Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                visualTransformation = PasswordVisualTransformation(),
                                label = { Text("New Password") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm New Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))


                            if (loading) {
                                // Show progress bar if loading
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {

                                Button(
                                    onClick = {
                                        if (newPassword == confirmPassword) {

                                            loading = true

                                            val credential = currentUser?.email?.let {
                                                EmailAuthProvider.getCredential(
                                                    it, currentPassword
                                                )
                                            }
                                            if (credential != null) {
                                                currentUser?.reauthenticate(credential)?.addOnSuccessListener {
                                                    currentUser?.updatePassword(newPassword)?.addOnSuccessListener {

                                                        loading = false

                                                        Toast.makeText(context, "Password Reset successful", Toast.LENGTH_SHORT).show()

                                                        FirebaseAuth.getInstance().signOut()
                                                        navController.navigate(ROUTE_LOGIN)

                                                    }?.addOnFailureListener {

                                                        loading = false

                                                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                                                        // Handle password update failure
                                                    }
                                                }?.addOnFailureListener { e ->
                                                    // Handle reauthentication failure
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    Text("Change Password")

                                }

                            }







                        }
                    }
                }
            }

        })






}

data class User(
    val userId: String = "",
    val school: String = "",
    val name: String = ""
)

fun saveUserDetails(user: User, param: (Any) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("users").document(user.userId)
        .set(user, SetOptions.merge())
        .addOnSuccessListener {

            progressDialog?.dismiss()
            // Success message or navigation
        }
        .addOnFailureListener {

            progressDialog?.dismiss()
            // Handle failure
        }
}
