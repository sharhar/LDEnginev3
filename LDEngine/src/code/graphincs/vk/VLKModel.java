package code.graphincs.vk;

import code.graphincs.Model;
import code.graphincs.Renderer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.*;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

public class VLKModel extends Model {
	long buffer;
	VLKRenderer vrc;
	LongBuffer offsets;
	LongBuffer pBuffers;
	int vertCount;
	
	public VLKModel(Renderer renderer, float[] data) {
		super(renderer, data);
	}

	protected void init() {
		vrc = (VLKRenderer)renderer;
		
		ByteBuffer vertexBuffer = memAlloc(data.length * 4);
		FloatBuffer fb = vertexBuffer.asFloatBuffer();
		fb.put(data);

		VkMemoryAllocateInfo memAlloc = VkMemoryAllocateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
				.pNext(NULL)
				.allocationSize(0)
				.memoryTypeIndex(0);
		VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		
		VkBufferCreateInfo bufInfo = VkBufferCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
				.pNext(NULL)
				.size(vertexBuffer.remaining())
				.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
				.flags(0);
		LongBuffer pBuffer = memAllocLong(1);
		VLK.VLKCheck(vkCreateBuffer(vrc.device.device, bufInfo, null, pBuffer), 
				"Failed to create buffer");
		buffer = pBuffer.get(0);
		memFree(pBuffer);
		bufInfo.free();

		vkGetBufferMemoryRequirements(vrc.device.device, buffer, memReqs);
		memAlloc.allocationSize(memReqs.size());
		IntBuffer memoryTypeIndex = memAllocInt(1);
		VLK.getMemoryType(vrc.device.memoryProperties, memReqs.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, memoryTypeIndex);
		memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
		memFree(memoryTypeIndex);
		memReqs.free();

		LongBuffer pMemory = memAllocLong(1);
		vkAllocateMemory(vrc.device.device, memAlloc, null, pMemory);
		long verticesMem = pMemory.get(0);
		memFree(pMemory);

		PointerBuffer pData = memAllocPointer(1);
		vkMapMemory(vrc.device.device, verticesMem, 0, memAlloc.allocationSize(), 0, pData);
		memAlloc.free();
		long mapped = pData.get(0);
		memFree(pData);

		MemoryUtil.memCopy(memAddress(vertexBuffer), mapped, vertexBuffer.remaining());
		memFree(vertexBuffer);
		vkUnmapMemory(vrc.device.device, verticesMem);
		vkBindBufferMemory(vrc.device.device, buffer, verticesMem, 0);
		
		offsets = memAllocLong(1);
        offsets.put(0, 0);
        pBuffers = memAllocLong(1);
        pBuffers.put(0, buffer);
        
        vertCount = data.length;
	}

	public void bind() {
		vkCmdBindVertexBuffers(vrc.device.commandBuffer, 0, pBuffers, offsets);
	}

	public void unbind() {

	}

	public void draw() {
		vkCmdDraw(vrc.device.commandBuffer, vertCount, 1, 0, 0);
	}
}
