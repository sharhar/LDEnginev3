package code.graphincs.vk;

import code.graphincs.Renderer;
import code.graphincs.Shader;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;

public class VLKShader extends Shader {

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

	long vertShader;
	long fragShader;
	long pipeline;
	long layout;
	long descriptorPool;
	long descriptorSet;
	long uniformmemory;
    long uniformallocationSize;
    long uniformbuffer;
    long descriptorSetLayout;
	VLKRenderer vrc;

	public VLKShader(Renderer renderer, String vertPath, String fragPath) {
		super(renderer, vertPath, fragPath);
	}

	protected void init() {
		vrc = (VLKRenderer) renderer;

		ByteBuffer shaderCode = null;
		try {
			shaderCode = ioResourceToByteBuffer(vertPath, 10024);
		} catch (IOException e) {
			e.printStackTrace();
		}

		VkShaderModuleCreateInfo moduleCreateInfo = VkShaderModuleCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO).pNext(NULL).pCode(shaderCode).flags(0);
		LongBuffer pShaderModule = memAllocLong(1);
		VLK.VLKCheck(vkCreateShaderModule(vrc.device.device, moduleCreateInfo, null, pShaderModule),
				"Failed to create shader");
		vertShader = pShaderModule.get(0);
		memFree(pShaderModule);

		try {
			shaderCode = ioResourceToByteBuffer(fragPath, 10024);
		} catch (IOException e) {
			e.printStackTrace();
		}

		moduleCreateInfo = VkShaderModuleCreateInfo.calloc().sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
				.pNext(NULL).pCode(shaderCode).flags(0);
		pShaderModule = memAllocLong(1);
		VLK.VLKCheck(vkCreateShaderModule(vrc.device.device, moduleCreateInfo, null, pShaderModule),
				"Failed to create shader");
		fragShader = pShaderModule.get(0);
		memFree(pShaderModule);

		VkDescriptorPoolSize.Buffer typeCounts = VkDescriptorPoolSize.calloc(1)
				.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER).descriptorCount(1);
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
        VLK.getMemoryType(vrc.device.memoryProperties, memoryTypeBits, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, pMemoryTypeIndex);
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
        
        VkDescriptorSetLayoutBinding.Buffer layoutBinding = VkDescriptorSetLayoutBinding.calloc(1)
                .binding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1)
                .stageFlags(VK_SHADER_STAGE_VERTEX_BIT)
                .pImmutableSamplers(null);
        VkDescriptorSetLayoutCreateInfo descriptorLayout = VkDescriptorSetLayoutCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                .pNext(NULL)
                .pBindings(layoutBinding);

        LongBuffer pDescriptorSetLayout = memAllocLong(1);
        VLK.VLKCheck(vkCreateDescriptorSetLayout(vrc.device.device, descriptorLayout, null, pDescriptorSetLayout), 
        		"Failed to create descriptor set layout");
        descriptorSetLayout = pDescriptorSetLayout.get(0);
        memFree(pDescriptorSetLayout);
        descriptorLayout.free();
        layoutBinding.free();
        
        pDescriptorSetLayout = memAllocLong(1);
        pDescriptorSetLayout.put(0, descriptorSetLayout);
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
        writeDescriptorSet.free();
        descriptor.free();
        
        float[] mats = {
        		1, 0, 0, 0,
        		0, 1, 0, 0,
        		0, 0, 1, 0,
        		0, 0, 0, 1,
        		
        		1, 0, 0, 0,
        		0, 1, 0, 0,
        		0, 0, 1, 0,
        		0, 0, 0, 1
        };
        
        PointerBuffer pData = memAllocPointer(1);
        VLK.VLKCheck(vkMapMemory(vrc.device.device, uniformmemory, 0, uniformallocationSize, 0, pData), 
        		"Failed to map memory");
        long data = pData.get(0);
        memFree(pData);
        FloatBuffer matrixBuffer = memFloatBuffer(data, 16 * 2);
        matrixBuffer.put(mats).flip();
        vkUnmapMemory(vrc.device.device, uniformmemory);
        
		VkVertexInputBindingDescription.Buffer bindingDescriptor = VkVertexInputBindingDescription.calloc(1).binding(0)
				.stride(4 * 4).inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

		VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2);
		attributeDescriptions.get(0).binding(0).location(0).format(VK_FORMAT_R32G32_SFLOAT).offset(0);
		attributeDescriptions.get(1).binding(0).location(1).format(VK_FORMAT_R32G32_SFLOAT).offset(2 * 4);

		VkPipelineVertexInputStateCreateInfo vi = VkPipelineVertexInputStateCreateInfo.calloc();
		vi.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
		vi.pNext(NULL);
		vi.pVertexBindingDescriptions(bindingDescriptor);
		vi.pVertexAttributeDescriptions(attributeDescriptions);

		VkPipelineInputAssemblyStateCreateInfo inputAssemblyState = VkPipelineInputAssemblyStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
				.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);

		VkPipelineRasterizationStateCreateInfo rasterizationState = VkPipelineRasterizationStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO).polygonMode(VK_POLYGON_MODE_FILL)
				.cullMode(VK_CULL_MODE_NONE).frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE).depthClampEnable(false)
				.rasterizerDiscardEnable(false).depthBiasEnable(false);

		VkPipelineColorBlendAttachmentState.Buffer colorWriteMask = VkPipelineColorBlendAttachmentState.calloc(1)
				.blendEnable(false).colorWriteMask(0xF);
		VkPipelineColorBlendStateCreateInfo colorBlendState = VkPipelineColorBlendStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO).pAttachments(colorWriteMask);

		VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO).viewportCount(1).scissorCount(1);

		IntBuffer pDynamicStates = memAllocInt(2);
		pDynamicStates.put(VK_DYNAMIC_STATE_VIEWPORT).put(VK_DYNAMIC_STATE_SCISSOR).flip();
		VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO).pDynamicStates(pDynamicStates);

		VkPipelineDepthStencilStateCreateInfo depthStencilState = VkPipelineDepthStencilStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO).depthTestEnable(false)
				.depthWriteEnable(false).depthCompareOp(VK_COMPARE_OP_ALWAYS).depthBoundsTestEnable(false)
				.stencilTestEnable(false);
		depthStencilState.back().failOp(VK_STENCIL_OP_KEEP).passOp(VK_STENCIL_OP_KEEP).compareOp(VK_COMPARE_OP_ALWAYS);
		depthStencilState.front(depthStencilState.back());

		VkPipelineMultisampleStateCreateInfo multisampleState = VkPipelineMultisampleStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO).pSampleMask(null)
				.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

		VkPipelineShaderStageCreateInfo vertShaderStage = VkPipelineShaderStageCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO).stage(VK_SHADER_STAGE_VERTEX_BIT)
				.module(vertShader).pName(memUTF8("main"));

		VkPipelineShaderStageCreateInfo fragShaderStage = VkPipelineShaderStageCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO).stage(VK_SHADER_STAGE_FRAGMENT_BIT)
				.module(fragShader).pName(memUTF8("main"));

		VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2);
		shaderStages.get(0).set(vertShaderStage);
		shaderStages.get(1).set(fragShaderStage);

		pDescriptorSetLayout = memAllocLong(1).put(0, descriptorSetLayout);
		VkPipelineLayoutCreateInfo pPipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
				.pNext(NULL)
				.pSetLayouts(pDescriptorSetLayout);
		LongBuffer pPipelineLayout = memAllocLong(1);
		VLK.VLKCheck(vkCreatePipelineLayout(vrc.device.device, pPipelineLayoutCreateInfo, null, pPipelineLayout),
				"Failed to create Pipeline Layout");
		layout = pPipelineLayout.get(0);
		memFree(pPipelineLayout);
		memFree(pDescriptorSetLayout);
		pPipelineLayoutCreateInfo.free();

		VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1)
				.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO).layout(layout)
				.renderPass(vrc.swapChain.renderPass).pVertexInputState(vi).pInputAssemblyState(inputAssemblyState)
				.pRasterizationState(rasterizationState).pColorBlendState(colorBlendState)
				.pMultisampleState(multisampleState).pViewportState(viewportState).pDepthStencilState(depthStencilState)
				.pStages(shaderStages).pDynamicState(dynamicState);

		LongBuffer pPipelines = memAllocLong(1);
		VLK.VLKCheck(vkCreateGraphicsPipelines(vrc.device.device, VK_NULL_HANDLE, pipelineCreateInfo, null, pPipelines),
				"Failed to create pipeline");
		pipeline = pPipelines.get(0);
		shaderStages.free();
		multisampleState.free();
		depthStencilState.free();
		dynamicState.free();
		memFree(pDynamicStates);
		viewportState.free();
		colorBlendState.free();
		colorWriteMask.free();
		rasterizationState.free();
		inputAssemblyState.free();
	}

	public void bind() {
		vkCmdBindPipeline(vrc.device.commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
		LongBuffer descriptorSets = memAllocLong(1).put(0, descriptorSet);
		vkCmdBindDescriptorSets(vrc.device.commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, layout, 0, descriptorSets, null);
		memFree(descriptorSets);
	}

	public void unbind() {

	}
}
