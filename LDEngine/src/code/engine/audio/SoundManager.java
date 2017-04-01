package code.engine.audio;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTThreadLocalContext.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import static org.lwjgl.system.MemoryUtil.*;

public class SoundManager {
	
	public static long device;
	public static ALCCapabilities deviceCaps;
	public static long context;
	
	public static void init() {
		device = alcOpenDevice((ByteBuffer)null);
		
		if(device == NULL) {
			throw new IllegalStateException("Failed to open openal device");
		}
		
		deviceCaps = ALC.createCapabilities(device);
		
		String defaultDeviceSpecifier = alcGetString(NULL, ALC_DEFAULT_DEVICE_SPECIFIER);
		if(defaultDeviceSpecifier == null) {
			throw new IllegalStateException("Failed to open openal device");
		}
		
		context = alcCreateContext(device, (IntBuffer)null);
		alcSetThreadContext(context);
		AL.createCapabilities(deviceCaps);
	}
	
	public static void destroy() {
		alcMakeContextCurrent(NULL);
		alcDestroyContext(context);
		alcCloseDevice(device);
	}
}
