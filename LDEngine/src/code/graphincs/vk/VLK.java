package code.graphincs.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;

import code.math.Vector4f;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class VLK {

	private static final boolean validation = Boolean.parseBoolean(System.getProperty("vulkan.validation", "false"));

	private static ByteBuffer[] layers = { memUTF8("VK_LAYER_LUNARG_standard_validation"), };

	public static final int VK_FLAGS_NONE = 0;
	public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

	public static void VLKCheck(int result, String message) {
		if (result != VK_SUCCESS) {
			throw new IllegalStateException(message);
		}
	}

	public static class VLKContext {
		public VkInstance instance;
		public long debug;
	}

	public static class VLKDevice {
		public VkPhysicalDevice physicalDevice;
		public VkDevice device;
		public int queueFamilyIndex;

		public long commandPool;
		public VkQueue queue;
		public VkCommandBuffer commandBuffer;
		public VkPhysicalDeviceMemoryProperties memoryProperties;
		
		public PointerBuffer pCommandBuffers = memAllocPointer(1);
	}

	public static class VLKSwapChain {
		public long surface;
		public int colorFormat;
		public int colorSpace;
		public long swapChain;
		public long[] images;
		public long[] imageViews;
		public long[] framebuffers;
		public long renderPass;
		
		LongBuffer pSwapchains = memAllocLong(1);
        LongBuffer pImageAcquiredSemaphore = memAllocLong(1);
        LongBuffer pRenderCompleteSemaphore = memAllocLong(1);
        IntBuffer pImageIndex = memAllocInt(1);
	}
	
	public static boolean getMemoryType(VkPhysicalDeviceMemoryProperties deviceMemoryProperties, int typeBits, int properties, IntBuffer typeIndex) {
        int bits = typeBits;
        for (int i = 0; i < 32; i++) {
            if ((bits & 1) == 1) {
                if ((deviceMemoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                    typeIndex.put(0, i);
                    return true;
                }
            }
            bits >>= 1;
        }
        return false;
    }

	public static VLKContext createContext(boolean debug) {
		PointerBuffer requiredExtensions = glfwGetRequiredInstanceExtensions();
		ByteBuffer VK_EXT_DEBUG_REPORT_EXTENSION = memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
		PointerBuffer ppEnabledExtensionNames = memAllocPointer(requiredExtensions.remaining() + 1);
		ppEnabledExtensionNames.put(requiredExtensions).put(VK_EXT_DEBUG_REPORT_EXTENSION).flip();

		PointerBuffer ppEnabledLayerNames = memAllocPointer(layers.length);
		for (int i = 0; validation && i < layers.length; i++) {
			ppEnabledLayerNames.put(layers[i]);
		}
		ppEnabledLayerNames.flip();

		VkApplicationInfo appInfo = VkApplicationInfo.calloc().sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
				.pApplicationName(memUTF8("GLFW Vulkan Demo")).pEngineName(memUTF8("LDEnginev3"))
				.apiVersion(VK_MAKE_VERSION(1, 0, 2));

		VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.calloc().sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
				.pNext(NULL).pApplicationInfo(appInfo).ppEnabledExtensionNames(ppEnabledExtensionNames)
				.ppEnabledLayerNames(ppEnabledLayerNames);

		PointerBuffer pInstance = memAllocPointer(1);

		VLKCheck(vkCreateInstance(pCreateInfo, null, pInstance), "Could not create instance");

		long instancePointer = pInstance.get(0);
		memFree(pInstance);

		VkInstance instance = new VkInstance(instancePointer, pCreateInfo);

		pCreateInfo.free();
		memFree(ppEnabledLayerNames);
		memFree(VK_EXT_DEBUG_REPORT_EXTENSION);
		memFree(ppEnabledExtensionNames);
		memFree(appInfo.pApplicationName());
		memFree(appInfo.pEngineName());
		appInfo.free();

		VLKContext context = new VLKContext();

		context.instance = instance;

		if (debug) {
			VkDebugReportCallbackCreateInfoEXT dbgCreateInfo = VkDebugReportCallbackCreateInfoEXT.calloc()
					.sType(VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT).pNext(NULL)
					.pfnCallback(new VkDebugReportCallbackEXT() {
						public int invoke(int flags, int objectType, long object, long location, int messageCode,
								long pLayerPrefix, long pMessage, long pUserData) {
							System.err.println("ERROR OCCURED: " + VkDebugReportCallbackEXT.getString(pMessage));
							return 0;
						}
					}).pUserData(NULL).flags(VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT);

			LongBuffer pCallback = memAllocLong(1);
			VLKCheck(vkCreateDebugReportCallbackEXT(instance, dbgCreateInfo, null, pCallback),
					"Failed ot create debug callback");
			long callbackHandle = pCallback.get(0);
			memFree(pCallback);
			dbgCreateInfo.free();

			context.debug = callbackHandle;
		} else {
			context.debug = 0;
		}

		return context;
	}

	public static void destroyContext(VLKContext context) {
		vkDestroyDebugReportCallbackEXT(context.instance, context.debug, null);
	}
	
	public static VLKDevice createDevice(VLKContext context) {
		VLKDevice device = new VLKDevice();

		IntBuffer pPhysicalDeviceCount = memAllocInt(1);
		VLKCheck(vkEnumeratePhysicalDevices(context.instance, pPhysicalDeviceCount, null),
				"Failed to enumerate through physical devices");
		PointerBuffer pPhysicalDevices = memAllocPointer(pPhysicalDeviceCount.get(0));
		VLKCheck(vkEnumeratePhysicalDevices(context.instance, pPhysicalDeviceCount, pPhysicalDevices),
				"Failed to get physical device");
		long physicalDevice = pPhysicalDevices.get(0);
		memFree(pPhysicalDeviceCount);
		memFree(pPhysicalDevices);

		device.physicalDevice = new VkPhysicalDevice(physicalDevice, context.instance);

		IntBuffer pQueueFamilyPropertyCount = memAllocInt(1);
		vkGetPhysicalDeviceQueueFamilyProperties(device.physicalDevice, pQueueFamilyPropertyCount, null);
		int queueCount = pQueueFamilyPropertyCount.get(0);
		VkQueueFamilyProperties.Buffer queueProps = VkQueueFamilyProperties.calloc(queueCount);
		vkGetPhysicalDeviceQueueFamilyProperties(device.physicalDevice, pQueueFamilyPropertyCount, queueProps);
		memFree(pQueueFamilyPropertyCount);
		int graphicsQueueFamilyIndex;
		for (graphicsQueueFamilyIndex = 0; graphicsQueueFamilyIndex < queueCount; graphicsQueueFamilyIndex++) {
			if ((queueProps.get(graphicsQueueFamilyIndex).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0)
				break;
		}
		queueProps.free();
		FloatBuffer pQueuePriorities = memAllocFloat(1).put(0.0f);
		pQueuePriorities.flip();
		VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
				.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO).queueFamilyIndex(graphicsQueueFamilyIndex)
				.pQueuePriorities(pQueuePriorities);

		PointerBuffer extensions = memAllocPointer(1);
		ByteBuffer VK_KHR_SWAPCHAIN_EXTENSION = memUTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
		extensions.put(VK_KHR_SWAPCHAIN_EXTENSION);
		extensions.flip();
		PointerBuffer ppEnabledLayerNames = memAllocPointer(layers.length);
		for (int i = 0; validation && i < layers.length; i++)
			ppEnabledLayerNames.put(layers[i]);
		ppEnabledLayerNames.flip();

		VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
				.pNext(NULL)
				.pQueueCreateInfos(queueCreateInfo)
				.ppEnabledExtensionNames(extensions)
				.ppEnabledLayerNames(ppEnabledLayerNames);

		PointerBuffer pDevice = memAllocPointer(1);
		VLKCheck(vkCreateDevice(device.physicalDevice, deviceCreateInfo, null, pDevice), "Failed to create device");
		long deviceHandle = pDevice.get(0);
		memFree(pDevice);
		
		VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
		vkGetPhysicalDeviceMemoryProperties(device.physicalDevice, memoryProperties);

		device.device = new VkDevice(deviceHandle, device.physicalDevice, deviceCreateInfo);
		device.queueFamilyIndex = graphicsQueueFamilyIndex;
		device.memoryProperties = memoryProperties;

		deviceCreateInfo.free();
		memFree(ppEnabledLayerNames);
		memFree(VK_KHR_SWAPCHAIN_EXTENSION);
		memFree(extensions);
		memFree(pQueuePriorities);

		VkCommandPoolCreateInfo cmdPoolInfo = VkCommandPoolCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO).queueFamilyIndex(device.queueFamilyIndex)
				.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
		LongBuffer pCmdPool = memAllocLong(1);
		VLKCheck(vkCreateCommandPool(device.device, cmdPoolInfo, null, pCmdPool), "Failed to create command pool");
		long commandPool = pCmdPool.get(0);
		cmdPoolInfo.free();
		memFree(pCmdPool);

		device.commandPool = commandPool;

		PointerBuffer pQueue = memAllocPointer(1);
		vkGetDeviceQueue(device.device, device.queueFamilyIndex, 0, pQueue);
		device.queue = new VkQueue(pQueue.get(0), device.device);
		memFree(pQueue);

		VkCommandBufferAllocateInfo cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
				.commandPool(commandPool)
				.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
				.commandBufferCount(1);
		PointerBuffer pCommandBuffer = memAllocPointer(1);
		VLKCheck(vkAllocateCommandBuffers(device.device, cmdBufAllocateInfo, pCommandBuffer),
				"Failed to allocate command buffer");
		cmdBufAllocateInfo.free();
		device.commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device.device);
		memFree(pCommandBuffer);
		
		device.pCommandBuffers.put(0, device.commandBuffer);

		return device;
	}

	public static void imageBarrier(VkCommandBuffer cmdbuffer, long image, int aspectMask, int oldImageLayout, int srcAccess, int newImageLayout, int dstAccess) {
        // Create an image barrier object
        VkImageMemoryBarrier.Buffer imageMemoryBarrier = VkImageMemoryBarrier.calloc(1)
                .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .pNext(NULL)
                .oldLayout(oldImageLayout)
                .srcAccessMask(srcAccess)
                .newLayout(newImageLayout)
                .dstAccessMask(dstAccess)
                .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .image(image);
        imageMemoryBarrier.subresourceRange()
                .aspectMask(aspectMask)
                .baseMipLevel(0)
                .levelCount(1)
                .layerCount(1);

        // Put barrier on top
        int srcStageFlags = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
        int destStageFlags = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
        
        // Put barrier inside setup command buffer
        vkCmdPipelineBarrier(cmdbuffer, srcStageFlags, destStageFlags, VK_FLAGS_NONE,
                null, // no memory barriers
                null, // no buffer memory barriers
                imageMemoryBarrier); // one image memory barrier
        imageMemoryBarrier.free();
    }
	
	public static VLKSwapChain createSwapChain(VLKContext context, VLKDevice device, long window) {
		VLKSwapChain swapChain = new VLKSwapChain();
		
		LongBuffer surfaceBuffer = memAllocLong(1);
		glfwCreateWindowSurface(context.instance, window, null, surfaceBuffer);
		swapChain.surface = surfaceBuffer.get(0);
		memFree(surfaceBuffer);
		
		IntBuffer pQueueFamilyPropertyCount = memAllocInt(1);
        vkGetPhysicalDeviceQueueFamilyProperties(device.physicalDevice, pQueueFamilyPropertyCount, null);
        int queueCount = pQueueFamilyPropertyCount.get(0);
        VkQueueFamilyProperties.Buffer queueProps = VkQueueFamilyProperties.calloc(queueCount);
        vkGetPhysicalDeviceQueueFamilyProperties(device.physicalDevice, pQueueFamilyPropertyCount, queueProps);
        memFree(pQueueFamilyPropertyCount);

        // Iterate over each queue to learn whether it supports presenting:
        IntBuffer supportsPresent = memAllocInt(queueCount);
        for (int i = 0; i < queueCount; i++) {
            supportsPresent.position(i);
            VLKCheck(vkGetPhysicalDeviceSurfaceSupportKHR(device.physicalDevice, i, swapChain.surface, supportsPresent), 
            		"Failed to get physical device surface properties");
        }

        // Search for a graphics and a present queue in the array of queue families, try to find one that supports both
        int graphicsQueueNodeIndex = Integer.MAX_VALUE;
        int presentQueueNodeIndex = Integer.MAX_VALUE;
        for (int i = 0; i < queueCount; i++) {
            if ((queueProps.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                if (graphicsQueueNodeIndex == Integer.MAX_VALUE) {
                    graphicsQueueNodeIndex = i;
                }
                if (supportsPresent.get(i) == VK_TRUE) {
                    graphicsQueueNodeIndex = i;
                    presentQueueNodeIndex = i;
                    break;
                }
            }
        }
        queueProps.free();
        if (presentQueueNodeIndex == Integer.MAX_VALUE) {
            // If there's no queue that supports both present and graphics try to find a separate present queue
            for (int i = 0; i < queueCount; ++i) {
                if (supportsPresent.get(i) == VK_TRUE) {
                    presentQueueNodeIndex = i;
                    break;
                }
            }
        }
        memFree(supportsPresent);

        // Generate error if could not find both a graphics and a present queue
        if (graphicsQueueNodeIndex == Integer.MAX_VALUE) {
            throw new AssertionError("No graphics queue found");
        }
        if (presentQueueNodeIndex == Integer.MAX_VALUE) {
            throw new AssertionError("No presentation queue found");
        }
        if (graphicsQueueNodeIndex != presentQueueNodeIndex) {
            throw new AssertionError("Presentation queue != graphics queue");
        }

        // Get list of supported formats
        IntBuffer pFormatCount = memAllocInt(1);
        VLKCheck(vkGetPhysicalDeviceSurfaceFormatsKHR(device.physicalDevice, swapChain.surface, pFormatCount, null), 
        		"Failed to query physical device formats");
        int formatCount = pFormatCount.get(0);

        VkSurfaceFormatKHR.Buffer surfFormats = VkSurfaceFormatKHR.calloc(formatCount);
        VLKCheck(vkGetPhysicalDeviceSurfaceFormatsKHR(device.physicalDevice, swapChain.surface, pFormatCount, surfFormats), 
        		"Failed to query physical device formats");
        memFree(pFormatCount);

        // If the format list includes just one entry of VK_FORMAT_UNDEFINED, the surface has no preferred format. Otherwise, at least one supported format will
        // be returned.
        int colorFormat;
        if (formatCount == 1 && surfFormats.get(0).format() == VK_FORMAT_UNDEFINED) {
            colorFormat = VK_FORMAT_B8G8R8A8_UNORM;
        } else {
            colorFormat = surfFormats.get(0).format();
        }
        int colorSpace = surfFormats.get(0).colorSpace();
        surfFormats.free();

        swapChain.colorFormat = colorFormat;
        swapChain.colorSpace = colorSpace;
        
        VkSurfaceCapabilitiesKHR surfCaps = VkSurfaceCapabilitiesKHR.calloc();
        VLKCheck(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.physicalDevice, swapChain.surface, surfCaps), 
        		"Failed to query device surface capabilities");
        

        IntBuffer pPresentModeCount = memAllocInt(1);
        VLKCheck(vkGetPhysicalDeviceSurfacePresentModesKHR(device.physicalDevice, swapChain.surface, pPresentModeCount, null), 
        		"Failed to query presentation mode");
        int presentModeCount = pPresentModeCount.get(0);
        

        IntBuffer pPresentModes = memAllocInt(presentModeCount);
        VLKCheck(vkGetPhysicalDeviceSurfacePresentModesKHR(device.physicalDevice, swapChain.surface, pPresentModeCount, pPresentModes), 
        		"Failed to query presentation mode");
        memFree(pPresentModeCount);

        // Try to use mailbox mode. Low latency and non-tearing
        int swapchainPresentMode = VK_PRESENT_MODE_FIFO_KHR;
        for (int i = 0; i < presentModeCount; i++) {
            if (pPresentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) {
                swapchainPresentMode = VK_PRESENT_MODE_MAILBOX_KHR;
                break;
            }
            if ((swapchainPresentMode != VK_PRESENT_MODE_MAILBOX_KHR) && (pPresentModes.get(i) == VK_PRESENT_MODE_IMMEDIATE_KHR)) {
                swapchainPresentMode = VK_PRESENT_MODE_IMMEDIATE_KHR;
            }
        }
        memFree(pPresentModes);

        // Determine the number of images
        int desiredNumberOfSwapchainImages = surfCaps.minImageCount() + 1;
        if ((surfCaps.maxImageCount() > 0) && (desiredNumberOfSwapchainImages > surfCaps.maxImageCount())) {
            desiredNumberOfSwapchainImages = surfCaps.maxImageCount();
        }
        
        int[] widthAr = {0};
        int[] heightAr = {0};
        
        glfwGetWindowSize(window, widthAr, heightAr);
        
        int width = widthAr[0];
        int height = heightAr[0];
        
        VkExtent2D currentExtent = surfCaps.currentExtent();
        int currentWidth = currentExtent.width();
        int currentHeight = currentExtent.height();
        if (currentWidth != -1 && currentHeight != -1) {
            width = currentWidth;
            height = currentHeight;
        }

        int preTransform;
        if ((surfCaps.supportedTransforms() & VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR) != 0) {
            preTransform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
        } else {
            preTransform = surfCaps.currentTransform();
        }
        surfCaps.free();

        VkSwapchainCreateInfoKHR swapchainCI = VkSwapchainCreateInfoKHR.calloc()
                .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                .pNext(NULL)
                .surface(swapChain.surface)
                .minImageCount(desiredNumberOfSwapchainImages)
                .imageFormat(colorFormat)
                .imageColorSpace(colorSpace)
                .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .preTransform(preTransform)
                .imageArrayLayers(1)
                .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                .pQueueFamilyIndices(null)
                .presentMode(swapchainPresentMode)
                .oldSwapchain(VK_NULL_HANDLE)
                .clipped(true)
                .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
        swapchainCI.imageExtent()
                .width(width)
                .height(height);
        LongBuffer pSwapChain = memAllocLong(1);
        VLKCheck(vkCreateSwapchainKHR(device.device, swapchainCI, null, pSwapChain), 
        		"Failed to create swapchain");
        swapchainCI.free();
        swapChain.swapChain = pSwapChain.get(0);
        memFree(pSwapChain);

        IntBuffer pImageCount = memAllocInt(1);
        VLKCheck(vkGetSwapchainImagesKHR(device.device, swapChain.swapChain, pImageCount, null), 
        		"Failed to query swapchain images");
        int imageCount = pImageCount.get(0);
        

        LongBuffer pSwapchainImages = memAllocLong(imageCount);
        VLKCheck(vkGetSwapchainImagesKHR(device.device, swapChain.swapChain, pImageCount, pSwapchainImages), 
        		"Failed to query swapchain images");
        memFree(pImageCount);

        long[] images = new long[imageCount];
        long[] imageViews = new long[imageCount];
        LongBuffer pBufferView = memAllocLong(1);
        VkImageViewCreateInfo colorAttachmentView = VkImageViewCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .pNext(NULL)
                .format(colorFormat)
                .viewType(VK_IMAGE_VIEW_TYPE_2D)
                .flags(VK_FLAGS_NONE);
        colorAttachmentView.components()
                .r(VK_COMPONENT_SWIZZLE_R)
                .g(VK_COMPONENT_SWIZZLE_G)
                .b(VK_COMPONENT_SWIZZLE_B)
                .a(VK_COMPONENT_SWIZZLE_A);
        colorAttachmentView.subresourceRange()
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);
        for (int i = 0; i < imageCount; i++) {
            images[i] = pSwapchainImages.get(i);
            // Bring the image from an UNDEFINED state to the VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT state
            imageBarrier(device.commandBuffer, images[i], VK_IMAGE_ASPECT_COLOR_BIT,
                    VK_IMAGE_LAYOUT_UNDEFINED, 0,
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
            colorAttachmentView.image(images[i]);
            VLKCheck(vkCreateImageView(device.device, colorAttachmentView, null, pBufferView), 
            		"Failed to create image views");
            imageViews[i] = pBufferView.get(0);
            
        }
        colorAttachmentView.free();
        memFree(pBufferView);
        memFree(pSwapchainImages);

        swapChain.images = images;
        swapChain.imageViews = imageViews;
		
        VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(1)
                .format(colorFormat)
                .samples(VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                .finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(1)
                .attachment(0)
                .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1)
                .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                .flags(VK_FLAGS_NONE)
                .pInputAttachments(null)
                .colorAttachmentCount(colorReference.remaining())
                .pColorAttachments(colorReference)
                .pResolveAttachments(null)
                .pDepthStencilAttachment(null)
                .pPreserveAttachments(null);

        VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                .pNext(NULL)
                .pAttachments(attachments)
                .pSubpasses(subpass)
                .pDependencies(null);

        LongBuffer pRenderPass = memAllocLong(1);
        VLKCheck(vkCreateRenderPass(device.device, renderPassInfo, null, pRenderPass),
        		"Failed to create renderPass");
        swapChain.renderPass = pRenderPass.get(0);
        memFree(pRenderPass);
        renderPassInfo.free();
        colorReference.free();
        subpass.free();
        attachments.free();
        
        LongBuffer frattachments = memAllocLong(1);
        VkFramebufferCreateInfo fci = VkFramebufferCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                .pAttachments(frattachments)
                .flags(VK_FLAGS_NONE)
                .height(height)
                .width(width)
                .layers(1)
                .pNext(NULL)
                .renderPass(swapChain.renderPass);
        
        swapChain.framebuffers = new long[swapChain.images.length];
        LongBuffer pFramebuffer = memAllocLong(1);
        for (int i = 0; i < swapChain.images.length; i++) {
            frattachments.put(0, swapChain.imageViews[i]);
            VLKCheck(vkCreateFramebuffer(device.device, fci, null, pFramebuffer),
            		"Failed to create framebuffer");
            long framebuffer = pFramebuffer.get(0);
            swapChain.framebuffers[i] = framebuffer;
        }
        memFree(frattachments);
        memFree(pFramebuffer);
        fci.free();
        
		return swapChain;
	}
	
	public static void clear(VLKDevice device, VLKSwapChain swapChain, Vector4f color) {
		// Info struct to create a semaphore
        VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
                .pNext(NULL)
                .flags(VLK.VK_FLAGS_NONE);
		
        VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc()
	                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
	                .pNext(NULL);
        
        // Specify clear color (cornflower blue)
	    VkClearValue.Buffer clearValues = VkClearValue.calloc(1);
	    clearValues.color()
	                .float32(0, color.x)
	                .float32(1, color.y)
	                .float32(2, color.z)
	                .float32(3, color.w);
		
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
        
        vkCreateSemaphore(device.device, semaphoreCreateInfo, null, swapChain.pImageAcquiredSemaphore);
        vkCreateSemaphore(device.device, semaphoreCreateInfo, null, swapChain.pRenderCompleteSemaphore);
		
        vkAcquireNextImageKHR(device.device, swapChain.swapChain, VLK.UINT64_MAX, swapChain.pImageAcquiredSemaphore.get(0), VK_NULL_HANDLE, swapChain.pImageIndex);
        
        renderPassBeginInfo.framebuffer(swapChain.framebuffers[swapChain.pImageIndex.get(0)]);
        
        vkBeginCommandBuffer(device.commandBuffer, cmdBufInfo);
        
        VkImageMemoryBarrier.Buffer postPresentBarrier = VkImageMemoryBarrier.calloc(1)
                .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .pNext(NULL)
                .srcAccessMask(0)
                .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
                .oldLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
                .newLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        postPresentBarrier.subresourceRange()
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);
        postPresentBarrier.image(swapChain.images[swapChain.pImageIndex.get(0)]);
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

        VkRect2D.Buffer scissor = VkRect2D.calloc(1);
        scissor.extent()
                .width(800)
                .height(600);
        scissor.offset()
                .x(0)
                .y(0);
        vkCmdSetScissor(device.commandBuffer, 0, scissor);
        scissor.free();

	}
	
	public static void swap(VLKDevice device, VLKSwapChain swapChain) {
		vkCmdEndRenderPass(device.commandBuffer);
        
        VkImageMemoryBarrier.Buffer prePresentBarrier = VkImageMemoryBarrier.calloc(1)
                .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .pNext(NULL)
                .srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
                .dstAccessMask(0)
                .oldLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                .newLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
                .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        prePresentBarrier.subresourceRange()
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);
        prePresentBarrier.image(swapChain.images[swapChain.pImageIndex.get(0)]);
        vkCmdPipelineBarrier(device.commandBuffer,
            VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
            VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
            VLK.VK_FLAGS_NONE,
            null, // No memory barriers
            null, // No buffer memory barriers
            prePresentBarrier); // One image memory barrier
        prePresentBarrier.free();
        
        vkEndCommandBuffer(device.commandBuffer);
        
        // Info struct to submit a command buffer which will wait on the semaphore
        IntBuffer pWaitDstStageMask = memAllocInt(1);
        pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(NULL)
                .waitSemaphoreCount(swapChain.pImageAcquiredSemaphore.remaining())
                .pWaitSemaphores(swapChain.pImageAcquiredSemaphore)
                .pWaitDstStageMask(pWaitDstStageMask)
                .pCommandBuffers(device.pCommandBuffers)
                .pSignalSemaphores(swapChain.pRenderCompleteSemaphore);

        
        
        vkQueueSubmit(device.queue, submitInfo, VK_NULL_HANDLE);
        
        swapChain.pSwapchains.put(0, swapChain.swapChain);
        
        // Info struct to present the current swapchain image to the display
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc()
                .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                .pNext(NULL)
                .pWaitSemaphores(swapChain.pRenderCompleteSemaphore)
                .swapchainCount(swapChain.pSwapchains.remaining())
                .pSwapchains(swapChain.pSwapchains)
                .pImageIndices(swapChain.pImageIndex)
                .pResults(null);
        vkQueuePresentKHR(device.queue, presentInfo);
        
        vkQueueWaitIdle(device.queue);
        
        vkDestroySemaphore(device.device, swapChain.pImageAcquiredSemaphore.get(0), null);
        vkDestroySemaphore(device.device, swapChain.pRenderCompleteSemaphore.get(0), null);
	}
}
