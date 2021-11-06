package at.dklostermann.engine;

import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture
{
    private final int id;
    private int width;
    private int height;

    public Texture()
    {
        id = glGenTextures();
    }

    public void bind()
    {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void setParameter(int name, int value)
    {
        glTexParameteri(GL_TEXTURE_2D, name, value);
    }

    public void uploadData(int width, int height, ByteBuffer data)
    {
        uploadData(GL_RGBA8, width, height, GL_RGBA, data);
    }

    public void uploadData(int internalFormat, int width, int height, int format, ByteBuffer data)
    {
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, data);
    }

    public void delete()
    {
        glDeleteTextures(id);
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        if (width > 0)
        {
            this.width = width;
        }
    }

    public int getHeight()
    {
        return height;
    }

    public int getTextureID()
    {
        return id;
    }

    public void setHeight(int height)
    {
        if (height > 0)
        {
            this.height = height;
        }
    }

    public static Texture createTexture(int width, int height, ByteBuffer data)
    {
        Texture texture = new Texture();
        texture.setWidth(width);
        texture.setHeight(height);

        texture.bind();

        texture.setParameter(GL_TEXTURE_WRAP_S, GL40.GL_CLAMP_TO_BORDER);
        texture.setParameter(GL_TEXTURE_WRAP_T, GL40.GL_CLAMP_TO_BORDER);
        texture.setParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        texture.setParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        texture.uploadData(GL_RGBA8, width, height, GL_RGBA, data);

        return texture;
    }

    public static Texture loadTextureFromFile(String filepath)
    {
        try
        {
            return loadTexture(new FileInputStream(filepath));
        } catch (IOException e)
        {
            throw new RuntimeException("Unable to load texture file from disk: " + System.lineSeparator() + e);
        }
    }

    public static Texture loadTextureFromResource(String resourcepath)
    {
        try
        {
            return loadTexture(Texture.class.getResourceAsStream(resourcepath));
        } catch (IOException | NullPointerException e)
        {
            throw new RuntimeException("Unable to load texture file from resources: " + System.lineSeparator() + e);
        }
    }

    public static Texture loadTexture(InputStream in) throws IOException
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(in.available());
        Channels.newChannel(in).read(byteBuffer);
        byteBuffer.flip();

        ByteBuffer nativeBuffer = MemoryUtil.memAlloc(byteBuffer.array().length);
        byteBuffer.rewind();
        nativeBuffer.put(byteBuffer);
        byteBuffer.rewind();
        nativeBuffer.flip();

        ByteBuffer image;
        int width, height;
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            /* Prepare image buffers */
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            /* Load image */
            stbi_set_flip_vertically_on_load(false);
            image = stbi_load_from_memory(nativeBuffer, w, h, comp, 4);
            MemoryUtil.memFree(nativeBuffer);

            if (image == null)
            {
                throw new RuntimeException("Failed to load texture file: "
                        + System.lineSeparator() + stbi_failure_reason());
            }

            /* Get width and height of image */
            width = w.get();
            height = h.get();
        }

        return createTexture(width, height, image);
    }
}
