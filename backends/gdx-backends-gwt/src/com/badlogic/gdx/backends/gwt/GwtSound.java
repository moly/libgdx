/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.gwt.soundmanager2.SMSound;
import com.badlogic.gdx.backends.gwt.soundmanager2.SMSound.SoundOptions;
import com.badlogic.gdx.backends.gwt.soundmanager2.SoundManager;
import com.badlogic.gdx.files.FileHandle;

public class GwtSound implements Sound {
	
	/** The maximum number of sound instances to create to support simultaneous playback. */
	private static final int MAX_SOUNDS = 8;
	
	/** Our sounds. */
	private SMSound[] sounds;
	/** The next player we think should be available for play - we circling through them to find a free one. */
	private int soundIndex;
	/** The path to the sound file. */
	private String soundURL;
	
	private SoundOptions soundOptions;
	
	public GwtSound (FileHandle file) {
		String url = ((GwtApplication)Gdx.app).getBaseUrl() + file.path();
		sounds = new SMSound[MAX_SOUNDS];
		sounds[0] = new SMSound(SoundManager.createSound(url));
		soundOptions = new SoundOptions();
		soundIndex = 0;
	}
	
	/** Let's find a sound that isn't currently playing.
	 * @return  The index of the sound or -1 if none is available. */
	private int findAvailableSound() {
		for (int i = 0; i < sounds.length; i++) {
			int index = (soundIndex + i) % sounds.length;
			if (sounds[index] == null || sounds[index].playState() == 0) {
				// point to the next likely free player
				soundIndex = (index + 1) % sounds.length; 
				
				// return the free player
				return index;
			}
		}
		
		// all are busy playing, stop the next sound in the queue and reuse it
		int index = soundIndex % sounds.length;
		soundIndex = (index + 1) % sounds.length;
		return index;
	}

	@Override
	public long play () {
		return play(1.0f, 1.0f, 0.0f, false, 0);
	}

	@Override
	public long play (float volume) {
		return play(volume, 1.0f, 0.0f, false, 0);
	}

	@Override
	public long play (float volume, float pitch, float pan) {
		return play(volume, pitch, pan, false, 0);
	}
	
	private long play (float volume, float pitch, float pan, boolean loop, int offset) {
		int soundId = findAvailableSound();
		if (soundId >= 0)
		{
			SMSound sound = sounds[soundId];
			if (sound == null)
				sound = new SMSound(SoundManager.createSound(soundURL));
			sound.stop();
			soundOptions.volume = (int)(volume * 100);
			soundOptions.pan = (int)(pan * 100);
			soundOptions.loops = loop ? Integer.MAX_VALUE : 1;
			soundOptions.offset = offset;
			sound.play(soundOptions);
		}
		return soundId;
	}

	@Override
	public long loop () {
		return play(1.0f, 1.0f, 0.0f, true, 0);
	}

	@Override
	public long loop (float volume) {
		return play(volume, 1.0f, 0.0f, true, 0);
	}
	
	@Override
	public long loop (float volume, float pitch, float pan) {
		return play(volume, pitch, pan, true, 0);
	}
	
	@Override
	public void stop () {
		for (int i = 0; i < sounds.length; i++) {
			if (sounds[i] != null)
				sounds[i].stop();
		}
	}

	@Override
	public void dispose () {
		stop();
		for (int i = 0; i < sounds.length; i++) {
			if (sounds[i] != null)
			sounds[i].destruct();
		}
		sounds = null;
	}

	@Override
	public void stop (long soundId) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
			sounds[(int)soundId].stop();
	}

	@Override
	public void setLooping (long soundId, boolean looping) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
		{
			SMSound sound = sounds[(int)soundId];
			if (sound.playState() == SMSound.PLAYING)
			{
				int offset = sound.getPosition();
				sound.stop();
				soundOptions.loops = looping ? Integer.MAX_VALUE : 1;
				soundOptions.volume = sound.getVolume();
				soundOptions.pan = sound.getPan();
				soundOptions.offset = offset;
				sound.play(soundOptions);
			}
		}
	}

	@Override
	public void setPitch (long soundId, float pitch) {
		// FIXME - Not possible?
	}

	@Override
	public void setVolume (long soundId, float volume) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
			sounds[(int)soundId].setVolume((int)(volume * 100));
	}
	
	public float getVolume (long soundId) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
			return sounds[(int)soundId].getVolume() / 100.f;
		else 
			return 0;
	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
		{
			sounds[(int)soundId].setVolume((int)(volume * 100));
			sounds[(int)soundId].setPan((int)(pan * 100));
		}
	}
	
	public float getPan (long soundId) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
			return sounds[(int)soundId].getPan() / 100.f;
		else 
			return 0;
	}

	@Override
	public void setPriority (long soundId, int priority) {
		// FIXME
	}
}