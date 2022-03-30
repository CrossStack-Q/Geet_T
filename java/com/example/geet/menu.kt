//
//package com.example.geet
//
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.content.ContentUris
//import android.content.Context
//import android.content.Intent
//import android.graphics.drawable.AnimatedVectorDrawable
//import android.graphics.drawable.Drawable
//import android.media.MediaMetadataRetriever
//import android.os.Build
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.GestureDetector
//import android.view.MenuItem
//import android.view.MotionEvent
//import android.view.View
//import android.widget.RelativeLayout
//import android.widget.Toast
//import androidx.annotation.LayoutRes
//import androidx.appcompat.graphics.drawable.DrawableWrapper
//import androidx.appcompat.widget.Toolbar
//import androidx.core.os.bundleOf
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.findNavController
//import androidx.navigation.navOptions
//import androidx.viewpager.widget.ViewPager
//import code.name.monkey.retromusic.EXTRA_ALBUM_ID
//import code.name.monkey.retromusic.EXTRA_ARTIST_ID
//import code.name.monkey.retromusic.R
//import code.name.monkey.retromusic.activities.MainActivity
//import code.name.monkey.retromusic.activities.tageditor.AbsTagEditorActivity
//import code.name.monkey.retromusic.activities.tageditor.SongTagEditorActivity
//import code.name.monkey.retromusic.db.PlaylistEntity
//import code.name.monkey.retromusic.db.toSongEntity
//import code.name.monkey.retromusic.dialogs.*
//import code.name.monkey.retromusic.extensions.currentFragment
//import code.name.monkey.retromusic.extensions.hide
//import code.name.monkey.retromusic.extensions.whichFragment
//import code.name.monkey.retromusic.fragments.NowPlayingScreen
//import code.name.monkey.retromusic.fragments.ReloadType
//import code.name.monkey.retromusic.fragments.player.PlayerAlbumCoverFragment
//import code.name.monkey.retromusic.helper.MusicPlayerRemote
//import code.name.monkey.retromusic.interfaces.IPaletteColorHolder
//import code.name.monkey.retromusic.model.Song
//import code.name.monkey.retromusic.repository.RealRepository
//import code.name.monkey.retromusic.service.MusicService
//import code.name.monkey.retromusic.util.*
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//import kotlinx.coroutines.Dispatchers.IO
//import kotlinx.coroutines.Dispatchers.Main
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.koin.android.ext.android.get
//import kotlin.math.abs
//
//abstract class AbsPlayerFragment(@LayoutRes layout: Int) : AbsMainActivityFragment(layout),
//    Toolbar.OnMenuItemClickListener, IPaletteColorHolder, PlayerAlbumCoverFragment.Callbacks {
//
//    private var playerAlbumCoverFragment: PlayerAlbumCoverFragment? = null
//
//    override fun onMenuItemClick(
//        item: MenuItem
//    ): Boolean {
//        val song = MusicPlayerRemote.currentSong
//        when (item.itemId) {
//            R.id.action_toggle_lyrics -> {
//                PreferenceUtil.showLyrics = !item.isChecked
//                item.isChecked = !item.isChecked
//                return true
//            }
//            R.id.action_go_to_lyrics -> {
//                goToLyrics(requireActivity())
//                return true
//            }
//            R.id.action_toggle_favorite -> {
//                toggleFavorite(song)
//                return true
//            }
//            R.id.action_share -> {
//                SongShareDialog.create(song).show(childFragmentManager, "SHARE_SONG")
//                return true
//            }
//            R.id.action_go_to_drive_mode -> {
//                NavigationUtil.gotoDriveMode(requireActivity())
//                return true
//            }
//            R.id.action_delete_from_device -> {
//                DeleteSongsDialog.create(song).show(childFragmentManager, "DELETE_SONGS")
//                return true
//            }
//            R.id.action_add_to_playlist -> {
//                lifecycleScope.launch(IO) {
//                    val playlists = get<RealRepository>().fetchPlaylists()
//                    withContext(Main) {
//                        AddToPlaylistDialog.create(playlists, song)
//                            .show(childFragmentManager, "ADD_PLAYLIST")
//                    }
//                }
//                return true
//            }
//            R.id.action_clear_playing_queue -> {
//                MusicPlayerRemote.clearQueue()
//                return true
//            }
//            R.id.action_save_playing_queue -> {
//                CreatePlaylistDialog.create(ArrayList(MusicPlayerRemote.playingQueue))
//                    .show(childFragmentManager, "ADD_TO_PLAYLIST")
//                return true
//            }
//            R.id.action_tag_editor -> {
//                val intent = Intent(activity, SongTagEditorActivity::class.java)
//                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id)
//                startActivity(intent)
//                return true
//            }
//            R.id.action_details -> {
//                SongDetailDialog.create(song).show(childFragmentManager, "SONG_DETAIL")
//                return true
//            }
//            R.id.action_go_to_album -> {
//                //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
//                mainActivity.setBottomNavVisibility(false)
//                mainActivity.collapsePanel()
//                requireActivity().findNavController(R.id.fragment_container).navigate(
//                    R.id.albumDetailsFragment,
//                    bundleOf(EXTRA_ALBUM_ID to song.albumId)
//                )
//                return true
//            }
//            R.id.action_go_to_artist -> {
//                goToArtist(requireActivity())
//                return true
//            }
//            R.id.now_playing -> {
//                requireActivity().findNavController(R.id.fragment_container).navigate(
//                    R.id.playing_queue_fragment,
//                    null
//                )
//                return true
//            }
//            R.id.action_show_lyrics -> {
//                goToLyrics(requireActivity())
//                return true
//            }
//            R.id.action_equalizer -> {
//                NavigationUtil.openEqualizer(requireActivity())
//                return true
//            }
//            R.id.action_sleep_timer -> {
//                SleepTimerDialog().show(parentFragmentManager, TAG)
//                return true
//            }
//            R.id.action_set_as_ringtone -> {
//                if (RingtoneManager.requiresDialog(requireActivity())) {
//                    RingtoneManager.getDialog(requireActivity())
//                }
//                val ringtoneManager = RingtoneManager(requireActivity())
//                ringtoneManager.setRingtone(song)
//                return true
//            }
//            R.id.action_go_to_genre -> {
//                val retriever = MediaMetadataRetriever()
//                val trackUri =
//                    ContentUris.withAppendedId(
//                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                        song.id
//                    )
//                retriever.setDataSource(activity, trackUri)
//                var genre: String? =
//                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
//                if (genre == null) {
//                    genre = "Not Specified"
//                }
//                Toast.makeText(context, genre, Toast.LENGTH_SHORT).show()
//                return true
//            }
//        }
//        return false
//    }
//
//    abstract fun playerToolbar(): Toolbar?
//
//    abstract fun onShow()
//
//    abstract fun onHide()
//
//    abstract fun onBackPressed(): Boolean
//
//    abstract fun toolbarIconColor(): Int
//
//    override fun onServiceConnected() {
//        updateIsFavorite()
//    }
//
//    override fun onPlayingMetaChanged() {
//        updateIsFavorite()
//    }
//
//    override fun onFavoriteStateChanged() {
//        updateIsFavorite(animate = true)
//    }
//
//    protected open fun toggleFavorite(song: Song) {
//        lifecycleScope.launch(IO) {
//            val playlist: PlaylistEntity = libraryViewModel.favoritePlaylist()
//            if (playlist != null) {
//                val songEntity = song.toSongEntity(playlist.playListId)
//                val isFavorite = libraryViewModel.isSongFavorite(song.id)
//                if (isFavorite) {
//                    libraryViewModel.removeSongFromPlaylist(songEntity)
//                } else {
//                    libraryViewModel.insertSongs(listOf(song.toSongEntity(playlist.playListId)))
//                }
//            }
//            libraryViewModel.forceReload(ReloadType.Playlists)
//            requireContext().sendBroadcast(Intent(MusicService.FAVORITE_STATE_CHANGED))
//        }
//    }
//
//    fun updateIsFavorite(animate: Boolean = false) {
//        lifecycleScope.launch(IO) {
//            val isFavorite: Boolean =
//                libraryViewModel.isSongFavorite(MusicPlayerRemote.currentSong.id)
//            withContext(Main) {
//                val icon = if (animate) {
//                    if (isFavorite) R.drawable.avd_favorite else R.drawable.avd_unfavorite
//                } else {
//                    if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
//                }
//                val drawable: Drawable = RetroUtil.getTintedVectorDrawable(
//                    requireContext(),
//                    icon,
//                    toolbarIconColor()
//                )
//                if (playerToolbar() != null) {
//                    playerToolbar()?.menu?.findItem(R.id.action_toggle_favorite)?.apply {
//                        setIcon(drawable)
//                        title =
//                            if (isFavorite) getString(R.string.action_remove_from_favorites)
//                            else getString(R.string.action_add_to_favorites)
//                        getIcon().also {
//                            if (it is AnimatedVectorDrawable) {
//                                it.start()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        if (PreferenceUtil.isFullScreenMode &&
//            view.findViewById<View>(R.id.status_bar) != null
//        ) {
//            view.findViewById<View>(R.id.status_bar).visibility = View.GONE
//        }
//        playerAlbumCoverFragment = whichFragment(R.id.playerAlbumCoverFragment)
//        playerAlbumCoverFragment?.setCallbacks(this)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            view.findViewById<RelativeLayout>(R.id.statusBarShadow)?.hide()
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onResume() {
//        super.onResume()
//        val nps = PreferenceUtil.nowPlayingScreen
//
//        if (nps == NowPlayingScreen.Circle || nps == NowPlayingScreen.Peak || nps == NowPlayingScreen.Tiny) {
//            playerToolbar()?.menu?.removeItem(R.id.action_toggle_lyrics)
//        } else {
//            playerToolbar()?.menu?.findItem(R.id.action_toggle_lyrics)?.apply {
//                fixCheckStateOnIcon()
//                isCheckable = true
//                isChecked = PreferenceUtil.showLyrics
//            }
//        }
//        requireView().setOnTouchListener(
//            SwipeDetector(
//                requireContext(),
//                playerAlbumCoverFragment?.viewPager,
//                requireView()
//            )
//        )
//    }
//
//    class SwipeDetector(val context: Context, val viewPager: ViewPager?, val view: View) :
//        View.OnTouchListener {
//        private var flingPlayBackController: GestureDetector = GestureDetector(
//            context,
//            object : GestureDetector.SimpleOnGestureListener() {
//                override fun onScroll(
//                    e1: MotionEvent?,
//                    e2: MotionEvent?,
//                    distanceX: Float,
//                    distanceY: Float
//                ): Boolean {
//                    return when {
//                        abs(distanceX) > abs(distanceY) -> {
//                            // Disallow Intercept Touch Event so that parent(BottomSheet) doesn't consume the events
//                            view.parent.requestDisallowInterceptTouchEvent(true)
//                            true
//                        }
//                        else -> {
//                            false
//                        }
//                    }
//                }
//            })
//
//        @SuppressLint("ClickableViewAccessibility")
//        override fun onTouch(v: View, event: MotionEvent?): Boolean {
//            viewPager?.dispatchTouchEvent(event)
//            return flingPlayBackController.onTouchEvent(event)
//        }
//    }
//
//    companion object {
//        val TAG: String = AbsPlayerFragment::class.java.simpleName
//        const val VISIBILITY_ANIM_DURATION: Long = 300
//    }
//
//    protected fun getUpNextAndQueueTime(): String {
//        val duration = MusicPlayerRemote.getQueueDurationMillis(MusicPlayerRemote.position)
//
//        return MusicUtil.buildInfoString(
//            resources.getString(R.string.up_next),
//            MusicUtil.getReadableDurationString(duration)
//        )
//    }
//}
//
//fun goToArtist(activity: Activity) {
//    if (activity !is MainActivity) return
//    val song = MusicPlayerRemote.currentSong
//    activity.apply {
//
//        // Remove exit transition of current fragment so
//        // it doesn't exit with a weird transition
//        currentFragment(R.id.fragment_container)?.exitTransition = null
//
//        //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
//        setBottomNavVisibility(false)
//        if (getBottomSheetBehavior().state == BottomSheetBehavior.STATE_EXPANDED) {
//            collapsePanel()
//        }
//
//        findNavController(R.id.fragment_container).navigate(
//            R.id.artistDetailsFragment,
//            bundleOf(EXTRA_ARTIST_ID to song.artistId),
//            navOptions {
//                launchSingleTop = true
//            },
//            null
//        )
//    }
//}
//
//fun goToAlbum(activity: Activity) {
//    if (activity !is MainActivity) return
//    val song = MusicPlayerRemote.currentSong
//    activity.apply {
//        currentFragment(R.id.fragment_container)?.exitTransition = null
//
//        //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
//        setBottomNavVisibility(false)
//        if (getBottomSheetBehavior().state == BottomSheetBehavior.STATE_EXPANDED) {
//            collapsePanel()
//        }
//
//        findNavController(R.id.fragment_container).navigate(
//            R.id.albumDetailsFragment,
//            bundleOf(EXTRA_ALBUM_ID to song.albumId),
//            navOptions {
//                launchSingleTop = true
//            },
//            null
//        )
//    }
//}
//
//fun goToLyrics(activity: Activity) {
//    if (activity !is MainActivity) return
//    activity.apply {
//        //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
//        setBottomNavVisibility(false)
//        if (getBottomSheetBehavior().state == BottomSheetBehavior.STATE_EXPANDED) {
//            collapsePanel()
//        }
//
//        findNavController(R.id.fragment_container).navigate(
//            R.id.lyrics_fragment,
//            null,
//            navOptions { launchSingleTop = true },
//            null
//        )
//    }
//}
///** Fixes checked state being ignored by injecting checked state directly into drawable */
//@SuppressLint("RestrictedApi")
//class CheckDrawableWrapper(val menuItem: MenuItem) : DrawableWrapper(menuItem.icon) {
//    // inject checked state into drawable state set
//    override fun setState(stateSet: IntArray) = super.setState(
//        if (menuItem.isChecked) stateSet + android.R.attr.state_checked else stateSet
//    )
//}
//
///** Wrap icon drawable with [CheckDrawableWrapper]. */
//fun MenuItem.fixCheckStateOnIcon() = apply { icon = CheckDrawableWrapper(this) }