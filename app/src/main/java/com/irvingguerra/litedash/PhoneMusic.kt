package com.vieyra28.litekia

import com.vieyra28.litekia.feature.link.PhoneLink
import com.vieyra28.litekia.feature.link.PhoneSong
import com.vieyra28.litekia.feature.music.MusicRepository
import com.vieyra28.litekia.feature.music.Song
import com.vieyra28.litekia.feature.music.SourceKind

/** La música del celular (LiteKia Connect) entra a la pieza Música y sus botones regresan por el enlace. */
fun wirePhoneMusic(link: PhoneLink, music: MusicRepository) {
    link.onMusic = { song -> music.fromPhone(song?.toSong(), artComing = song != null && song.hasArt && song.art == null) }
    music.phoneControl = link::command
    // El enlace ya traía una canción antes de unirlos (LiteKia volvió al frente).
    link.song?.let { link.onMusic?.invoke(it) }
}

private fun PhoneSong.toSong() = Song(
    title = title,
    artist = artist,
    app = app,
    artwork = art,
    playing = playing,
    durationMs = durationMs,
    positionMs = positionMs,
    readAt = readAt,
    speed = speed,
    pkg = "",
    seekable = seekable,
    actions = emptyList(),
    source = SourceKind.BLUETOOTH,
    buffering = buffering,
    error = null,
    phone = true,
)
