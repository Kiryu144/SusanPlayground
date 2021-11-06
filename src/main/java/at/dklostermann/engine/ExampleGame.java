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
        Rect shipRect = new Rect(windowWidth / 2.0f - shipTexture.getWidth() / 2.0f, windowHeight - 100.0f, 100.0f, 100.0f);
        this.drawTexture(shipTexture, shipRect);
    }

    public static void main(String[] args)
    {
        new ExampleGame().run();
    }
}
