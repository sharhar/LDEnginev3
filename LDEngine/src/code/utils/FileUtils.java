package code.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.lwjgl.BufferUtils;

public class FileUtils {
	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}

	public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
		ByteBuffer buffer = BufferUtils.createByteBuffer(bufferSize);

		FileInputStream source = new FileInputStream(resource);

		try {
			ReadableByteChannel rbc = Channels.newChannel(source);
			try {
				while (true) {
					int bytes = rbc.read(buffer);
					if (bytes == -1)
						break;
					if (buffer.remaining() == 0)
						buffer = resizeBuffer(buffer, buffer.capacity() * 2);
				}
				buffer.flip();
			} finally {
				rbc.close();
			}
		} finally {
			source.close();
		}
		return buffer;
	}
}
