// MainActivity.kt

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.banglagan

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.banglagan.data.Song
import com.example.banglagan.data.SongDao
import com.example.banglagan.data.SongRepository
import com.example.banglagan.vi.screens.GenericListScreen
import com.example.banglagan.vi.screens.AddSongScreen
import com.example.banglagan.vi.screens.StatisticsScreen
import com.example.banglagan.utils.toBanglaString
import com.example.banglagan.vi.song.SongUiState
import com.example.banglagan.vi.song.SongViewModel
import com.example.banglagan.vi.song.SongViewModelFactory
import com.example.banglagan.vi.theme.BanglaGanTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val SONG_LIST_ROUTE = "song_list"
    const val ADD_SONG_ROUTE = "add_song"
    const val STATISTICS_ROUTE = "statistics"

    const val SONG_DETAIL_ROUTE_BASE = "song_detail"
    const val SONG_DETAIL_ARG_SONG_ID = "songId"
    const val SONG_DETAIL_ROUTE_PATTERN = "$SONG_DETAIL_ROUTE_BASE/{$SONG_DETAIL_ARG_SONG_ID}"
    fun songDetailRoute(songId: Int) = "$SONG_DETAIL_ROUTE_BASE/$songId"

    const val SEARCH_ROUTE_BASE = "search"
    const val SEARCH_ARG_QUERY = "query"
    const val SEARCH_ARG_TYPE = "type"
    const val SEARCH_ROUTE_PATTERN = "$SEARCH_ROUTE_BASE?$SEARCH_ARG_QUERY={$SEARCH_ARG_QUERY}&$SEARCH_ARG_TYPE={$SEARCH_ARG_TYPE}"
    fun searchRoute(query: String = "", type: String = ""): String {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val encodedType = URLEncoder.encode(type, StandardCharsets.UTF_8.toString())
        return "$SEARCH_ROUTE_BASE?$SEARCH_ARG_QUERY=$encodedQuery&$SEARCH_ARG_TYPE=$encodedType"
    }

    const val FAVORITES_ROUTE = "favorites"

    const val ARTIST_LIST_ROUTE = "artist_list"
    const val LYRICIST_LIST_ROUTE = "lyricist_list"
    const val COMPOSER_LIST_ROUTE = "composer_list"
    const val ERA_LIST_ROUTE = "era_list"
    const val GENRE_LIST_ROUTE = "genre_list"

    const val SONGS_BY_CATEGORY_ROUTE_BASE = "songs_by_category"
    const val SONGS_BY_CATEGORY_ARG_TYPE = "categoryType"
    const val SONGS_BY_CATEGORY_ARG_NAME = "categoryName"
    const val SONGS_BY_CATEGORY_ROUTE_PATTERN = "$SONGS_BY_CATEGORY_ROUTE_BASE/{$SONGS_BY_CATEGORY_ARG_TYPE}/{$SONGS_BY_CATEGORY_ARG_NAME}"
    fun songsByCategoryRoute(categoryType: String, categoryName: String): String {
        val encodedCategoryName = URLEncoder.encode(categoryName, StandardCharsets.UTF_8.toString())
        return "$SONGS_BY_CATEGORY_ROUTE_BASE/$categoryType/$encodedCategoryName"
    }
}

data class CategoryHomeItem(val label: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun ActualHomeScreen(
    onNavigateToSearch: (String, String) -> Unit,
    onNavigateToArtistList: () -> Unit,
    onNavigateToLyricistList: () -> Unit,
    onNavigateToComposerList: () -> Unit,
    onNavigateToEraList: () -> Unit,
    onNavigateToGenreList: () -> Unit,
    onNavigateToAddSong: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    songViewModel: SongViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val uiState by songViewModel.songUiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                    )
                )
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "স্বাগতম!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "বাংলা গানের ভুবনে আপনাকে স্বাগতম",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Text(
            "এক নজরে ডেটাবেস",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )
        StatsSection(uiState = uiState, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "ক্যাটাগরি",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )

        val categoryItems = listOf(
            CategoryHomeItem("গান", Icons.Filled.MusicNote, onClick = { onNavigateToSearch("", "song") }),
            CategoryHomeItem("শিল্পী", Icons.Filled.Person, onClick = { onNavigateToArtistList() }),
            CategoryHomeItem("গীতিকার", Icons.Filled.EditNote, onClick = { onNavigateToLyricistList() }),
            CategoryHomeItem("সুরকার", Icons.Filled.LibraryMusic, onClick = { onNavigateToComposerList() }),
            CategoryHomeItem("যুগ", Icons.Filled.AccessTime, onClick = { onNavigateToEraList() }),
            CategoryHomeItem("ধরণ", Icons.Filled.Category, onClick = { onNavigateToGenreList() })
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(min = 200.dp, max = 400.dp)
        ) {
            items(items = categoryItems, key = { it.label }) { itemData ->
                CategoryHomeButton(label = itemData.label, icon = itemData.icon, onClick = itemData.onClick)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "শর্টকাট",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                label = "সব গান",
                icon = Icons.Filled.List,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(AppDestinations.SONG_LIST_ROUTE) }
            )
            QuickActionCard(
                label = "পছন্দের গান",
                icon = Icons.Filled.Favorite,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(AppDestinations.FAVORITES_ROUTE) }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                label = "নতুন গান",
                icon = Icons.Filled.Add,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToAddSong() }
            )
            QuickActionCard(
                label = "পরিসংখ্যান",
                icon = Icons.Filled.Analytics,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToStatistics() }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun CategoryHomeButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
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
                    ErrorScreen("Application context is not BanglaGanApplication.")
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
            val title = when {
                currentRoute == AppDestinations.HOME_ROUTE -> "বাংলা গানের সংগ্রহ"
                currentRoute == AppDestinations.SONG_LIST_ROUTE -> "সব গান"
                currentRoute == AppDestinations.FAVORITES_ROUTE -> "পছন্দের গান"
                currentRoute == AppDestinations.ADD_SONG_ROUTE -> "নতুন গান যোগ করুন"
                currentRoute == AppDestinations.STATISTICS_ROUTE -> "পরিসংখ্যান"
                currentRoute == AppDestinations.SEARCH_ROUTE_PATTERN -> "গান খুঁজুন"
                currentRoute == AppDestinations.ARTIST_LIST_ROUTE -> "শিল্পীর তালিকা"
                currentRoute == AppDestinations.LYRICIST_LIST_ROUTE -> "গীতিকারের তালিকা"
                currentRoute == AppDestinations.COMPOSER_LIST_ROUTE -> "সুরকারের তালিকা"
                currentRoute == AppDestinations.ERA_LIST_ROUTE -> "যুগের তালিকা"
                currentRoute == AppDestinations.GENRE_LIST_ROUTE -> "ধরনের তালিকা"
                currentRoute?.startsWith(AppDestinations.SONGS_BY_CATEGORY_ROUTE_BASE) == true -> {
                    val categoryName = currentBackStack?.arguments?.getString(AppDestinations.SONGS_BY_CATEGORY_ARG_NAME)
                    URLDecoder.decode(categoryName ?: "গান", StandardCharsets.UTF_8.toString())
                }
                currentRoute?.startsWith(AppDestinations.SONG_DETAIL_ROUTE_BASE) == true -> "গানের বিবরণ"
                else -> "বাংলা গান"
            }
            BanglaGanTopAppBar(
                title = title,
                canNavigateBack = navController.previousBackStackEntry != null && currentRoute != AppDestinations.HOME_ROUTE,
                navigateUp = { navController.navigateUp() },
                showSearchIcon = !(currentRoute?.startsWith(AppDestinations.SONG_DETAIL_ROUTE_BASE) == true ||
                        currentRoute == AppDestinations.SEARCH_ROUTE_PATTERN ||
                        currentRoute == AppDestinations.ADD_SONG_ROUTE ||
                        currentRoute == AppDestinations.STATISTICS_ROUTE),
                onSearchClick = { navController.navigate(AppDestinations.searchRoute()) }
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
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পেছনে যান")
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
                    popUpTo(AppDestinations.HOME_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "সব গান") },
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
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = "যোগ করুন") },
            label = { Text("যোগ করুন") },
            selected = currentRoute == AppDestinations.ADD_SONG_ROUTE,
            onClick = {
                navController.navigate(AppDestinations.ADD_SONG_ROUTE) {
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
            ActualHomeScreen(
                onNavigateToSearch = { query, type -> navController.navigate(AppDestinations.searchRoute(query, type)) },
                onNavigateToArtistList = { navController.navigate(AppDestinations.ARTIST_LIST_ROUTE) },
                onNavigateToLyricistList = { navController.navigate(AppDestinations.LYRICIST_LIST_ROUTE) },
                onNavigateToComposerList = { navController.navigate(AppDestinations.COMPOSER_LIST_ROUTE) },
                onNavigateToEraList = { navController.navigate(AppDestinations.ERA_LIST_ROUTE) },
                onNavigateToGenreList = { navController.navigate(AppDestinations.GENRE_LIST_ROUTE) },
                onNavigateToAddSong = { navController.navigate(AppDestinations.ADD_SONG_ROUTE) },
                onNavigateToStatistics = { navController.navigate(AppDestinations.STATISTICS_ROUTE) },
                songViewModel = songViewModel,
                navController = navController
            )
        }
        
        composable(AppDestinations.ADD_SONG_ROUTE) {
            AddSongScreen(
                songViewModel = songViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(AppDestinations.STATISTICS_ROUTE) {
            StatisticsScreen(
                songViewModel = songViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(AppDestinations.SONG_LIST_ROUTE) {
            val songUiState by songViewModel.songUiState.collectAsState()
            SongListScreen(
                uiState = songUiState,
                onSongClick = { songId -> navController.navigate(AppDestinations.songDetailRoute(songId)) },
                onFavoriteToggle = { song -> songViewModel.toggleFavoriteStatus(song) }
            )
        }
        composable(
            route = AppDestinations.SONG_DETAIL_ROUTE_PATTERN,
            arguments = listOf(navArgument(AppDestinations.SONG_DETAIL_ARG_SONG_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getInt(AppDestinations.SONG_DETAIL_ARG_SONG_ID)
            var songDetailFromVM by remember { mutableStateOf<Song?>(null) }
            var isLoadingSongDetail by remember { mutableStateOf(true) }

            LaunchedEffect(songId) {
                isLoadingSongDetail = true
                songDetailFromVM = songId?.let { songViewModel.getSongById(it) }
                isLoadingSongDetail = false
            }

            if (isLoadingSongDetail && songId != null) {
                LoadingScreen()
            } else if (songDetailFromVM != null) {
                SongDetailScreen(
                    song = songDetailFromVM!!,
                    songViewModel = songViewModel,
                    onArtistNameClick = { artistName ->
                        artistName.let { navController.navigate(AppDestinations.songsByCategoryRoute("artist", it.trim())) }
                    }
                )
            } else {
                Text("গান পাওয়া যায়নি অথবা অবৈধ আইডি।")
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
        composable(
            route = AppDestinations.SEARCH_ROUTE_PATTERN,
            arguments = listOf(
                navArgument(AppDestinations.SEARCH_ARG_QUERY) { type = NavType.StringType; defaultValue = "" },
                navArgument(AppDestinations.SEARCH_ARG_TYPE) { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val query = URLDecoder.decode(backStackEntry.arguments?.getString(AppDestinations.SEARCH_ARG_QUERY) ?: "", StandardCharsets.UTF_8.toString())
            LaunchedEffect(query) { songViewModel.searchSongs(query) }
            val searchResultsState by songViewModel.searchResults.collectAsState()

            SearchScreen(
                searchQuery = query,
                onSearchQueryChange = { newQuery ->
                    navController.navigate(AppDestinations.searchRoute(query = newQuery)) {
                        popUpTo(AppDestinations.SEARCH_ROUTE_PATTERN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                searchResults = searchResultsState,
                onSongClick = { songId -> navController.navigate(AppDestinations.songDetailRoute(songId)) },
                onFavoriteToggle = { song -> songViewModel.toggleFavoriteStatus(song) }
            )
        }

        composable(AppDestinations.ARTIST_LIST_ROUTE) {
            GenericListScreen(
                title = "শিল্পীর তালিকা",
                itemsFlow = songViewModel.artists,
                onItemClick = { artistName ->
                    navController.navigate(AppDestinations.songsByCategoryRoute("artist", artistName))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.LYRICIST_LIST_ROUTE) {
            GenericListScreen(
                title = "গীতিকারের তালিকা",
                itemsFlow = songViewModel.lyricists,
                onItemClick = { lyricistName ->
                    navController.navigate(AppDestinations.songsByCategoryRoute("lyricist", lyricistName))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.COMPOSER_LIST_ROUTE) {
            GenericListScreen(
                title = "সুরকারের তালিকা",
                itemsFlow = songViewModel.composers,
                onItemClick = { composerName ->
                    navController.navigate(AppDestinations.songsByCategoryRoute("composer", composerName))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.ERA_LIST_ROUTE) {
            GenericListScreen(
                title = "যুগের তালিকা",
                itemsFlow = songViewModel.eras,
                onItemClick = { eraName ->
                    navController.navigate(AppDestinations.songsByCategoryRoute("era", eraName))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.GENRE_LIST_ROUTE) {
            GenericListScreen(
                title = "ধরনের তালিকা",
                itemsFlow = songViewModel.genres,
                onItemClick = { genreName ->
                    navController.navigate(AppDestinations.songsByCategoryRoute("genre", genreName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestinations.SONGS_BY_CATEGORY_ROUTE_PATTERN,
            arguments = listOf(
                navArgument(AppDestinations.SONGS_BY_CATEGORY_ARG_TYPE) { type = NavType.StringType },
                navArgument(AppDestinations.SONGS_BY_CATEGORY_ARG_NAME) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryType = backStackEntry.arguments?.getString(AppDestinations.SONGS_BY_CATEGORY_ARG_TYPE) ?: ""
            val encodedCategoryName = backStackEntry.arguments?.getString(AppDestinations.SONGS_BY_CATEGORY_ARG_NAME) ?: ""
            val categoryName = URLDecoder.decode(encodedCategoryName, StandardCharsets.UTF_8.toString())

            var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
            var isLoadingSongs by remember { mutableStateOf(true) }

            LaunchedEffect(categoryType, categoryName) {
                isLoadingSongs = true
                songViewModel.loadSongsForCategory(categoryType, categoryName)
                songViewModel.songsBySelectedCategory.collect { collectedSongs ->
                    songs = collectedSongs
                    isLoadingSongs = false
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(categoryName) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                if (isLoadingSongs) {
                    LoadingScreen(modifier = Modifier.padding(paddingValues))
                } else if (songs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center){
                        Text("$categoryName ক্যাটেগরিতে কোনো গান পাওয়া যায়নি।")
                    }
                } else {
                    SongList(
                        songs = songs,
                        onSongClick = { songId -> navController.navigate(AppDestinations.songDetailRoute(songId)) },
                        onFavoriteToggle = { song -> songViewModel.toggleFavoriteStatus(song) },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsSection(uiState: SongUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) { StatsCard(label = "মোট গান", count = uiState.totalSongs) }
            Box(modifier = Modifier.weight(1f)) { StatsCard(label = "মোট শিল্পী", count = uiState.totalArtists) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) { StatsCard(label = "মোট গীতিকার", count = uiState.totalLyricists) }
            Box(modifier = Modifier.weight(1f)) { StatsCard(label = "মোট সুরকার", count = uiState.totalComposers) }
        }
    }
}

@Composable
fun StatsCard(label: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().aspectRatio(1.5f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toBanglaString(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        items(items = songs, key = { song -> song.id }) { song ->
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
            .padding(vertical = 4.dp)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "শিল্পী: ${song.artistName ?: "অজানা"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!song.era.isNullOrEmpty() || !song.genre.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!song.era.isNullOrEmpty()) {
                            AssistChip(
                                onClick = { },
                                label = { Text(song.era, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        if (!song.genre.isNullOrEmpty()) {
                            AssistChip(
                                onClick = { },
                                label = { Text(song.genre, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (song.isFavorite) "পছন্দ থেকে সরান" else "পছন্দ করুন",
                    tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            song.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { songViewModel.toggleFavoriteStatus(song) }) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "ফেভারিট",
                                tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.clickable(enabled = song.artistName != null) {
                            song.artistName?.let { onArtistNameClick(it) }
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = song.artistName ?: "অজানা",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textDecoration = if (song.artistName != null) androidx.compose.ui.text.style.TextDecoration.Underline else null
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(icon = Icons.Filled.Album, label = "অ্যালবাম", value = song.albumName)
                    DetailRow(icon = Icons.Filled.EditNote, label = "গীতিকার", value = song.lyricist)
                    DetailRow(icon = Icons.Filled.LibraryMusic, label = "সুরকার", value = song.composer)
                    DetailRow(icon = Icons.Filled.AccessTime, label = "যুগ", value = song.era)
                    DetailRow(icon = Icons.Filled.Category, label = "ধরণ", value = song.genre)
                    if (song.releaseYear != null && song.releaseYear > 0) {
                        DetailRow(icon = Icons.Filled.CalendarToday, label = "প্রকাশকাল", value = song.releaseYear.toBanglaString())
                    }
                }
            }
        }

        if (!song.lyrics.isNullOrEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "গানের কথা",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            song.lyrics!!,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                        )
                    }
                }
            }
        }

        if (!song.notes.isNullOrEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "বিশেষ তথ্য",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            song.notes!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String?) {
    if (!value.isNullOrEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
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
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("গান, শিল্পী, অ্যালবাম...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
            Text("'$searchQuery' এর জন্য কোনো ফলাফল পাওয়া যায়নি।")
        } else {
            SongList(songs = searchResults, onSongClick = onSongClick, onFavoriteToggle = onFavoriteToggle)
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
        Text(text = "লোড হচ্ছে...", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun EmptySongListScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("কোনো গান পাওয়া যায়নি।", style = MaterialTheme.typography.headlineSmall)
        Text("কিছু গান যোগ করুন অথবা ডাটাবেস সিঙ্ক করুন!", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top=4.dp))
    }
}

@Composable
fun ErrorScreen(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("একটি সমস্যা হয়েছে:", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
        Text(message, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
    }
}

// ✅ প্রিভিউ ফাংশনগুলো সঠিকভাবে সেটআপ করা হয়েছে
@Preview(showBackground = true, name = "Song Item Preview")
@Composable
fun SongItemPreview() {
    BanglaGanTheme {
        SongItem(
            song = Song(
                id = 1, 
                title = "আমার সোনার বাংলা", 
                artistName = "রবীন্দ্রনাথ ঠাকুর", 
                era = "আধুনিক", 
                genre = "রবীন্দ্রসঙ্গীত", 
                isFavorite = true, 
                albumName = "গীতবিতান", 
                lyricist = "রবীন্দ্রনাথ ঠাকুর", 
                composer = "রবীন্দ্রনাথ ঠাকুর", 
                lyrics = "আমার সোনার বাংলা, আমি তোমায় ভালোবাসি...", 
                releaseYear = 1905, 
                notes = "জাতীয় সঙ্গীত", 
                audioUrl = null, 
                videoUrl = null
            ),
            onFavoriteToggle = {},
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun HomeScreenPreview() {
    BanglaGanTheme {
        val dummyViewModel = SongViewModel(SongRepository(FakeSongDao()))
        ActualHomeScreen(
            onNavigateToSearch = { _, _ -> },
            onNavigateToArtistList = {},
            onNavigateToLyricistList = {},
            onNavigateToComposerList = {},
            onNavigateToEraList = {},
            onNavigateToGenreList = {},
            onNavigateToAddSong = {},
            onNavigateToStatistics = {},
            songViewModel = dummyViewModel,
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
            lyrics = "কফি হাউসের সেই আড্ডাটা আজ আর নেই,\nকোথায় হারিয়ে গেল সোনালী বিকেলগুলো সেই...", 
            isFavorite = true, 
            releaseYear = 1983, 
            notes = "একটি কালজয়ী গান", 
            audioUrl = null, 
            videoUrl = null
        )
        val dummyViewModel = SongViewModel(SongRepository(FakeSongDao()))
        SongDetailScreen(
            song = previewSong, 
            songViewModel = dummyViewModel, 
            onArtistNameClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Stats Section Preview")
@Composable
fun StatsSectionPreview() {
    BanglaGanTheme {
        StatsSection(
            uiState = SongUiState(
                totalSongs = 869,
                totalArtists = 29,
                totalLyricists = 990,
                totalComposers = 67
            )
        )
    }
}

@Preview(showBackground = true, name = "Category Button Preview")
@Composable
fun CategoryButtonPreview() {
    BanglaGanTheme {
        CategoryHomeButton(
            label = "শিল্পী",
            icon = Icons.Filled.Person,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Search Screen Preview")
@Composable
fun SearchScreenPreview() {
    BanglaGanTheme {
        SearchScreen(
            searchQuery = "রবীন্দ্রনাথ",
            onSearchQueryChange = {},
            searchResults = listOf(
                Song(
                    id = 1, 
                    title = "আমার সোনার বাংলা", 
                    artistName = "রবীন্দ্রনাথ ঠাকুর", 
                    era = "আধুনিক", 
                    genre = "রবীন্দ্রসঙ্গীত"
                )
            ),
            onSongClick = {},
            onFavoriteToggle = {}
        )
    }
}

// FakeSongDao প্রিভিউয়ের জন্য
private class FakeSongDao : SongDao {
    override fun getAllSongs(): Flow<List<Song>> = flowOf(listOf(
        Song(id = 1, title = "আমার সোনার বাংলা", artistName = "রবীন্দ্রনাথ ঠাকুর", era = "আধুনিক", genre = "রবীন্দ্রসঙ্গীত", isFavorite = true, albumName = "গীতবিতান", lyricist = "রবীন্দ্রনাথ ঠাকুর", composer = "রবীন্দ্রনাথ ঠাকুর", lyrics = "...", releaseYear = 1905, notes = "জাতীয় সঙ্গীত", audioUrl = null, videoUrl = null),
        Song(id = 2, title = "কফি হাউসের সেই আড্ডাটা", artistName = "মান্না দে", genre = "আধুনিক বাংলা", albumName = "কফি হাউস", lyricist = "গৌরীপ্রসন্ন মজুমদার", composer = "সুপর্ণকান্তি ঘোষ", era = "১৯৮৩", lyrics = "...", releaseYear = 1983, notes = "জনপ্রিয় গান", isFavorite = false, audioUrl = null, videoUrl = null)
    ))
    override fun getFavoriteSongs(): Flow<List<Song>> = flowOf(listOf(
        Song(id = 1, title = "আমার সোনার বাংলা", artistName = "রবীন্দ্রনাথ ঠাকুর", era = "আধুনিক", genre = "রবীন্দ্রসঙ্গীত", isFavorite = true, albumName = "গীতবিতান", lyricist = "রবীন্দ্রনাথ ঠাকুর", composer = "রবীন্দ্রনাথ ঠাকুর", lyrics = "...", releaseYear = 1905, notes = "জাতীয় সঙ্গীত", audioUrl = null, videoUrl = null)
    ))
    override fun getSongById(songId: Int): Flow<Song?> = flowOf(
        Song(id = songId, title = "কফি হাউসের সেই আড্ডাটা ($songId)", artistName = "মান্না দে", albumName = "Single", lyricist = "গৌরীপ্রসন্ন মজুমদার", composer = "সুপর্ণকান্তি ঘোষ", era = "আধুনিক", genre = "আধুনিক বাংলা", lyrics = "কফি হাউসের সেই আড্ডাটা আজ আর নেই...", isFavorite = true, releaseYear = 1983, notes = "একটি কালজয়ী গান", audioUrl = null, videoUrl = null)
    )
    override suspend fun insertSong(song: Song) {}
    override suspend fun insertAllSongs(songs: List<Song>) {}
    override suspend fun updateSong(song: Song) {}
    override suspend fun deleteSong(song: Song) {}
    override fun searchSongs(query: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongsByArtist(artistName: String): Flow<List<Song>> = flowOf(
        listOf(
            Song(id = 3, title = "গান ১ শিল্পী $artistName", artistName = artistName, albumName = "", lyricist = "", composer = "", era = "", genre = "", lyrics = "", isFavorite = false, releaseYear = null, notes = null, audioUrl = null, videoUrl = null),
            Song(id = 4, title = "গান ২ শিল্পী $artistName", artistName = artistName, albumName = "", lyricist = "", composer = "", era = "", genre = "", lyrics = "", isFavorite = false, releaseYear = null, notes = null, audioUrl = null, videoUrl = null)
        )
    )
    override fun getSongsByGenre(genreName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongCount(): Flow<Int> = flowOf(869)
    override fun getArtistCount(): Flow<Int> = flowOf(29)
    override fun getLyricistCount(): Flow<Int> = flowOf(990)
    override fun getComposerCount(): Flow<Int> = flowOf(67)
    override fun getAllArtists(): Flow<List<String>> = flowOf(listOf("রবীন্দ্রনাথ ঠাকুর", "মান্না দে", "কাজী নজরুল ইসলাম"))
    override fun getSongsByLyricist(lyricistName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongsByComposer(composerName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongsByEra(eraName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getAllLyricists(): Flow<List<String>> = flowOf(listOf("গীতিকার ১", "গীতিকার ২"))
    override fun getAllComposers(): Flow<List<String>> = flowOf(listOf("সুরকার ১", "সুরকার ২"))
    override fun getAllGenres(): Flow<List<String>> = flowOf(listOf("রবীন্দ্রসঙ্গীত", "নজরুলগীতি", "আধুনিক"))
    override fun getAllEras(): Flow<List<String>> = flowOf(listOf("প্রাচীন", "মধ্যযুগ", "আধুনিক"))
    override fun getEraCount(): Flow<Int> = flowOf(3)
    override fun getGenreCount(): Flow<Int> = flowOf(3)
}