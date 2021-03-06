package com.bjdodson.pocketbox.upnp.statemachine;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.impl.state.AbstractState;
import org.teleal.cling.support.avtransport.impl.state.NoMediaPresent;
import org.teleal.cling.support.avtransport.lastchange.AVTransportVariable;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;

import android.media.MediaPlayer;
import android.util.Log;

import com.bjdodson.pocketbox.upnp.MediaRenderer;
import com.bjdodson.pocketbox.upnp.PlaylistManagerService;
import com.bjdodson.pocketbox.upnp.PlaylistManagerService.Playlist;

public class PBNoMediaPresent extends NoMediaPresent<AVTransport> {
	private static final String TAG = "jinzora";
	
    public PBNoMediaPresent(AVTransport transport) {
        super(transport);
        
        // This is guaranteed to be the first state we see.
        MediaRenderer renderer = MediaRenderer.getInstance();
        MediaRenderer.getInstance().setAVTransport(transport);
        synchronized(renderer) {
        	renderer.notify();
        }
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
    	
    	// UPnP input vs notification from PlaylistManager
    	if (!PlaylistManagerService.META_PLAYLIST_CHANGED.equals(metaData)) {
    		PlaylistManagerService pmService = MediaRenderer.getInstance().getPlaylistManager();
        	UnsignedIntegerFourBytes instanceId = getTransport().getInstanceId();
    		pmService.setAVTransportURI(instanceId, uri.toString(), metaData);
	
    		AVTransport transport = getTransport();
	    	PBTransitionHelpers.setTrackDetails(transport, uri, metaData);
    	}        
        
    	MediaPlayer player = MediaRenderer.getInstance().getMediaPlayer();
    	Playlist playlist = MediaRenderer.getInstance().getPlaylistManager().getPlaylist();
    	String url = playlist.list.get(playlist.cursor).uri;
    	
    	try {
    		player.reset();
    		player.setDataSource(url);
    	} catch (IOException e) {
    		Log.e(TAG, "could not set data source", e);
    		return PBNoMediaPresent.class;
    	}

        return PBStopped.class;
    }
}