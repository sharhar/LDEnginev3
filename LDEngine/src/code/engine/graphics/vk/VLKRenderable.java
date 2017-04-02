package code.engine.graphics.vk;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;

import code.engine.graphics.Model;
import code.engine.graphics.Renderable;
import code.engine.graphics.Renderer;
import code.engine.graphics.Shader;
import code.engine.graphics.Texture;
import code.engine.math.Vector2f;

public class VLKRenderable extends Renderable{
	
	public VLKRenderer vrc;
	public VLKShader vshd;
	public VLKTexture vtex;
	public VLKModel vmodel;
	public long descriptorPool;
	public long descriptorSet;
	public long uniformmemory;
	public long uniformallocationSize;
	public long uniformbuffer;
	
	public VLKRenderable(Renderer renderer, Model model, Shader shader, Vector2f pos, float rot, Vector2f size, Texture texture) {
		super(renderer, model, shader, pos, rot, size, texture);
	}

	protected void init() {
		vrc = (VLKRenderer)renderer;
		vshd = (VLKShader)shader;
		vtex = (VLKTexture)texture;
		vmodel = (VLKModel)model;
		
		VkDescriptorPoolSize.Buffer typeCounts = VkDescriptorPoolSize.calloc(1)
				.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
				.descriptorCount(1);
		VkDescriptorPoolCreateInfo descriptorPoolInfo = VkDescriptorPoolCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(NULL)
				.pPoolSizes(typeCounts)
				.maxSets(1);

		LongBuffer pDescriptorPool = memAllocLong(1);
		VLK.VLKCheck(vkCreateDescriptorPool(vrc.device.device, descriptorPoolInfo, null, pDescriptorPool), 
				"Failed to create descriptor pool");
		descriptorPool = pDescriptorPool.get(0);
		memFree(pDescriptorPool);
		descriptorPoolInfo.free();
		typeCounts.free();

		VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .size(16 * 4 * 2)
                .usage(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
        LongBuffer pUniformDataVSBuffer = memAllocLong(1);
        VLK.VLKCheck(vkCreateBuffer(vrc.device.device, bufferInfo, null, pUniformDataVSBuffer), 
        		"Failed to create buffer");
        uniformbuffer = pUniformDataVSBuffer.get(0);
        memFree(pUniformDataVSBuffer);
        bufferInfo.free();
		
        VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
        vkGetBufferMemoryRequirements(vrc.device.device, uniformbuffer, memReqs);
        uniformallocationSize = memReqs.size();
        int memoryTypeBits = memReqs.memoryTypeBits();
        memReqs.free();
        
        IntBuffer pMemoryTypeIndex = memAllocInt(1);
        VLK.getMemoryType(vrc.device.memoryProperties, memoryTypeBits, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, pMemoryTypeIndex);
        int memoryTypeIndex = pMemoryTypeIndex.get(0);
        memFree(pMemoryTypeIndex);
        
        LongBuffer pUniformDataVSMemory = memAllocLong(1);
        VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                .pNext(NULL)
                .allocationSize(uniformallocationSize)
                .memoryTypeIndex(memoryTypeIndex);
        vkAllocateMemory(vrc.device.device, allocInfo, null, pUniformDataVSMemory);
        uniformmemory = pUniformDataVSMemory.get(0);
        memFree(pUniformDataVSMemory);
        allocInfo.free();
        
        vkBindBufferMemory(vrc.device.device, uniformbuffer, uniformmemory, 0);
        
        LongBuffer pDescriptorSetLayout = memAllocLong(1);
        pDescriptorSetLayout.put(0, vshd.descriptorSetLayout);
        VkDescriptorSetAllocateInfo descAllocInfo = VkDescriptorSetAllocateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                .descriptorPool(descriptorPool)
                .pSetLayouts(pDescriptorSetLayout);

        LongBuffer pDescriptorSet = memAllocLong(1);
        VLK.VLKCheck(vkAllocateDescriptorSets(vrc.device.device, descAllocInfo, pDescriptorSet), 
        		"Failed to allocate descriptor set");
        descriptorSet = pDescriptorSet.get(0);
        memFree(pDescriptorSet);
        descAllocInfo.free();
        memFree(pDescriptorSetLayout);

        VkDescriptorBufferInfo.Buffer descriptor = VkDescriptorBufferInfo.calloc(1)
                .buffer(uniformbuffer)
                .range(16 * 4 * 2)
                .offset(0);
        
        VkWriteDescriptorSet.Buffer writeDescriptorSet = VkWriteDescriptorSet.calloc(1)
                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(descriptorSet)
                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .pBufferInfo(descriptor)
                .dstBinding(0);
        vkUpdateDescriptorSets(vrc.device.device, writeDescriptorSet, null);
        
        descriptor.free();
        
        long window = vrc.window;
		
		int[] widthArr = new int[1];
		int[] heightArr = new int[1];
		glfwGetWindowSize(window, widthArr, heightArr);
		
		float r = widthArr[0];
		float l = 0;
		float t = heightArr[0];
		float b = 0;
		float f = 1;
		float n = -1;
        
        float[] mats = {
        		1, 0, 0, 0,
        		0, 1, 0, 0,
        		0, 0, 1, 0,
        		0, 0, 0, 1,
        		
        		2/(r - l), 0, 0, 0,
    			0, -2/(t - b), 0, 0,
    			0, 0, -2/(f - n), 0,
    			-(r + l) / (r - l), (t + b) / (t - b), -(f + n) / (f - n), 1
        };
        
        PointerBuffer pData = memAllocPointer(1);
        VLK.VLKCheck(vkMapMemory(vrc.device.device, uniformmemory, 0, uniformallocationSize, 0, pData), 
        		"Failed to map memory");
        long data = pData.get(0);
        memFree(pData);
        FloatBuffer matrixBuffer = memFloatBuffer(data, 16 * 2);
        matrixBuffer.put(mats).flip();
        vkUnmapMemory(vrc.device.device, uniformmemory);
        
        VkDescriptorImageInfo.Buffer tex_desc = VkDescriptorImageInfo.calloc(1)
				.sampler(vtex.texsampler)
				.imageView(vtex.texview)
				.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        
        VkWriteDescriptorSet.Buffer tex_write = VkWriteDescriptorSet.calloc(1)
    			.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
    			.dstSet(descriptorSet)
    			.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
    			.pImageInfo(tex_desc)
    			.dstBinding(1)
    			.dstArrayElement(0);

    	vkUpdateDescriptorSets(vrc.device.device, tex_write, null);
    	
    	writeDescriptorSet.free();
	}

	public void applyUniforms() {
		PointerBuffer pData = memAllocPointer(1);
        VLK.VLKCheck(vkMapMemory(vrc.device.device, uniformmemory, 0, uniformallocationSize, 0, pData), 
        		"Failed to map memory");
        long data = pData.get(0);
        memFree(pData);
        FloatBuffer matrixBuffer = memFloatBuffer(data, 16);
        matrixBuffer.put(modelview).flip();
        vkUnmapMemory(vrc.device.device, uniformmemory);
		
		LongBuffer descriptorSets = memAllocLong(1).put(0, descriptorSet);
		vkCmdBindDescriptorSets(vrc.device.commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, vshd.layout, 0, descriptorSets, null);
		memFree(descriptorSets);
	}
	
	public void destroy() {
		vkDestroyBuffer(vrc.device.device, vmodel.buffer, null);
		vkFreeMemory(vrc.device.device, vmodel.buffermem, null);
	}
}
