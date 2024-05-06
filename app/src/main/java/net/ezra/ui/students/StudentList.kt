package net.ezra.ui.students

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

import androidx.lifecycle.ViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import net.ezra.R
import net.ezra.navigation.ROUTE_HOME
import net.ezra.navigation.ROUTE_VIEW_STUDENTS


data class Student(

    val imageUrl: String? = "",
    val studentName: String? = "",
    val studentClass: String? = "",
    val phone: String? = "",
    val location: String? = ""


)


class FirestoreViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val itemsCollection = firestore.collection("Students")

    private val _items = MutableLiveData<List<Student>>()
    val items: LiveData<List<Student>> = _items

    init {
        fetchItems()
    }

    fun fetchItems() {
        itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreViewModel", "Error fetching students", error)
                return@addSnapshotListener
            }

            val itemList = mutableListOf<Student>()
            snapshot?.documents?.forEach { document ->
                val item = document.toObject(Student::class.java)
                item?.let {
                    itemList.add(it)
                }
            }
            _items.value = itemList
        }
    }
}


@Composable
fun StudentList(items: List<Student>) {

    Column {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(10.dp),
            horizontalArrangement = Arrangement.Center
        ) {

            items.forEach { item ->
                item {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){

                           SubcomposeAsyncImage(
                               model = ImageRequest.Builder(LocalContext.current)
                                   .data(item.imageUrl)
                                   .crossfade(true)
                                   .build(),
                               loading = {
                                   CircularProgressIndicator()
                               },
                               contentDescription = item.studentName,
                               modifier = Modifier
                                   .clip(RoundedCornerShape(10))
                                   .size(150.dp)

                           )

                        item.studentName?.let { Text(text = it) }
                        item.phone?.let { Text(text = it) }



                    }

                }
            }


        }
    }
}




@SuppressLint("ResourceAsColor")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Students(navController: NavHostController, viewModel: FirestoreViewModel) {
    val items by viewModel.items.observeAsState(initial = emptyList())

    // Fetch items when the composable is first created
    LaunchedEffect(viewModel) {
        viewModel.fetchItems()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Students")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_VIEW_STUDENTS) { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "backIcon",
                            tint = Color.White
                        )
                    }
                },


                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = topAppBarColors(
        containerColor = Color(0xff0FB06A),


        titleContentColor = Color.White,
    ),
            )
        },

        content = {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(Color(0xff9AEDC9)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                StudentList(items)


            }
        },


        )
}