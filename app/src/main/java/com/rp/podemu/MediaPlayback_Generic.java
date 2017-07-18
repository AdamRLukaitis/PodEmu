/**

 Copyright (C) 2017, Roman P., dev.roman [at] gmail

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see http://www.gnu.org/licenses/

 */

package com.rp.podemu;

/**
 * Created by rp on 9/1/15.
 */
public class MediaPlayback_Generic extends MediaPlayback
{
    private boolean trackStatusChanged=true;
    private PodEmuMediaStore.Playlist currentPlaylist;


    private boolean isPlaying=false;
    private int positionMS;
    private long timeSent;

    MediaPlayback_Generic()
    {
        if(currentPlaylist == null)
        {
            PodEmuMediaStore podEmuMediaStore=PodEmuMediaStore.getInstance();
            currentPlaylist = podEmuMediaStore.new Playlist();
        }
    }

    @Override
    public void setCurrentPlaylist(PodEmuMediaStore.Playlist playlist)
    {
        currentPlaylist=playlist;
    }

    public boolean getTrackStatusChanged()
    {
        return trackStatusChanged;
    }

    public void setTrackStatusChanged(boolean status)
    {
        trackStatusChanged = status;
    }

    public PodEmuMediaStore.Playlist getCurrentPlaylist()
    {
        return currentPlaylist;
    }


    /**
     * Calculating exact position of currently played song in MS.
     * We don't need to bother if the song was paused as we receive updated
     * postion once it is paused or unpaused.
     * @return position in miliseconds
     */
    public int getCurrentTrackPositionMS()
    {
        int position;

        // if playback is active then calculate real position, otherwise return last known position
        if(isPlaying)
        {
            position = positionMS + (int)(System.currentTimeMillis() - timeSent);
        }
        else
        {
            position = positionMS;
        }
        return position;
    }

    public boolean isPlaying()
    {
        return isPlaying;
    }





    public void    updateCurrentlyPlayingTrack(PodEmuMessage msg)
    {
        trackStatusChanged = true;

        if(msg.getAction()!=PodEmuMessage.ACTION_QUEUE_CHANGED)
        {
            PodEmuMediaStore.Track track;
            int listSize = msg.getListSize();
            boolean dummy_playlist=false;
            PodEmuMediaStore podEmuMediaStore=PodEmuMediaStore.getInstance();


            isPlaying = msg.isPlaying();
            timeSent = msg.getTimeSent();
            positionMS = msg.getPositionMS();

            // some media application does not provide listSize and listPosition information
            // if so, we should mimic it to fool the dock
            if(listSize<0)
            {
                listSize = PodEmuMediaStore.getInstance().getPlaylistCountSize();
                dummy_playlist = true;
            }
            PodEmuLog.debug("PEMSGen: updateCurrentlyPlayingTrack: track " + msg.getListPosition() + "/" + listSize + ", prev_size=" + currentPlaylist.getTrackCount());


            // if total song count changed then we need to rebuild DB
            if (currentPlaylist.getTrackCount() != listSize)
            {
                PodEmuMediaDB.getInstance().rebuildDB(PodEmuMediaStore.getInstance(), listSize);

                // update playback engine playlist and selection
                PodEmuMediaStore.getInstance().selectionReset();
                this.setCurrentPlaylist(PodEmuMediaStore.getInstance().selectionBuildPlaylist());

                // TODO: check if initialization for tracks is ok
                podEmuMediaStore.selectionReset();
                podEmuMediaStore.selectionInitializeDB(1 /* 1=playlist */);

                if( msg.getListSize()>0 )
                {
                    PodEmuMediaStore.getInstance().setPlaylistCountMode(PlaylistCountDialogFragment.MODE_PLAYLIST_SIZE_NORMAL);
                }
                currentPlaylist.setCurrentTrackPosToStart();
            }

            if(dummy_playlist)
            {
                track = currentPlaylist.getCurrentTrack();
                track.track_number = currentPlaylist.getCurrentTrackPos();
            }
            else
            {
                currentPlaylist.setCurrentTrack(msg.getListPosition());
                track = currentPlaylist.getCurrentTrack();
                track.track_number = msg.getListPosition();
            }

            track.id = track.track_number; // this is true only for generic playlists
            track.length = msg.getLength();
            track.external_id = msg.getExternalId();
            track.name = msg.getTrackName();
            track.album = msg.getAlbum();
            track.genre = msg.getGenre();
            track.composer = msg.getComposer();
            track.artist = msg.getArtist();

            if(track.name==null)
            {
                track.name = "Generic track";
            }

            // finally update the title in the db and set track
            podEmuMediaStore.updateTrack(track);


            //currentPlaylist.getCurrentTrack().copyFrom(track1);

        }
    }

    /*
    Inserting KEYCODE_MEDIA_SKIP_FORWARD or KEYCODE_MEDIA_SKIP_BACKWARD will throw the following log

    09-12 23:42:56.013  27177-29132/com.rp.podemu W/dalvikvm﹕ threadid=13: thread exiting with uncaught exception (group=0x2b4e71f8)
    09-12 23:42:56.023  27177-29132/com.rp.podemu E/AndroidRuntime﹕ FATAL EXCEPTION: Thread-6329
        java.lang.SecurityException: Injecting to another application requires INJECT_EVENTS permission
                at android.os.Parcel.readException(Parcel.java:1327)
                at android.os.Parcel.readException(Parcel.java:1281)
                at android.view.IWindowManager$Stub$Proxy.injectKeyEvent(IWindowManager.java:1178)
                at android.app.Instrumentation.sendKeySync(Instrumentation.java:859)
                at android.app.Instrumentation.sendKeyDownUpSync(Instrumentation.java:871)
                at com.rp.podemu.MediaControlLibrary$6.run(MediaControlLibrary.java:93)
                at java.lang.Thread.run(Thread.java:856)

    could be fixed on rooted device
    SOLUTION: http://stackoverflow.com/questions/5383401/android-inject-events-permission?rq=1
 */

}
