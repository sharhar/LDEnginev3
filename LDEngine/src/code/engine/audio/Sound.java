package code.engine.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbisInfo;

import code.engine.utils.FileUtils;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.stb.STBVorbis.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class Sound {
	
	int buffer;
	int source;
	
	public Sound(String path) {
		buffer = alGenBuffers();
		source = alGenSources();
		
		STBVorbisInfo info = STBVorbisInfo.malloc();
		
		ByteBuffer vorbis;
		try {
			vorbis = FileUtils.ioResourceToByteBuffer(path, 32 * 1024);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		IntBuffer error = BufferUtils.createIntBuffer(1);
		long decoder = stb_vorbis_open_memory(vorbis, error, null);
		if ( decoder == NULL )
			throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));

		stb_vorbis_get_info(decoder, info);

		int channels = info.channels();

		int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

		ShortBuffer pcm = BufferUtils.createShortBuffer(lengthSamples);

		pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
		stb_vorbis_close(decoder);
		
		alBufferData(buffer, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
		
		alSourcei(source, AL_BUFFER, buffer);
		
		alSourcei(source, AL_LOOPING, AL_FALSE);
	}
	
	public void play() {
		alSourcePlay(source);
	}
	
	public void stop() {
		alSourceStop(source);
	}
	
	public void destroy() {
		alDeleteBuffers(buffer);
		alDeleteSources(source);
	}
}
