package at.dklostermann.engine;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class Game
{
    // The window handle
    private long window;

    protected int lastWindowWidth, lastWindowHeight;

    public void run()
    {
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init()
    {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(1440, 900, "Window", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->
        {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        glfwSetErrorCallback((i, l) ->
        {
            System.out.printf("Error: %d %d%n", i, l);
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush())
        {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop()
    {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        this.onInit();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glfwSetWindowSizeCallback(window, (long window, int w, int h) ->
        {
            glViewport(0, 0, w, h);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0.0f, w, h, 0.0f, 0.0f, 1.0f);
        });

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(window, w, h);

        glViewport(0, 0, w.get(0), h.get(0));
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0f, w.get(0), h.get(0), 0.0f, 0.0f, 1.0f);

        long lastTime = System.nanoTime();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window))
        {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            IntBuffer ww = BufferUtils.createIntBuffer(1);
            IntBuffer hh = BufferUtils.createIntBuffer(1);
            glfwGetWindowSize(window, ww, hh);
            lastWindowWidth = ww.get();
            lastWindowHeight = hh.get();

            long now = System.nanoTime();
            long delta = now - lastTime;
            double deltaS = delta / 1e6f;
            lastTime = now;

            this.onRender((float) deltaS, lastWindowWidth, lastWindowHeight);

            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();
        }
    }

    protected void drawTexture(Texture texture, Rect target)
    {
        this.drawTexture(texture, target, null);
    }

    protected void drawTexture(Texture texture, Rect target, Rect source)
    {
        if (texture == null || target == null)
        {
            return;
        }

        if (source == null)
        {
            source = new Rect(0.0f, 0.0f, 1.0f, 1.0f);
        } else
        {
            source = new Rect(source.getX() / texture.getWidth(), source.getY() / texture.getHeight(), source.getWidth() / texture.getWidth(), source.getHeight() / texture.getHeight());
        }

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBindTexture(GL_TEXTURE_2D, texture.getTextureID());

        glBegin(GL_QUADS);
        glTexCoord2f(source.getX(), source.getY());
        glVertex2f(target.getX(), target.getY());

        glTexCoord2f(source.getX(), source.getY() + source.getHeight());
        glVertex2f(target.getX(), target.getY() + target.getHeight());

        glTexCoord2f(source.getX() + source.getWidth(), source.getY() + source.getHeight());
        glVertex2f(target.getX() + target.getWidth(), target.getY() + target.getHeight());

        glTexCoord2f(source.getX() + source.getWidth(), source.getY());
        glVertex2f(target.getX() + target.getWidth(), target.getY());

        glEnd();

        glDisable(GL_TEXTURE_2D);
    }

    protected boolean isKeyDown(int key)
    {
        return glfwGetKey(this.window, key) == GLFW_PRESS;
    }

    protected abstract void onInit();

    protected abstract void onRender(float delta, int windowWidth, int windowHeight);
}























