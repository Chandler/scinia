package com.scinia

// we can get away without mbids and albums
// but timestamp track and artist are required.
case class SongPlay(
  time:        String,
  trackName:   String,
  trackMbid:   Option[String],
  artistName:  String,
  artistMbid:  Option[String],
  albumName:   Option[String],
  albumMbid:   Option[String],
  sourceId:    Int
)
