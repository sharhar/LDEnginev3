package code.engine.graphics.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import code.engine.graphics.Model;
import code.engine.graphics.Renderer;

import java.nio.*;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

public class VLKModel extends Model {
	public long buffer;
	public long buffermem;
	public VLKRenderer vrc;
	public LongBuffer offsets;
	public LongBuffer pBuffers;
	public int vertCount;
	
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
				.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT)
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
		VLK.getMemoryType(vrc.device.memoryProperties, memReqs.memoryTypeBits(), VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, memoryTypeIndex);
		memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
		memFree(memoryTypeIndex);
		memReqs.free();

		LongBuffer pMemory = memAllocLong(1);
		vkAllocateMemory(vrc.device.device, memAlloc, null, pMemory);
		buffermem = pMemory.get(0);
		memFree(pMemory);

		vkBindBufferMemory(vrc.device.device, buffer, buffermem, 0);

		memAlloc = VkMemoryAllocateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
				.pNext(NULL)
				.allocationSize(0)
				.memoryTypeIndex(0);
		memReqs = VkMemoryRequirements.calloc();
		
		bufInfo = VkBufferCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
				.pNext(NULL)
				.size(vertexBuffer.remaining())
				.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
				.flags(0);
		pBuffer = memAllocLong(1);
		VLK.VLKCheck(vkCreateBuffer(vrc.device.device, bufInfo, null, pBuffer), 
				"Failed to create buffer");
		long stagingbuffer = pBuffer.get(0);
		memFree(pBuffer);
		bufInfo.free();

		vkGetBufferMemoryRequirements(vrc.device.device, stagingbuffer, memReqs);
		memAlloc.allocationSize(memReqs.size());
		memoryTypeIndex = memAllocInt(1);
		VLK.getMemoryType(vrc.device.memoryProperties, memReqs.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, memoryTypeIndex);
		memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
		memFree(memoryTypeIndex);
		memReqs.free();

		pMemory = memAllocLong(1);
		vkAllocateMemory(vrc.device.device, memAlloc, null, pMemory);
		long stagingbuffermem = pMemory.get(0);
		memFree(pMemory);

		vkBindBufferMemory(vrc.device.device, stagingbuffer, stagingbuffermem, 0);
		
		PointerBuffer pData = memAllocPointer(1);
		vkMapMemory(vrc.device.device, stagingbuffermem, 0, memAlloc.allocationSize(), 0, pData);
		memAlloc.free();
		long mapped = pData.get(0);
		memFree(pData);

		MemoryUtil.memCopy(memAddress(vertexBuffer), mapped, vertexBuffer.remaining());
		memFree(vertexBuffer);
		vkUnmapMemory(vrc.device.device, stagingbuffermem);
		
		
		VkBufferCopy.Buffer bufferCopy = VkBufferCopy.calloc(1)
				.dstOffset(0)
				.srcOffset(0)
				.size(vertexBuffer.remaining());
		
		VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
				.pNext(NULL);
		
		vkBeginCommandBuffer(vrc.device.commandBuffer, cmdBufInfo);
		
		vkCmdCopyBuffer(vrc.device.commandBuffer, 
				stagingbuffer, buffer, 
				bufferCopy);
		
		vkEndCommandBuffer(vrc.device.commandBuffer);
		
		IntBuffer pWaitDstStageMask = memAllocInt(1);
        pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(NULL)
                .waitSemaphoreCount(0)
                .pWaitSemaphores(null)
                .pWaitDstStageMask(pWaitDstStageMask)
                .pCommandBuffers(vrc.device.pCommandBuffers)
                .pSignalSemaphores(null);
        vkQueueSubmit(vrc.device.queue, submitInfo, VK_NULL_HANDLE);
        vkQueueWaitIdle(vrc.device.queue);
        
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
