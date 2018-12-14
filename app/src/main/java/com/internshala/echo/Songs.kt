package com.internshala.echo

import android.os.Parcel
import android.os.Parcelable

class Songs( var SongID : Long, var songTitle : String,
             var artist : String, var songData : String,
             var dateAdded : Long) : Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong())

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeLong(SongID)
        p0?.writeString(songTitle)
        p0?.writeString(artist)
        p0?.writeString(songData)
        p0?.writeLong(dateAdded)
    }

    override fun describeContents(): Int {
        return 0
    }

    object Statified {

       var nameComparator : Comparator<Songs> = Comparator<Songs> { song1, song2 ->
           var songOne = song1.songTitle.toUpperCase()
           var songTwo = song2.songTitle.toUpperCase()
           songOne.compareTo(songTwo)
       }

        var dateComparator : Comparator<Songs> = Comparator<Songs> { song1, song2 ->
            var songOne = song1.dateAdded.toDouble()
            var songTwo = song2.dateAdded.toDouble()
            songTwo.compareTo(songOne)
        }
    }

    companion object CREATOR : Parcelable.Creator<Songs> {
        override fun createFromParcel(parcel: Parcel): Songs {
            return Songs(parcel)
        }

        override fun newArray(size: Int): Array<Songs?> {
            return arrayOfNulls(size)
        }
    }

}