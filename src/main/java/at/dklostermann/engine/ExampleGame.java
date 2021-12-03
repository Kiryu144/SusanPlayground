package at.dklostermann.engine;

public class ExampleGame extends Game
{
    private Texture shipTexture;

    @Override
    protected void onInit()
    {
        shipTexture = Texture.loadTextureFromResource("/ship.png");
    }

    @Override
    protected void onRender(float delta, int windowWidth, int windowHeight)
    {
        Rect shipRect = new Rect(windowWidth / 2.0f - shipTexture.getWidth() / 2.0f, windowHeight - 300.0f, 100.0f, 100.0f);
        this.drawTexture(shipTexture, shipRect, (float) (System.currentTimeMillis() / 10.0 % 360.0), (int) (System.currentTimeMillis() % (long) 0x00FFFFFF) | 0xFF000000);
    }

    public static void main(String[] args)
    {
        new ExampleGame().run();
    }
}
