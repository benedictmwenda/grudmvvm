package net.ezra


import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import net.ezra.navigation.AppNavHost

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {



        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        FirebaseFirestore.getInstance()
        setContent {
            AppNavHost()
        }
    }





}