package code;

import code.VLK.VLKContext;
import code.VLK.VLKDevice;
import code.VLK.VLKSwapChain;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.vulkan.*;

import java.nio.*;

import javax.imageio.stream.MemoryCacheImageInputStream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class Main {
	
	private static VkImageMemoryBarrier.Buffer createPrePresentBarrier(long presentImage) {
        VkImageMemoryBarrier.Buffer imageMemoryBarrier = VkImageMemoryBarrier.calloc(1)
                .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .pNext(NULL)
                .srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
                .dstAccessMask(0)
                .oldLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                .newLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
                .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        imageMemoryBarrier.subresourceRange()
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);
        imageMemoryBarrier.image(presentImage);
        return imageMemoryBarrier;
    }

    private static VkImageMemoryBarrier.Buffer createPostPresentBarrier(long presentImage) {
        VkImageMemoryBarrier.Buffer imageMemoryBarrier = VkImageMemoryBarrier.calloc(1)
                .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .pNext(NULL)
                .srcAccessMask(0)
                .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
                .oldLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
                .newLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        imageMemoryBarrier.subresourceRange()
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);
        imageMemoryBarrier.image(presentImage);
        return imageMemoryBarrier;
    }
	
    private static void submitCommandBuffer(VkQueue queue, VkCommandBuffer commandBuffer) {
        if (commandBuffer == null || commandBuffer.address() == NULL)
            return;
        VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
        PointerBuffer pCommandBuffers = memAllocPointer(1)
                .put(commandBuffer)
                .flip();
        submitInfo.pCommandBuffers(pCommandBuffers);
        int err = vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE);
        memFree(pCommandBuffers);
        submitInfo.free();
    }
    
    private static void submitPostPresentBarrier(long image, VkCommandBuffer commandBuffer, VkQueue queue) {
        VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .pNext(NULL);
        int err = vkBeginCommandBuffer(commandBuffer, cmdBufInfo);
        cmdBufInfo.free();
        
        

        err = vkEndCommandBuffer(commandBuffer);
        
        submitCommandBuffer(queue, commandBuffer);
    }
    
	public static void main(String[] args) {
		glfwInit();
		
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		long window = glfwCreateWindow(800, 600, "LD Game", 0, 0);
		
		VLKContext context = VLK.createContext(true);
		VLKDevice device = VLK.createDevice(context);
		VLKSwapChain swapChain = VLK.createSwapChain(context, device, window);
		
		IntBuffer pImageIndex = memAllocInt(1);
        int currentBuffer = 0;
        PointerBuffer pCommandBuffers = memAllocPointer(1);
        pCommandBuffers.put(0, device.commandBuffer);
        LongBuffer pSwapchains = memAllocLong(1);
        LongBuffer pImageAcquiredSemaphore = memAllocLong(1);
        LongBuffer pRenderCompleteSemaphore = memAllocLong(1);
        
        // Info struct to create a semaphore
        VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
                .pNext(NULL)
                .flags(VLK.VK_FLAGS_NONE);

        // Info struct to submit a command buffer which will wait on the semaphore
        IntBuffer pWaitDstStageMask = memAllocInt(1);
        pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(NULL)
                .waitSemaphoreCount(pImageAcquiredSemaphore.remaining())
                .pWaitSemaphores(pImageAcquiredSemaphore)
                .pWaitDstStageMask(pWaitDstStageMask)
                .pCommandBuffers(pCommandBuffers)
                .pSignalSemaphores(pRenderCompleteSemaphore);

        // Info struct to present the current swapchain image to the display
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc()
                .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                .pNext(NULL)
                .pWaitSemaphores(pRenderCompleteSemaphore)
                .swapchainCount(pSwapchains.remaining())
                .pSwapchains(pSwapchains)
                .pImageIndices(pImageIndex)
                .pResults(null);
        
        VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .pNext(NULL);

        // Specify clear color (cornflower blue)
        VkClearValue.Buffer clearValues = VkClearValue.calloc(1);
        clearValues.color()
                .float32(0, 100/255.0f)
                .float32(1, 149/255.0f)
                .float32(2, 237/255.0f)
                .float32(3, 1.0f);

        // Specify everything to begin a render pass
        VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                .pNext(NULL)
                .renderPass(swapChain.renderPass)
                .pClearValues(clearValues);
        VkRect2D renderArea = renderPassBeginInfo.renderArea();
        renderArea.offset()
                .x(0)
                .y(0);
        renderArea.extent()
                .width(800)
                .height(600);
		
		while(!glfwWindowShouldClose(window)) {
			glfwPollEvents();
			
			vkCreateSemaphore(device.device, semaphoreCreateInfo, null, pImageAcquiredSemaphore);
            vkCreateSemaphore(device.device, semaphoreCreateInfo, null, pRenderCompleteSemaphore);
			
            vkAcquireNextImageKHR(device.device, swapChain.swapChain, VLK.UINT64_MAX, pImageAcquiredSemaphore.get(0), VK_NULL_HANDLE, pImageIndex);
            currentBuffer = pImageIndex.get(0);
            
            renderPassBeginInfo.framebuffer(swapChain.framebuffers[pImageIndex.get(0)]);
            
            vkBeginCommandBuffer(device.commandBuffer, cmdBufInfo);
            
            VkImageMemoryBarrier.Buffer postPresentBarrier = createPostPresentBarrier(swapChain.images[pImageIndex.get(0)]);
            vkCmdPipelineBarrier(
                device.commandBuffer,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                VLK.VK_FLAGS_NONE,
                null, // No memory barriers,
                null, // No buffer barriers,
                postPresentBarrier); // one image barrier
            postPresentBarrier.free();
            
            vkCmdBeginRenderPass(device.commandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);
            
            VkViewport.Buffer viewport = VkViewport.calloc(1)
                    .height(600)
                    .width(800)
                    .minDepth(0.0f)
                    .maxDepth(1.0f);
            vkCmdSetViewport(device.commandBuffer, 0, viewport);
            viewport.free();

            // Update dynamic scissor state
            VkRect2D.Buffer scissor = VkRect2D.calloc(1);
            scissor.extent()
                    .width(800)
                    .height(600);
            scissor.offset()
                    .x(0)
                    .y(0);
            vkCmdSetScissor(device.commandBuffer, 0, scissor);
            scissor.free();

            vkCmdEndRenderPass(device.commandBuffer);
            
            VkImageMemoryBarrier.Buffer prePresentBarrier = createPrePresentBarrier(swapChain.images[pImageIndex.get(0)]);
            vkCmdPipelineBarrier(device.commandBuffer,
                VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                VLK.VK_FLAGS_NONE,
                null, // No memory barriers
                null, // No buffer memory barriers
                prePresentBarrier); // One image memory barrier
            prePresentBarrier.free();
            
            vkEndCommandBuffer(device.commandBuffer);
            
            vkQueueSubmit(device.queue, submitInfo, VK_NULL_HANDLE);
            
            pSwapchains.put(0, swapChain.swapChain);
            
            vkQueuePresentKHR(device.queue, presentInfo);
            
            vkQueueWaitIdle(device.queue);
            
            vkDestroySemaphore(device.device, pImageAcquiredSemaphore.get(0), null);
            vkDestroySemaphore(device.device, pRenderCompleteSemaphore.get(0), null);
		}
		
		renderPassBeginInfo.free();
        clearValues.free();
        cmdBufInfo.free();
		
		vkDestroyDebugReportCallbackEXT(context.instance, context.debug, null);
		
		glfwDestroyWindow(window);
		glfwTerminate();
	}
}
