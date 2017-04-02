package code.engine.graphics.vk;

import code.engine.graphics.Renderer;
import code.engine.graphics.Shader;
import code.engine.utils.FileUtils;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.vulkan.*;

public class VLKShader extends Shader {
	public long vertShader;
	public long fragShader;
	public long pipeline;
	public long layout;
	public long descriptorSetLayout;
	public VLKRenderer vrc;

	public VLKShader(Renderer renderer, String vertPath, String fragPath) {
		super(renderer, vertPath, fragPath);
	}

	protected void init() {
		vrc = (VLKRenderer) renderer;

		ByteBuffer shaderCode = null;
		try {
			shaderCode = FileUtils.ioResourceToByteBuffer(vertPath, 10024);
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
			shaderCode = FileUtils.ioResourceToByteBuffer(fragPath, 10024);
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
        
        VkDescriptorSetLayoutBinding.Buffer layoutBinding = VkDescriptorSetLayoutBinding.calloc(3);
        layoutBinding.get(0)
                .binding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1)
                .stageFlags(VK_SHADER_STAGE_VERTEX_BIT)
                .pImmutableSamplers(null);
        
        layoutBinding.get(1)
        		.binding(1)
        		.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
        		.descriptorCount(1)
        		.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT)
        		.pImmutableSamplers(null);
        
        layoutBinding.get(2)
			.binding(2)
			.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1)
			.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT)
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
        
		VkVertexInputBindingDescription.Buffer bindingDescriptor = VkVertexInputBindingDescription.calloc(1).binding(0)
				.stride(4 * 4)
				.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

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
				.blendEnable(true)
				.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT)
				.srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA)
				.dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
				.colorBlendOp(VK_BLEND_OP_ADD)
				.srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
				.dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
				.alphaBlendOp(VK_BLEND_OP_ADD);
		
		VkPipelineColorBlendStateCreateInfo colorBlendState = VkPipelineColorBlendStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
				.pAttachments(colorWriteMask);

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
		
	}

	public void unbind() {

	}
}
