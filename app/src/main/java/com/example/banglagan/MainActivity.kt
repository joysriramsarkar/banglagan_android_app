@file:OptIn(ExperimentalMaterial3Api::class) // Opt-in for ExperimentalMaterial3Api

package com.example.banglagan // আপনার প্যাকেজের নামের সাথে মিলিয়ে নিন

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.aspectRatio // এই ইম্পোর্টগুলো যোগ করুন
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home // Home আইকন ইম্পোর্ট
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.banglagan.data.Song // Song ডেটা ক্লাসের ইম্পোর্ট
import com.example.banglagan.data.SongDao
import com.example.banglagan.data.SongRepository
import com.example.banglagan.utils.toBanglaString
import com.example.banglagan.vi.song.SongUiState // SongUiState এর ইম্পোর্ট
import com.example.banglagan.vi.song.SongViewModel
import com.example.banglagan.vi.song.SongViewModelFactory
import com.example.banglagan.vi.theme.BanglaGanTheme // আপনার থিমের নামের সাথে মিলিয়ে নিন
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// নেভিগেশন রুটগুলো ডিফাইন করা হচ্ছে
object AppDestinations {
    const val HOME_ROUTE = "home" // হোম স্ক্রিনের রুট
    const val SONG_LIST_ROUTE = "song_list" // গানের তালিকা স্ক্রিনের রুট
    const val SONG_DETAIL_ROUTE = "song_detail" // গানের বিস্তারিত স্ক্রিনের রুট, songId সহ
    const val SEARCH_ROUTE = "search" // সার্চ স্ক্রিনের রুট
    const val FAVORITES_ROUTE = "favorites" // পছন্দের গানের তালিকা স্ক্রিনের রুট
    const val ARTIST_LIST_ROUTE = "artist_list"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BanglaGanTheme {
                // BanglaGanApplication থেকে repository অ্যাক্সেস করার চেষ্টা
                val application = LocalContext.current.applicationContext as? BanglaGanApplication
                if (application != null) {
                    val songViewModel: SongViewModel = viewModel(
                        factory = SongViewModelFactory(application.repository)
                    )
                    BanglaGanApp(songViewModel = songViewModel)
                } else {
                    // ফলব্যাক UI যদি Application ক্লাস কাস্ট করা না যায়
                    ErrorScreen("Application context could not be cast to BanglaGanApplication. Check your Application class and Manifest.")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanglaGanApp(songViewModel: SongViewModel) {
    val navController = rememberNavController() // NavController তৈরি করা হচ্ছে
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route // বর্তমান রুট

    Scaffold(
        topBar = {
            // শুধুমাত্র SONG_DETAIL_ROUTE ছাড়া অন্য রুটে সার্চ আইকন দেখাবে
            val showSearchIcon = currentRoute != "${AppDestinations.SONG_DETAIL_ROUTE}/{songId}"
            BanglaGanTopAppBar(
                // বর্তমান রুটের উপর ভিত্তি করে টপবারের টাইটেল সেট করা হচ্ছে
                title = when (currentRoute) {
                    AppDestinations.HOME_ROUTE -> "বাংলা গানের সংগ্রহ"
                    AppDestinations.SONG_LIST_ROUTE -> "সব গান"
                    AppDestinations.FAVORITES_ROUTE -> "পছন্দের গান"
                    AppDestinations.SEARCH_ROUTE -> "গান খুঁজুন"
                    AppDestinations.ARTIST_LIST_ROUTE -> "শিল্পীর তালিকা"
                    "${AppDestinations.SONG_DETAIL_ROUTE}/{songId}" -> "গানের বিবরণ" // এটি পরিবর্তন হতে পারে
                    else -> "বাংলা গান"
                },
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }, // Back Navigation
                showSearchIcon = showSearchIcon,
                onSearchClick = { navController.navigate(AppDestinations.SEARCH_ROUTE) }
            )
        },
        bottomBar = {
            BanglaGanBottomBar(navController = navController) // Bottom Navigation Bar
        }
    ) { innerPadding ->
        BanglaGanNavHost( // NavHost যেখানে বিভিন্ন স্ক্রিন লোড হবে
            navController = navController,
            songViewModel = songViewModel,
            modifier = Modifier.padding(innerPadding) // TopBar ও BottomBar এর জন্য প্যাডিং
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanglaGanTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    showSearchIcon: Boolean,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) { // যদি পেছনে যাওয়ার পথ থাকে, তাহলে Back আইকন দেখানো হবে
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "পেছনে যান"
                    )
                }
            }
        },
        actions = {
            if (showSearchIcon) { // যদি সার্চ আইকন দেখানোর পারমিশন থাকে
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "সার্চ করুন"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors( // টপবারের রঙ
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        )
    )
}

@Composable
fun BanglaGanBottomBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar {
        NavigationBarItem( // হোম আইটেম
            icon = { Icon(Icons.Filled.Home, contentDescription = "হোম") },
            label = { Text("হোম") },
            selected = currentRoute == AppDestinations.HOME_ROUTE,
            onClick = {
                navController.navigate(AppDestinations.HOME_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { // স্ট্যাকের শুরু পর্যন্ত পপ আপ
                        saveState = true
                    }
                    launchSingleTop = true // একই স্ক্রিন একাধিকবার স্ট্যাকে না আসার জন্য
                    restoreState = true // আগের স্টেট রিস্টোর করার জন্য
                }
            }
        )
        NavigationBarItem( // সব গান আইটেম
            icon = {
                // একটি সাধারণ আইকন ব্যবহার করা যেতে পারে, যেমন মিউজিক নোট
                BadgedBox(badge = {}) { // BadgedBox এখানে শুধু আইকন দেখানোর জন্য, পরে Badge যোগ করা যেতে পারে
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_slideshow), // একটি ডিফল্ট অ্যান্ড্রয়েড আইকন
                        contentDescription = "সব গান"
                    )
                }
            },
            label = { Text("সব গান") },
            selected = currentRoute == AppDestinations.SONG_LIST_ROUTE,
            onClick = {
                navController.navigate(AppDestinations.SONG_LIST_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem( // পছন্দের গান আইটেম
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "পছন্দের গান") },
            label = { Text("পছন্দের") },
            selected = currentRoute == AppDestinations.FAVORITES_ROUTE,
            onClick = {
                navController.navigate(AppDestinations.FAVORITES_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}


@Composable
fun BanglaGanNavHost(
    navController: NavHostController,
    songViewModel: SongViewModel,
    modifier: Modifier = Modifier
) {
    NavHost( // নেভিগেশন গ্রাফের হোস্ট
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE, // অ্যাপ শুরু হবে হোম স্ক্রিন থেকে
        modifier = modifier
    ) {
        composable(AppDestinations.HOME_ROUTE) { // হোম স্ক্রিনের জন্য Composable
            val songUiState by songViewModel.songUiState.collectAsState() // ViewModel থেকে UiState সংগ্রহ করুন
            HomeScreen(
                uiState = songUiState, // HomeScreen-এ UiState পাস করুন
                navController = navController
            )
        }
        composable(AppDestinations.SONG_LIST_ROUTE) { // গানের তালিকা স্ক্রিনের জন্য Composable
            val songUiState by songViewModel.songUiState.collectAsState()
            SongListScreen(
                uiState = songUiState,
                onSongClick = { songId -> // কোনো গানে ক্লিক করলে
                    navController.navigate("${AppDestinations.SONG_DETAIL_ROUTE}/$songId") // বিস্তারিত স্ক্রিনে যাবে
                },
                onFavoriteToggle = { song -> songViewModel.toggleFavoriteStatus(song) } // ফেভারিট টগল
            )
        }
        composable("${AppDestinations.SONG_DETAIL_ROUTE}/{songId}") { backStackEntry -> // গানের বিস্তারিত স্ক্রিন
            val songId = backStackEntry.arguments?.getString("songId")?.toIntOrNull()

            val songDetailFromVM by produceState<Song?>(initialValue = null, songId) {
                value = songId?.let { songViewModel.getSongById(it) }
            }

            if (songDetailFromVM != null) {
                SongDetailScreen(song = songDetailFromVM!!, songViewModel = songViewModel)
            } else if (songId != null) {
                // গান লোড হচ্ছে বা পাওয়া যায়নি
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (songDetailFromVM == null && songId != 0) CircularProgressIndicator() else Text("গান পাওয়া যায়নি।")
                }
            } else {
                Text("অবৈধ গানের আইডি।")
            }
        }
        composable(AppDestinations.FAVORITES_ROUTE) { // পছন্দের গানের তালিকা
            val favoriteSongsState by songViewModel.favoriteSongs.collectAsState()
            SongListScreen(
                uiState = SongUiState(allSongs = favoriteSongsState, isLoading = false), // ফেভারিট গান দিয়ে UiState তৈরি
                onSongClick = { songId ->
                    navController.navigate("${AppDestinations.SONG_DETAIL_ROUTE}/$songId")
                },
                onFavoriteToggle = { song -> songViewModel.toggleFavoriteStatus(song) }
            )
        }
        composable(AppDestinations.SEARCH_ROUTE) { // সার্চ স্ক্রিন
            val searchResultsState by songViewModel.searchResults.collectAsState()
            val currentSearchQueryState by songViewModel.currentSearchQuery.collectAsState()
            SearchScreen(
                searchQuery = currentSearchQueryState,
                onSearchQueryChange = { query -> songViewModel.searchSongs(query) },
                searchResults = searchResultsState,
                onSongClick = { songId ->
                    navController.navigate("${AppDestinations.SONG_DETAIL_ROUTE}/$songId")
                },
                onFavoriteToggle = { song -> songViewModel.toggleFavoriteStatus(song) }
            )
        }
        composable(AppDestinations.ARTIST_LIST_ROUTE) { // শিল্পীর তালিকা স্ক্রিন
            val artists by songViewModel.allArtists.collectAsState()
            ArtistListScreen(
                artists = artists,
                onArtistClick = { artistName ->
                    // আপাতত, আমরা শিল্পীর নামে সার্চ স্ক্রিনে পাঠাতে পারি
                    // পরে আমরা নির্দিষ্ট শিল্পীর গানের তালিকা দেখাব
                    navController.navigate("${AppDestinations.SEARCH_ROUTE}?query=${artistName.trim()}")
                }
            )
        }
    }
}

// --- নতুন স্ক্রিনগুলোর কঙ্কাল ---

@Composable
fun HomeScreen(
    uiState: SongUiState, // SongUiState গ্রহণ করুন
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // আইটেমগুলোর মধ্যে ফাঁকা জায়গা
    ) {
        Text("স্বাগতম!", style = MaterialTheme.typography.headlineMedium)

        // --- পরিসংখ্যান বিভাগ ---
        Text("এক নজরে ডেটাবেস", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        StatsGrid(uiState = uiState) // পরিসংখ্যান দেখানোর গ্রিড
        Spacer(modifier = Modifier.height(16.dp))

        // --- অন্যান্য বাটন ---
        Text("আরও দেখুন", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(AppDestinations.SONG_LIST_ROUTE) }) {
            Text("সব গান দেখুন", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
        }
        Card(modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(AppDestinations.FAVORITES_ROUTE) }) {
            Text("পছন্দের গান", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
        }
        // আপনি চাইলে এখানে আরও বাটন যোগ করতে পারেন
    }
}

@Composable
fun StatsGrid(uiState: SongUiState, modifier: Modifier = Modifier) {
    // LazyVerticalGrid ব্যবহার করে গ্রিড তৈরি করা হচ্ছে
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // দুটি কলাম থাকবে
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp), // গ্রিডের একটি নির্দিষ্ট উচ্চতা দিন বা প্রয়োজন অনুযায়ী পরিবর্তন করুন
        horizontalArrangement = Arrangement.spacedBy(16.dp), // কলামের মধ্যে ফাঁকা জায়গা
        verticalArrangement = Arrangement.spacedBy(16.dp), // সারির মধ্যে ফাঁকা জায়গা
        contentPadding = PaddingValues(8.dp)
    ) {
        item { StatsCard(label = "মোট গান", count = uiState.totalSongs) }
        item { StatsCard(label = "মোট শিল্পী", count = uiState.totalArtists) }
        item { StatsCard(label = "মোট গীতিকার", count = uiState.totalLyricists) }
        item { StatsCard(label = "মোট সুরকার", count = uiState.totalComposers) }
        // আপনি চাইলে এখানে আরও কার্ড যোগ করতে পারেন
    }
}

@Composable
fun StatsCard(label: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f), // কার্ডটিকে বর্গাকার করার চেষ্টা
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toBanglaString(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center // লেখা মাঝখানে দেখানোর জন্য
            )
        }
    }
}

@Composable
fun SongListScreen( // গানের তালিকা দেখানোর স্ক্রিন
    uiState: SongUiState,
    onSongClick: (Int) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading && uiState.allSongs.isEmpty()) { // যদি লোডিং চলে এবং কোনো গান না থাকে
        LoadingScreen(modifier = modifier.fillMaxSize())
    } else if (uiState.allSongs.isEmpty() && !uiState.isLoading) { // যদি লোডিং শেষ এবং তালিকা খালি
        EmptySongListScreen(modifier = modifier.fillMaxSize())
    } else { // গান থাকলে তালিকা দেখানো হবে
        SongList(
            songs = uiState.allSongs,
            onSongClick = onSongClick,
            onFavoriteToggle = onFavoriteToggle,
            modifier = modifier
        )
    }
}


@Composable
fun SongList( // গানের তালিকা
    songs: List<Song>,
    onSongClick: (Int) -> Unit, // এখন id পাস করবে গানে ক্লিক করলে
    onFavoriteToggle: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn( // স্ক্রলযোগ্য তালিকা
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // কন্টেন্টের জন্য প্যাডিং
        verticalArrangement = Arrangement.spacedBy(8.dp) // আইটেমগুলোর মধ্যে ফাঁকা জায়গা
    ) {
        items(songs, key = { song -> song.id }) { song -> // প্রতিটি গানের জন্য একটি আইটেম
            SongItem(
                song = song,
                onFavoriteToggle = { onFavoriteToggle(song) },
                onClick = { onSongClick(song.id) } // গানে ক্লিক করলে কল হবে
            )
        }
    }
}

@Composable
fun SongItem( // একটি গানের আইটেম
    song: Song,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit, // onClick প্যারামিটার যোগ করা হয়েছে
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Card-কে ক্লিকযোগ্য করা হয়েছে
            .padding(vertical = 4.dp), // Card এর উপরে ও নিচে প্যাডিং
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Card এর ছায়া
    ) {
        Row( // ফেভারিট আইকন পাশে আনার জন্য Row ব্যবহার করা হয়েছে
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically, // উল্লম্বভাবে মাঝখানে
            horizontalArrangement = Arrangement.SpaceBetween // আইটেমগুলোর মধ্যে ফাঁকা জায়গা
        ) {
            Column(modifier = Modifier.weight(1f)) { // লেখাগুলো যেন যথেষ্ট জায়গা পায়
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1, // এক লাইনে দেখাবে
                    overflow = TextOverflow.Ellipsis // লেখা বেশি হলে ... দেখাবে
                )
                Text(
                    text = "শিল্পী: ${song.artistName ?: "অজানা"}", // শিল্পীর নাম না থাকলে "অজানা" দেখাবে
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!song.era.isNullOrEmpty()) { // যদি যুগ থাকে
                    Text(
                        text = "যুগ: ${song.era}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (!song.genre.isNullOrEmpty()) { // যদি গানের ধরণ থাকে
                    Text(
                        text = "ধরণ: ${song.genre}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(onClick = onFavoriteToggle) { // ফেভারিট বাটন
                Icon(
                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (song.isFavorite) "পছন্দ থেকে সরান" else "পছন্দ করুন",
                    tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SongDetailScreen(song: Song, songViewModel: SongViewModel, modifier: Modifier = Modifier) {
    // গানের বিস্তারিত তথ্য এখানে দেখানো হবে
    LazyColumn( // লিরিক্স লম্বা হতে পারে, তাই LazyColumn
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start // লেখাগুলো বাম দিক থেকে শুরু হবে
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(song.title, style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = { songViewModel.toggleFavoriteStatus(song) }) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "ফেভারিট",
                        tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Text("শিল্পী: ${song.artistName ?: "অজানা"}", style = MaterialTheme.typography.titleMedium) }
        if (!song.albumName.isNullOrEmpty()) {
            item { Text("অ্যালবাম: ${song.albumName}", style = MaterialTheme.typography.bodyLarge) }
        }
        if (!song.lyricist.isNullOrEmpty()) {
            item { Text("গীতিকার: ${song.lyricist}", style = MaterialTheme.typography.bodyLarge) }
        }
        if (!song.composer.isNullOrEmpty()) {
            item { Text("সুরকার: ${song.composer}", style = MaterialTheme.typography.bodyLarge) }
        }
        if (!song.era.isNullOrEmpty()) {
            item { Text("যুগ: ${song.era}", style = MaterialTheme.typography.bodyLarge) }
        }
        if (!song.genre.isNullOrEmpty()) {
            item { Text("ধরণ: ${song.genre}", style = MaterialTheme.typography.bodyLarge) }
        }
        if (song.releaseYear != null && song.releaseYear > 0) {
            item { Text("প্রকাশকাল: ${song.releaseYear?.toBanglaString() ?: ""}", style = MaterialTheme.typography.bodyLarge) }
        }

        if (!song.lyrics.isNullOrEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("গানের কথা:", style = MaterialTheme.typography.titleMedium)
                Text(song.lyrics, style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (!song.notes.isNullOrEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("বিশেষ তথ্য:", style = MaterialTheme.typography.titleMedium)
                Text(song.notes, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun SearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<Song>,
    onSongClick: (Int) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("গান, শিল্পী, অ্যালবাম...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
            Text("'$searchQuery' এর জন্য কোনো ফলাফল পাওয়া যায়নি।")
        } else {
            SongList( // গানের তালিকা Composable পুনরায় ব্যবহার করা হচ্ছে
                songs = searchResults,
                onSongClick = onSongClick,
                onFavoriteToggle = onFavoriteToggle
            )
        }
    }
}

// --- Helper Composable ---
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) { // লোডিং হচ্ছে দেখানোর জন্য
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator() // লোডিং অ্যানিমেশন
        Text(
            text = "গান লোড হচ্ছে...",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun EmptySongListScreen(modifier: Modifier = Modifier) { // গানের তালিকা খালি থাকলে
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("কোনো গান পাওয়া যায়নি।", style = MaterialTheme.typography.headlineSmall)
        Text("কিছু গান যোগ করুন অথবা ডাটাবেস সিঙ্ক করুন!", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top=4.dp))
    }
}

@Composable
fun ErrorScreen(message: String, modifier: Modifier = Modifier) { // কোনো সমস্যা হলে
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("একটি সমস্যা হয়েছে:", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
        Text(message, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
    }
}

// --- প্রিভিউ ফাংশন ---

// SongDao-এর সব মেথডসহ FakeSongDao
private class FakeSongDao : SongDao {
    override fun getAllSongs(): Flow<List<Song>> = flowOf(listOf(
        Song(id = 1, title = "আমার সোনার বাংলা", artistName = "রবীন্দ্রনাথ ঠাকুর", era = "আধুনিক", genre = "রবীন্দ্রসঙ্গীত", isFavorite = true),
        Song(id = 2, title = "কফি হাউসের সেই আড্ডাটা", artistName = "মান্না দে", genre = "আধুনিক বাংলা")
    ))

    override fun getFavoriteSongs(): Flow<List<Song>> = flowOf(listOf(
        Song(id = 1, title = "আমার সোনার বাংলা", artistName = "রবীন্দ্রনাথ ঠাকুর", era = "আধুনিক", genre = "রবীন্দ্রসঙ্গীত", isFavorite = true)
    ))

    override fun getSongById(songId: Int): Flow<Song?> = flowOf(
        Song(
            id = 1,
            title = "কফি হাউসের সেই আড্ডাটা",
            artistName = "মান্না দে",
            albumName = "Single",
            lyricist = "গৌরীপ্রসন্ন মজুমদার",
            composer = "সুপর্ণকান্তি ঘোষ",
            era = "আধুনিক",
            genre = "আধুনিক বাংলা",
            releaseYear = 1983,
            lyrics = "কফি হাউসের সেই আড্ডাটা আজ আর নেই,\nকোথায় হারিয়ে গেল সোনালী বিকেলগুলো সেই...",
            isFavorite = true,
            notes = "একটি কালজয়ী জনপ্রিয় বাংলা গান。"
        )
    )

    override suspend fun insertSong(song: Song) {}
    override suspend fun insertAllSongs(songs: List<Song>) {}
    override suspend fun updateSong(song: Song) {}
    override suspend fun deleteSong(song: Song) {}
    override fun searchSongs(query: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongsByArtist(artistName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongsByGenre(genreName: String): Flow<List<Song>> = flowOf(emptyList())

    // --- এই নতুন ফাংশনগুলো যোগ করুন ---
    override fun getSongCount(): Flow<Int> = flowOf(869) // ডামি সংখ্যা
    override fun getArtistCount(): Flow<Int> = flowOf(29)  // ডামি সংখ্যা
    override fun getLyricistCount(): Flow<Int> = flowOf(990)// ডামি সংখ্যা
    override fun getComposerCount(): Flow<Int> = flowOf(67) // ডামি সংখ্যা
    // --- এই নতুন ফাংশনটি যোগ করুন ---
    override fun getAllArtists(): Flow<List<String>> = flowOf(listOf("রবীন্দ্রনাথ ঠাকুর", "মান্না দে", "কাজী নজরুল ইসলাম"))
    // --- ---
    // --- ---
}


@Preview(showBackground = true, name = "Song Item Preview")
@Composable
fun SongItemPreview() {
    BanglaGanTheme {
        SongItem(
            song = Song(id = 1, title = "আমার সোনার বাংলা", artistName = "রবীন্দ্রনাথ ঠাকুর", era = "আধুনিক", genre = "রবীন্দ্রসঙ্গীত", isFavorite = true),
            onFavoriteToggle = {},
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun HomeScreenPreview() {
    BanglaGanTheme {
        // প্রিভিউয়ের জন্য একটি ডামি UiState তৈরি করুন
        val previewUiState = SongUiState(
            totalSongs = 869,
            totalArtists = 29,
            totalLyricists = 990,
            totalComposers = 67
        )
        HomeScreen(
            uiState = previewUiState, // ডামি ডেটা পাস করুন
            navController = rememberNavController() // প্রিভিউয়ের জন্য একটি ডামি NavController
        )
    }
}

@Preview(showBackground = true, name = "Song Detail Preview")
@Composable
fun SongDetailScreenPreview() {
    BanglaGanTheme {
        val previewSong = Song(
            id = 1,
            title = "কফি হাউসের সেই আড্ডাটা",
            artistName = "মান্না দে",
            albumName = "Single",
            lyricist = "গৌরীপ্রসন্ন মজুমদার",
            composer = "সুপর্ণকান্তি ঘোষ",
            era = "আধুনিক",
            genre = "আধুনিক বাংলা",
            releaseYear = 1983,
            lyrics = "কফি হাউসের সেই আড্ডাটা আজ আর নেই,\nকোথায় হারিয়ে গেল সোনালী বিকেলগুলো সেই...",
            isFavorite = true,
            notes = "একটি কালজয়ী জনপ্রিয় বাংলা গান।"
        )

        // ডামি ViewModel তৈরি (সরাসরি ক্লাস ব্যবহার করে)
        val dummyViewModel = SongViewModel(SongRepository(FakeSongDao()))

        SongDetailScreen(song = previewSong, songViewModel = dummyViewModel)
    }
}

@Composable
fun ArtistListScreen(
    artists: List<String>,
    onArtistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (artists.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("কোনো শিল্পী পাওয়া যায়নি।")
        }
    } else {
        LazyColumn(modifier = modifier.padding(16.dp)) {
            items(artists) { artist ->
                Text(
                    text = artist,
                    style = MaterialTheme.typography.titleMedium, // একটু বড় ফন্ট
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onArtistClick(artist) }
                        .padding(vertical = 16.dp) // প্রতিটি আইটেমের মধ্যে ফাঁকা জায়গা
                )
                Divider() // আইটেমগুলোর মধ্যে বিভাজক রেখা
            }
        }
    }
}