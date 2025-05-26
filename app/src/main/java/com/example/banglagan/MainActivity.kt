@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.banglagan

import android.os.Bundle
import android.util.Log // <-- Log ব্যবহারের জন্য ইম্পোর্ট
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.aspectRatio
// LazyVerticalGrid আর প্রয়োজন নেই যদি আমরা Column/Row ব্যবহার করি গ্রিডের জন্য
// import androidx.compose.foundation.lazy.grid.GridCells
// import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.banglagan.data.Song
import com.example.banglagan.data.SongDao
import com.example.banglagan.data.SongRepository
import com.example.banglagan.utils.toBanglaString
import com.example.banglagan.vi.song.SongUiState
import com.example.banglagan.vi.song.SongViewModel
import com.example.banglagan.vi.song.SongViewModelFactory
import com.example.banglagan.vi.theme.BanglaGanTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val SONG_LIST_ROUTE = "song_list"

    // SONG_DETAIL_ROUTE এর জন্য নতুন এবং সঠিক সংজ্ঞা
    const val SONG_DETAIL_ROUTE_BASE = "song_detail"
    const val SONG_DETAIL_ARG_SONG_ID = "songId"
    const val SONG_DETAIL_ROUTE_PATTERN = "$SONG_DETAIL_ROUTE_BASE/{$SONG_DETAIL_ARG_SONG_ID}"
    fun songDetailRoute(songId: Int) = "$SONG_DETAIL_ROUTE_BASE/$songId"

    const val SEARCH_ROUTE = "search"
    const val FAVORITES_ROUTE = "favorites"
    const val ARTIST_LIST_ROUTE = "artist_list"
    const val ARTIST_SONGS_ROUTE_BASE = "artist_songs"
    const val ARTIST_SONGS_ARG_ARTIST_NAME = "artistName"
    const val ARTIST_SONGS_ROUTE_PATTERN = "$ARTIST_SONGS_ROUTE_BASE/{$ARTIST_SONGS_ARG_ARTIST_NAME}"
    fun artistSongsRoute(artistName: String) = "$ARTIST_SONGS_ROUTE_BASE/$artistName"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BanglaGanTheme {
                val application = LocalContext.current.applicationContext as? BanglaGanApplication
                if (application != null) {
                    val songViewModel: SongViewModel = viewModel(
                        factory = SongViewModelFactory(application.repository)
                    )
                    BanglaGanApp(songViewModel = songViewModel)
                } else {
                    // ErrorScreen Composable ফাংশনটি নিচে ডিফাইন করা আছে
                    ErrorScreen("Application context is not BanglaGanApplication. Check AndroidManifest.xml and Application class.")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanglaGanApp(songViewModel: SongViewModel) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        topBar = {
            val showSearchIcon = !(currentRoute?.startsWith(AppDestinations.SONG_DETAIL_ROUTE_BASE) == true)

            BanglaGanTopAppBar(
                title = when {
                    currentRoute == AppDestinations.HOME_ROUTE -> "বাংলা গানের সংগ্রহ"
                    currentRoute == AppDestinations.SONG_LIST_ROUTE -> "সব গান"
                    currentRoute == AppDestinations.FAVORITES_ROUTE -> "পছন্দের গান"
                    currentRoute == AppDestinations.SEARCH_ROUTE -> "গান খুঁজুন"
                    currentRoute == AppDestinations.ARTIST_LIST_ROUTE -> "শিল্পীর তালিকা"
                    currentRoute?.startsWith(AppDestinations.ARTIST_SONGS_ROUTE_BASE) == true -> {
                        val artistNameFromRoute = navController.currentBackStackEntry?.arguments?.getString(AppDestinations.ARTIST_SONGS_ARG_ARTIST_NAME)
                        if (!artistNameFromRoute.isNullOrEmpty()) {
                            "${artistNameFromRoute}-এর গান"
                        } else {
                            "শিল্পীর গান"
                        }
                    }
                    currentRoute?.startsWith(AppDestinations.SONG_DETAIL_ROUTE_BASE) == true -> "গানের বিবরণ"
                    else -> "বাংলা গান"
                },
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                showSearchIcon = showSearchIcon,
                onSearchClick = { navController.navigate(AppDestinations.SEARCH_ROUTE) }
            )
        },
        bottomBar = {
            BanglaGanBottomBar(navController = navController)
        }
    ) { innerPadding ->
        BanglaGanNavHost(
            navController = navController,
            songViewModel = songViewModel,
            modifier = Modifier.padding(innerPadding)
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
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) }, // maxLines ও overflow যোগ করা হলো
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "পেছনে যান")
                }
            }
        },
        actions = {
            if (showSearchIcon) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Filled.Search, contentDescription = "সার্চ করুন")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        )
    )
}

@Composable
fun BanglaGanBottomBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "হোম") },
            label = { Text("হোম") },
            selected = currentRoute == AppDestinations.HOME_ROUTE,
            onClick = {
                navController.navigate(AppDestinations.HOME_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { BadgedBox(badge = {}) { Icon(painterResource(id = android.R.drawable.ic_menu_slideshow), contentDescription = "সব গান") } },
            label = { Text("সব গান") },
            selected = currentRoute == AppDestinations.SONG_LIST_ROUTE,
            onClick = {
                navController.navigate(AppDestinations.SONG_LIST_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "পছন্দের গান") },
            label = { Text("পছন্দের") },
            selected = currentRoute == AppDestinations.FAVORITES_ROUTE,
            onClick = {
                navController.navigate(AppDestinations.FAVORITES_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
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
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE,
        modifier = modifier
    ) {
        composable(AppDestinations.HOME_ROUTE) {
            val songUiState by songViewModel.songUiState.collectAsState()
            HomeScreen(uiState = songUiState, navController = navController)
        }
        composable(AppDestinations.SONG_LIST_ROUTE) {
            val songUiState by songViewModel.songUiState.collectAsState()
            SongListScreen(
                uiState = songUiState,
                onSongClick = { songId -> navController.navigate(AppDestinations.songDetailRoute(songId)) },
                onFavoriteToggle = { song -> songViewModel.toggleFavoriteStatus(song) }
            )
        }
        composable(AppDestinations.SONG_DETAIL_ROUTE_PATTERN) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString(AppDestinations.SONG_DETAIL_ARG_SONG_ID)?.toIntOrNull()
            val songDetailFromVM by produceState<Song?>(initialValue = null, key1 = songId) {
                value = songId?.let {
                    Log.d("BanglaGanNavHost", "Loading song detail for ID: $it")
                    val song = songViewModel.getSongById(it)
                    Log.d("BanglaGanNavHost", "Song detail loaded for ID $it: ${song?.title}")
                    song
                }
            }

            if (songDetailFromVM != null) {
                SongDetailScreen(
                    song = songDetailFromVM!!,
                    songViewModel = songViewModel,
                    onArtistNameClick = { artistName ->
                        navController.navigate(AppDestinations.artistSongsRoute(artistName.trim()))
                    }
                )
            } else if (songId != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Text("অবৈধ গানের আইডি।")
            }
        }
        composable(AppDestinations.FAVORITES_ROUTE) {
            val favoriteSongsState by songViewModel.favoriteSongs.collectAsState()
            SongListScreen(
                uiState = SongUiState(allSongs = favoriteSongsState, isLoading = false),
                onSongClick = { songId -> navController.navigate(AppDestinations.songDetailRoute(songId)) },
                onFavoriteToggle = { song -> songViewModel.toggleFavoriteStatus(song) }
            )
        }
        composable(AppDestinations.SEARCH_ROUTE) {
            val searchResultsState by songViewModel.searchResults.collectAsState()
            val currentSearchQueryState by songViewModel.currentSearchQuery.collectAsState()
            SearchScreen(
                searchQuery = currentSearchQueryState,
                onSearchQueryChange = { query -> songViewModel.searchSongs(query) },
                searchResults = searchResultsState,
                onSongClick = { songId -> navController.navigate(AppDestinations.songDetailRoute(songId)) },
                onFavoriteToggle = { song -> songViewModel.toggleFavoriteStatus(song) }
            )
        }
        composable(AppDestinations.ARTIST_LIST_ROUTE) {
            val artists by songViewModel.allArtists.collectAsState()
            ArtistListScreen(
                artists = artists,
                onArtistClick = { artistName ->
                    navController.navigate(AppDestinations.artistSongsRoute(artistName.trim()))
                }
            )
        }
        composable(AppDestinations.ARTIST_SONGS_ROUTE_PATTERN) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString(AppDestinations.ARTIST_SONGS_ARG_ARTIST_NAME) ?: ""
            val songsByArtist by songViewModel.getSongsByArtist(artistName).collectAsState(initial = emptyList())

            SongListScreen(
                uiState = SongUiState(allSongs = songsByArtist, isLoading = false),
                onSongClick = { songId -> navController.navigate(AppDestinations.songDetailRoute(songId)) },
                onFavoriteToggle = { song -> songViewModel.toggleFavoriteStatus(song) }
            )
        }
    }
}

@Composable
fun HomeScreen(
    uiState: SongUiState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // এটি থাকবে
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("স্বাগতম!", style = MaterialTheme.typography.headlineMedium)

        Text("এক নজরে ডেটাবেস", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        StatsSection(uiState = uiState, modifier = Modifier.fillMaxWidth()) // নতুন ফাংশন
        Spacer(modifier = Modifier.height(16.dp))

        Text("আরও অন্বেষণ করুন", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        ExploreButtonsSection(navController = navController, modifier = Modifier.fillMaxWidth()) // নতুন ফাংশন

        Spacer(modifier = Modifier.height(16.dp))

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
    }
}

@Composable
fun StatsSection(uiState: SongUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                StatsCard(label = "মোট গান", count = uiState.totalSongs)
            }
            Box(modifier = Modifier.weight(1f)) {
                StatsCard(label = "মোট শিল্পী", count = uiState.totalArtists)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                StatsCard(label = "মোট গীতিকার", count = uiState.totalLyricists)
            }
            Box(modifier = Modifier.weight(1f)) {
                StatsCard(label = "মোট সুরকার", count = uiState.totalComposers)
            }
        }
    }
}

@Composable
fun ExploreButtonsSection(navController: NavHostController, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ExploreButton(
                    label = "গান",
                    onClick = { navController.navigate(AppDestinations.SEARCH_ROUTE) }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ExploreButton(
                    label = "শিল্পী",
                    onClick = {
                        Log.d("BanglaGanApp", "শিল্পী খুঁজুন button clicked from HomeScreen")
                        navController.navigate(AppDestinations.ARTIST_LIST_ROUTE)
                    }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ExploreButton(
                    label = "গীতিকার",
                    onClick = { navController.navigate(AppDestinations.SEARCH_ROUTE) } // TODO: গীতিকার তালিকা পাতায় যাবে
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ExploreButton(
                    label = "সুরকার",
                    onClick = { navController.navigate(AppDestinations.SEARCH_ROUTE) } // TODO: সুরকার তালিকা পাতায় যাবে
                )
            }
        }
    }
}

@Composable
fun ExploreButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun StatsCard(label: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth() // এটি ওজন (weight) অনুযায়ী জায়গা নেবে
            .aspectRatio(1.5f), // উচ্চতা/প্রস্থ অনুপাত, প্রয়োজন অনুযায়ী পরিবর্তন করুন
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SongListScreen(
    uiState: SongUiState,
    onSongClick: (Int) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading && uiState.allSongs.isEmpty()) {
        LoadingScreen(modifier = modifier.fillMaxSize())
    } else if (uiState.allSongs.isEmpty() && !uiState.isLoading) {
        EmptySongListScreen(modifier = modifier.fillMaxSize())
    } else {
        SongList(
            songs = uiState.allSongs,
            onSongClick = onSongClick,
            onFavoriteToggle = onFavoriteToggle,
            modifier = modifier
        )
    }
}


@Composable
fun SongList(
    songs: List<Song>,
    onSongClick: (Int) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(songs, key = { song -> song.id }) { song ->
            SongItem(
                song = song,
                onFavoriteToggle = { onFavoriteToggle(song) },
                onClick = { onSongClick(song.id) }
            )
        }
    }
}

@Composable
fun SongItem(
    song: Song,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "শিল্পী: ${song.artistName ?: "অজানা"}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!song.era.isNullOrEmpty()) {
                    Text(
                        text = "যুগ: ${song.era}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (!song.genre.isNullOrEmpty()) {
                    Text(
                        text = "ধরণ: ${song.genre}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(onClick = onFavoriteToggle) {
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
fun SongDetailScreen(
    song: Song,
    songViewModel: SongViewModel,
    onArtistNameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
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

        item {
            val artistName = song.artistName ?: "অজানা"
            Row(
                modifier = Modifier.clickable(enabled = song.artistName != null) {
                    if (song.artistName != null) {
                        Log.d("BanglaGanApp", "Artist name '${song.artistName}' clicked from SongDetailScreen")
                        onArtistNameClick(song.artistName)
                    }
                }
            ) {
                Text("শিল্পী: ", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (song.artistName != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                )
            }
        }

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
fun ArtistListScreen(
    artists: List<String>,
    onArtistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("BanglaGanApp", "ArtistListScreen displayed with ${artists.size} artists.")
    if (artists.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("কোনো শিল্পী পাওয়া যায়নি।")
        }
    } else {
        LazyColumn(modifier = modifier.padding(16.dp)) {
            items(artists) { artist ->
                Text(
                    text = artist,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Log.d("BanglaGanApp", "Artist '$artist' clicked in ArtistListScreen")
                            onArtistClick(artist)
                        }
                        .padding(vertical = 16.dp)
                )
                Divider()
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
            SongList(
                songs = searchResults,
                onSongClick = onSongClick,
                onFavoriteToggle = onFavoriteToggle
            )
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = "গান লোড হচ্ছে...",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun EmptySongListScreen(modifier: Modifier = Modifier) {
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
fun ErrorScreen(message: String, modifier: Modifier = Modifier) {
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
            id = songId, // ডামি আইডির সাথে মিল রাখার জন্য
            title = "কফি হাউসের সেই আড্ডাটা ($songId)",
            artistName = "মান্না দে",
            albumName = "Single",
            lyricist = "গৌরীপ্রসন্ন মজুমদার",
            composer = "সুপর্ণকান্তি ঘোষ",
            era = "আধুনিক",
            genre = "আধুনিক বাংলা",
            releaseYear = 1983,
            lyrics = "কফি হাউসের সেই আড্ডাটা আজ আর নেই...",
            isFavorite = true,
            notes = "একটি কালজয়ী জনপ্রিয় বাংলা গান।"
        )
    )

    override suspend fun insertSong(song: Song) {}
    override suspend fun insertAllSongs(songs: List<Song>) {}
    override suspend fun updateSong(song: Song) {}
    override suspend fun deleteSong(song: Song) {}
    override fun searchSongs(query: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongsByArtist(artistName: String): Flow<List<Song>> = flowOf(
        listOf(
            Song(id = 3, title = "গান ১ শিল্পী $artistName", artistName = artistName),
            Song(id = 4, title = "গান ২ শিল্পী $artistName", artistName = artistName)
        )
    )
    override fun getSongsByGenre(genreName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongCount(): Flow<Int> = flowOf(869)
    override fun getArtistCount(): Flow<Int> = flowOf(29)
    override fun getLyricistCount(): Flow<Int> = flowOf(990)
    override fun getComposerCount(): Flow<Int> = flowOf(67)
    override fun getAllArtists(): Flow<List<String>> = flowOf(listOf("রবীন্দ্রনাথ ঠাকুর", "মান্না দে", "কাজী নজরুল ইসলাম"))

    // FakeSongDao তে বাকি ফাংশনগুলো যোগ করতে হবে
    override fun getSongsByLyricist(lyricistName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongsByComposer(composerName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongsByEra(eraName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getAllLyricists(): Flow<List<String>> = flowOf(listOf("গীতিকার ১", "গীতিকার ২"))
    override fun getAllComposers(): Flow<List<String>> = flowOf(listOf("সুরকার ১", "সুরকার ২"))
    override fun getAllGenres(): Flow<List<String>> = flowOf(listOf("রবীন্দ্রসঙ্গীত", "নজরুলগীতি", "আধুনিক"))
    override fun getAllEras(): Flow<List<String>> = flowOf(listOf("প্রাচীন", "মধ্যযুগ", "আধুনিক"))
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
        val previewUiState = SongUiState(
            totalSongs = 869,
            totalArtists = 29,
            totalLyricists = 990,
            totalComposers = 67
        )
        HomeScreen(
            uiState = previewUiState,
            navController = rememberNavController()
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
            notes = "একটি কালজয়ী জনপ্রিয় বাংলা গান。"
        )
        val dummyViewModel = SongViewModel(SongRepository(FakeSongDao()))
        SongDetailScreen(
            song = previewSong,
            songViewModel = dummyViewModel,
            onArtistNameClick = {}
        )
    }
}