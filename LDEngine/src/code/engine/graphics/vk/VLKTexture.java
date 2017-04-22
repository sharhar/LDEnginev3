package code.engine.graphics.vk;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;

import code.engine.graphics.Renderer;
import code.engine.graphics.Texture;

public class VLKTexture extends Texture{
	
	public VLKRenderer vrc;
	public long teximage;
	public long texmem;
	public long texsampler;
	public long texview;
	
	public VLKTexture(Renderer renderer, String path) {
		super(renderer, path);
	}
	
	public VLKTexture(Renderer renderer, BufferedImage img) {
		super(renderer, img);
	}

	protected void init() {
		vrc = (VLKRenderer)renderer;
		
		VkImageCreateInfo image_create_info_staging = VkImageCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
				.pNext(NULL)
				.imageType(VK_IMAGE_TYPE_2D)
				.format(VK_FORMAT_B8G8R8A8_UNORM)
				.mipLevels(1)
				.arrayLayers(1)
				.samples(VK_SAMPLE_COUNT_1_BIT)
				.tiling(VK_IMAGE_TILING_LINEAR)
				.usage(VK_IMAGE_USAGE_TRANSFER_SRC_BIT)
				.initialLayout(VK_IMAGE_LAYOUT_PREINITIALIZED)
				.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
				.flags(0);
		image_create_info_staging.extent()
				.width(this.width)
				.height(this.height)
				.depth(1);
		
		LongBuffer lp = memAllocLong(1);
		VLK.VLKCheck(vkCreateImage(vrc.device.device, image_create_info_staging, null, lp), 
				"Failed to create Image");
		image_create_info_staging.free();
		long stagingImage = lp.get(0);
		
		VkMemoryRequirements mem_reqs = VkMemoryRequirements.malloc();
		vkGetImageMemoryRequirements(vrc.device.device, stagingImage, mem_reqs);
		VkMemoryAllocateInfo mem_alloc = VkMemoryAllocateInfo.malloc()
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
			.pNext(NULL)
			.allocationSize(mem_reqs.size())
			.memoryTypeIndex(0);
		
		int typeBits = mem_reqs.memoryTypeBits();
		
		for ( int i = 0; i < 32; i++ ) {
			if ( (typeBits & 1) == 1 ) {
				if ( (vrc.device.memoryProperties.memoryTypes().get(i).propertyFlags() 
						& (VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)) 
						== (VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT) ) {
					mem_alloc.memoryTypeIndex(i);
					break;
				}
			}
			typeBits >>= 1;
		}
		
		mem_reqs.free();
		
		vkAllocateMemory(vrc.device.device, mem_alloc, null, lp);
		long stagingMem = lp.get(0);

	    vkBindImageMemory(vrc.device.device, stagingImage, stagingMem, 0);
	    
	    VkImageSubresource subres = VkImageSubresource.malloc()
			.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
			.mipLevel(0)
			.arrayLayer(0);

		VkSubresourceLayout layout = VkSubresourceLayout.malloc();
		vkGetImageSubresourceLayout(vrc.device.device, stagingImage, subres, layout);
		subres.free();

		PointerBuffer pp = memAllocPointer(1);
		vkMapMemory(vrc.device.device, stagingMem, 0, mem_alloc.allocationSize(), 0, pp);
		
		for ( int y = 0; y < height; y++ ) {
			IntBuffer row = memIntBuffer(pp.get(0) + layout.rowPitch() * y, width);
			for ( int x = 0; x < width; x++ )
				row.put(x, this.data[y * width + x]);
		}
		
		layout.free();

		vkUnmapMemory(vrc.device.device, stagingMem);
		
		mem_alloc.free();
		
		VkImageCreateInfo image_create_info = VkImageCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
				.pNext(NULL)
				.imageType(VK_IMAGE_TYPE_2D)
				.format(VK_FORMAT_B8G8R8A8_UNORM)
				.mipLevels(1)
				.arrayLayers(1)
				.samples(VK_SAMPLE_COUNT_1_BIT)
				.tiling(VK_IMAGE_TILING_OPTIMAL)
				.usage(VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT)
				.initialLayout(VK_IMAGE_LAYOUT_PREINITIALIZED)
				.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
				.flags(0);
		image_create_info.extent()
				.width(this.width)
				.height(this.height)
				.depth(1);
		
		VLK.VLKCheck(vkCreateImage(vrc.device.device, image_create_info, null, lp), 
				"Failed to create Image");
		image_create_info.free();
		teximage = lp.get(0);
		
		mem_reqs = VkMemoryRequirements.malloc();
		vkGetImageMemoryRequirements(vrc.device.device, teximage, mem_reqs);
		mem_alloc = VkMemoryAllocateInfo.malloc()
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
			.pNext(NULL)
			.allocationSize(mem_reqs.size())
			.memoryTypeIndex(0);
		
		typeBits = mem_reqs.memoryTypeBits();
		
		for ( int i = 0; i < 32; i++ ) {
			if ( (typeBits & 1) == 1 ) {
				if ( (vrc.device.memoryProperties.memoryTypes().get(i).propertyFlags() 
						& VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) 
						== VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT ) {
					mem_alloc.memoryTypeIndex(i);
					break;
				}
			}
			typeBits >>= 1;
		}
		
		mem_reqs.free();
		
		vkAllocateMemory(vrc.device.device, mem_alloc, null, lp);
		texmem = lp.get(0);

	    vkBindImageMemory(vrc.device.device, teximage, texmem, 0);
		
		VkImageMemoryBarrier.Buffer image_memory_barrier_src = VkImageMemoryBarrier.malloc(1)
				.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
				.pNext(NULL)
				.srcAccessMask(VK_ACCESS_HOST_WRITE_BIT)
				.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT)
				.oldLayout(VK_IMAGE_LAYOUT_PREINITIALIZED)
				.newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
				.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.image(stagingImage);

		image_memory_barrier_src.subresourceRange()
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1);
		
		VkImageMemoryBarrier.Buffer image_memory_barrier_dst = VkImageMemoryBarrier.malloc(1)
				.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
				.pNext(NULL)
				.srcAccessMask(VK_ACCESS_HOST_WRITE_BIT)
				.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
				.oldLayout(VK_IMAGE_LAYOUT_PREINITIALIZED)
				.newLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.image(teximage);
		image_memory_barrier_dst.subresourceRange()
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1);
		
		VkImageMemoryBarrier.Buffer image_memory_barrier_read = VkImageMemoryBarrier.malloc(1)
				.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
				.pNext(NULL)
				.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
				.dstAccessMask(VK_ACCESS_SHADER_READ_BIT)
				.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
				.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.image(teximage);
		image_memory_barrier_read.subresourceRange()
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1);
		
		VkImageSubresourceLayers subresource = VkImageSubresourceLayers.calloc()
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.baseArrayLayer(0)
				.mipLevel(0)
				.layerCount(1);
		
		VkImageCopy.Buffer region = VkImageCopy.calloc(1)
				.srcSubresource(subresource)
				.dstSubresource(subresource)
				.srcOffset(VkOffset3D.calloc()
						.x(0).y(0).z(0))
				.dstOffset(VkOffset3D.calloc()
						.x(0).y(0).z(0))
				.extent(VkExtent3D.calloc()
						.width(width).height(height).depth(1));
		
		VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
				.pNext(NULL);
		
		vkBeginCommandBuffer(vrc.device.commandBuffer, cmdBufInfo);
			
		vkCmdPipelineBarrier(vrc.device.commandBuffer, 
				VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, 
				VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, 
				0, null, null, image_memory_barrier_src);
		
		vkCmdPipelineBarrier(vrc.device.commandBuffer, 
				VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, 
				VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, 
				0, null, null, image_memory_barrier_dst);
		
		vkCmdCopyImage(vrc.device.commandBuffer, 
				stagingImage, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, 
				teximage, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 
				region);
		
		vkCmdPipelineBarrier(vrc.device.commandBuffer, 
				VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, 
				VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, 
				0, null, null, image_memory_barrier_read);
		
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
        
        VkSamplerCreateInfo samplerCreate = VkSamplerCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
			.pNext(NULL)
			.magFilter(VK_FILTER_LINEAR)
			.minFilter(VK_FILTER_LINEAR)
			.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR)
			.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT)
			.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT)
			.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
			.mipLodBias(0.0f)
			.anisotropyEnable(false)
			.maxAnisotropy(1)
			.compareOp(VK_COMPARE_OP_NEVER)
			.minLod(0.0f)
			.maxLod(0.0f)
			.borderColor(VK_BORDER_COLOR_INT_OPAQUE_WHITE)
			.unnormalizedCoordinates(false);
        
        VLK.VLKCheck(vkCreateSampler(vrc.device.device, samplerCreate, null, lp), 
        		"Failed to create Sampler");
        samplerCreate.free();
		texsampler = lp.get(0);
		
		VkImageViewCreateInfo viewCreate = VkImageViewCreateInfo.malloc()
			.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
			.pNext(NULL)
			.image(teximage)
			.viewType(VK_IMAGE_VIEW_TYPE_2D)
			.format(VK_FORMAT_B8G8R8A8_UNORM)
			.flags(0);
		viewCreate.components()
			.r(VK_COMPONENT_SWIZZLE_R)
			.g(VK_COMPONENT_SWIZZLE_G)
			.b(VK_COMPONENT_SWIZZLE_B)
			.a(VK_COMPONENT_SWIZZLE_A);
		viewCreate.subresourceRange()
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1);
			
		VLK.VLKCheck(vkCreateImageView(vrc.device.device, viewCreate, null, lp), 
				"Failed ot create image view");
		viewCreate.free();
		
		texview = lp.get(0);
	}
}
