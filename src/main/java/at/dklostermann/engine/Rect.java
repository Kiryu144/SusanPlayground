package at.dklostermann.engine;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Rect implements Cloneable
{
    private float x;
    private float y;
    private float width;
    private float height;

    @Override
    public Rect clone()
    {
        return new Rect(x, y, width, height);
    }

    public boolean isColliding(Rect other)
    {
        if (other == null)
        {
            return false;
        }

        if (this == other)
        {
            return true;
        }

        return !(this.x + this.width < other.x || this.y + this.height < other.y || this.x > other.x + other.width || this.y > other.y + other.height);
    }
}
